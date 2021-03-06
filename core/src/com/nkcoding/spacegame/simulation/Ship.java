package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ExternalMethodHandler;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.compiler.CompileException;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.Program;
import com.nkcoding.spacegame.GameScriptProvider;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalProperty;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertySpecification;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDefBase;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Ship extends Simulated {

    public static final short REMOVE_COMPONENT = -1;
    public static final short REMOVE_COMPONENTS = -2;
    public static final short UPDATE_COMPONENT = -3;

    public ShipModel model = null;

    private List<Component> components = new ArrayList<>();

    private List<Component> iterationList;

    //the map of the components
    private final Component[][] componentsMap = new Component[ShipDef.MAX_SIZE][ShipDef.MAX_SIZE];

    private boolean componentsChanged = true;

    private Ship(SpaceSimulation spaceSimulation, short owner, int id) {
        super(SimulatedType.Ship, spaceSimulation, BodyDef.BodyType.DynamicBody, 1, owner, id);
        setSyncPriority(SynchronizationPriority.HIGH);
    }

    /**
     * constructor for deserialization
     */
    private Ship(SpaceSimulation spaceSimulation, DataInputStream inputStream) throws IOException{
        super(SimulatedType.Ship, spaceSimulation, BodyDef.BodyType.DynamicBody, 1, inputStream);

        int componentAmount = inputStream.readInt();
        for (int i = 0; i < componentAmount; i++) {
            addComponent(ComponentDefBase.deserialize(inputStream).deserializeComponent(this, inputStream));
        }
        updateCenterPos();
    }

    public Ship(ShipDef shipDef, SpaceSimulation spaceSimulation, Vector2 initialPosition) {
        this(spaceSimulation, spaceSimulation.getClientID(), spaceSimulation.getNewId());
        getBody().setTransform(initialPosition, 0);
        model = new ShipModel(shipDef, spaceSimulation);
        for (ComponentDef comDef : shipDef.componentDefs) {
            createComponent(comDef);
        }
        model.initComponentProperties();
        //update center pos
        updateCenterPos();
    }

    public Ship(Ship oldShip, ShipModel oldModel, List<Component> components) {
        this(oldShip.getSpaceSimulation(), oldShip.getSpaceSimulation().getClientID(), oldShip.getSpaceSimulation().getNewId());
        model = new ShipModel(oldShip, oldModel, components);
        for (Component component : components) {
            addComponent(component);
            component.setShip(this);
        }
        //update center pos
        updateCenterPos();
    }

    public static Ship deserialize(SpaceSimulation spaceSimulation, DataInputStream inputStream) throws IOException {
        return new Ship(spaceSimulation, inputStream);
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException{
        super.serialize(outputStream);
        outputStream.writeInt(components.size());
        for (Component component : components) {
            component.serialize(outputStream);
        }
        serializeBodyState(outputStream);
    }

    @Override
    public UpdateTransmission deserializeTransmission(DataInputStream inputStream, short updateID) throws IOException{
        switch (updateID) {
            case REMOVE_COMPONENT:
                return new RemoveComponentTransmission(inputStream);
            case REMOVE_COMPONENTS:
                return new RemoveComponentsTransmission(inputStream);
            case UPDATE_COMPONENT:
                short componentUpdateID = inputStream.readShort();
                switch (componentUpdateID) {
                    case ComponentUpdateID.DAMAGE:
                        return new DamageTransmission(inputStream);
                    case ComponentUpdateID.RADIUS:
                        return new RadiusTransmission(inputStream);
                    case ComponentUpdateID.SHIELD:
                        return new ShieldTransmission(inputStream);
                    default:
                        throw new IllegalStateException("unknown ComponentID: " + componentUpdateID);
                }
            default:
                return super.deserializeTransmission(inputStream, updateID);
        }
    }

    /**
     * the default act method
     * a subclass must call this, otherwise positioning will not work
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        //check if list has changed
        if (componentsChanged) {
            iterationList = List.copyOf(components);
            updateCenterPos();
        }
        if (isOriginal()) {
            model.act(delta);
        }
        boolean isOriginal = isOriginal();
        for (Component component : iterationList) {
            component.act(delta, isOriginal);
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        for (Component component : components) component.draw(batch, isOriginal());
    }

    /**
     * subclasses should overwrite this method if they want to receive update transmissions
     *
     * @param transmission the update transmission
     */
    @Override
    public void receiveTransmission(UpdateTransmission transmission) {
        super.receiveTransmission(transmission);
        switch (transmission.updateID) {
            case REMOVE_COMPONENT:
                RemoveComponentTransmission rct = (RemoveComponentTransmission) transmission;
                removeComponent(rct.x, rct.y);
                break;
            case REMOVE_COMPONENTS:
                RemoveComponentsTransmission rcst = (RemoveComponentsTransmission) transmission;
                for (int[] pair : rcst.components) {
                    removeComponent(pair[0], pair[1]);
                }
                break;
            default:
                UpdateComponentTransmission uct = (UpdateComponentTransmission) transmission;
                componentsMap[uct.x][uct.y].receiveTransmission(uct);
                break;
        }
    }

    /**
     * removes a component and performs a structure check if necessary
     *
     * @param component the Component to destroy
     */
    public void destroyComponent(Component component) {
        post(new RemoveComponentTransmission(component, id));
        if (isOriginal()) {
            model.isStructureCheckNecessary = true;
        }
    }

    /**
     * CONSTRUCTOR USE ONLY
     * THIS DOES NOT HANDLE SYNCHRONISATION
     * adds a Component to this ship
     * handles Actor.addActor, components, componentsMap
     *
     * @param component the Component to add
     */
    private void addComponent(Component component) {
        componentsChanged = true;
        ComponentDefBase def = component.getComponentDef();
        components.add(component);

        //add to map
        for (int _x = def.getX(); _x < (def.getX() + def.getRealWidth()); _x++) {
            for (int _y = def.getY(); _y < (def.getY() + def.getRealHeight()); _y++) {
                componentsMap[_x][_y] = component;
            }
        }
        if (isOriginal()) {
            model.addComponentInternally(component);
        }
        component.addComponent();
    }

    /**
     * create an original Component out of a ComponentDef
     */
    private void createComponent(ComponentDef comDef) {
        Component component = comDef.createComponent(this, model);
        addComponent(component);
    }

    /**
     * removes a component
     * handles Actor.addActor, components, componentsMap
     * only call this from receive!!!
     * THIS DOES NOT HANDLE SYNCHRONISATION
     */
    private void removeComponent(int x, int y) {
        Component component = componentsMap[x][y];
        component.removeFixtures();
        componentsChanged = true;
        components.remove(component);
        //remove from map
        ComponentDefBase comDef = component.getComponentDef();
        for (int _x = comDef.getX(); _x < (comDef.getX() + comDef.getRealWidth()); _x++) {
            for (int _y = comDef.getY(); _y < (comDef.getY() + comDef.getRealHeight()); _y++) {
                componentsMap[_x][_y] = null;
            }
        }

        if (isOriginal()) {
            model.removeComponentInternally(component);
        }
        component.removeComponent();
    }

    //update the center position
    private void updateCenterPos() {
        int minX = components.stream().mapToInt(component -> component.getComponentDef().getX()).min().orElse(0);
        int maxX = components.stream().mapToInt(component -> (component.getComponentDef().getX() + component.getComponentDef().getRealWidth())).max().orElse(ShipDef.MAX_SIZE);
        int minY = components.stream().mapToInt(component -> component.getComponentDef().getY()).min().orElse(0);
        int maxY = components.stream().mapToInt(component -> (component.getComponentDef().getY() + component.getComponentDef().getRealHeight())).max().orElse(ShipDef.MAX_SIZE);
        float posX = (minX + maxX) / 2f * ShipDef.UNIT_SIZE;
        float posY = (minY + maxY) / 2f * ShipDef.UNIT_SIZE;
        centerPosition = new Vector2(posX, posY);
        width = (maxX - minX) * ShipDef.UNIT_SIZE;
        height = (maxY - minY) * ShipDef.UNIT_SIZE;
        radius = (float) Math.sqrt(height * height + width * width);
    }

    /**
     * initializes the Ship type
     * adds ExternalMethod handlers
     */
    public static void initializeType(final GameScriptProvider engine) {
        Consumer<ExternalMethodFuture> handleGetterSetter = (future ->
        {
            ExternalMethodHandler handler =  engine.getExternalMethodHandler((String)future.getParameters()[0]);
            if (handler != null) {
                handler.handleExternalMethod(future);
            }
        });

        for (ComponentType type : ComponentType.values()) {
            for (ExternalPropertySpecification specification : type.propertySpecifications) {
                if (specification.supportsRead) {
                    engine.addExternalMethod(specification.createGetter(), specification.supportsConcurrentHandling, handleGetterSetter);
                }
                if (specification.supportsWrite) {
                    engine.addExternalMethod(specification.createSetter(), specification.supportsConcurrentHandling, handleGetterSetter);
                }

            }
        }
    }

    public class ShipModel {
        //global variables
        public final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables;
        //the list of components which compose the ship
        private List<Component.ComponentModel> components;
        //corresponds the order of the components to the order of the PowerLevel?
        private boolean isPowerLevelOrderCorrect = false;
        //did the power request change?
        private boolean isPowerRequestDifferent = true;
        //is a structure check necessary
        private boolean isStructureCheckNecessary = true;

        private HashMap<String, MethodStatement> methods;

        //construct Ship out of ShipDef (public constructor)
        public ShipModel(ShipDef def, SpaceSimulation spaceSimulation) {
            if (!def.getValidated()) throw new IllegalArgumentException("shipDef is not validated");
            //compile the script
            Compiler compiler = spaceSimulation.createCompiler(def.code);
            Program program = null;
            try {
                program = compiler.compile();
            } catch (CompileException e) {
                e.printStackTrace();
            }
            globalVariables = program.globalVariables;
            methods = new HashMap<>();
            for (MethodStatement statement : program.methods) {
                methods.put(statement.getDefinition().getName(), statement);
            }
            //init new list with all the components
            components = new ArrayList<>(def.componentDefs.size());
        }
        //endregion

        //package-private constructor to construct Ship out of components (used to split up a ship)
        //pass other ship to copy important stuff (external method stuff etc.)
        ShipModel(Ship oldShip, ShipModel oldModel, List<Component> components) {
            //set globalVariables
            this.globalVariables = oldModel.globalVariables;
            //set the components
            this.components = new ArrayList<>(components.size());
            Body oldBody = oldShip.getBody();
            Body body = getBody();
            body.setTransform(oldBody.getPosition(), oldBody.getAngle());
            updateLinearVelocity(oldBody);
            body.setAngularVelocity(oldBody.getAngularVelocity());
        }

        private void initComponentProperties() {
            for (Component.ComponentModel componentModel : components) {
                componentModel.initProperties(componentModel.getComponentDef().properties.values(), methods);
            }
        }

        private void addComponentInternally(Component component) {
            components.add(component.model);
            getSpaceSimulation().addExternalPropertyHandler(component.model);
        }

        private void removeComponentInternally(Component component) {
            components.remove(component.model);
            getSpaceSimulation().removeExternalPropertyHandler(component.model);
        }

        //checks if the ship structure is intact or constructs partial ships otherwise
        private void checkStructure() {
            if (components.size() > 0) {
                checkStructureRec(components.get(0));
                List<Component.ComponentModel> otherComponents = components.stream().filter(com -> !com.structureHelper).collect(Collectors.toList());
                //remove other components
                post(new RemoveComponentsTransmission(otherComponents, id));
                //reset remaining
                components.forEach(component -> component.structureHelper = false);
                //create new ship
                if (otherComponents.size() > 0) getSpaceSimulation().addSimulated(new Ship(Ship.this, this,
                        otherComponents.stream().map(Component.ComponentModel::getComponent).collect(Collectors.toList())));
                updateLinearVelocity(getBody());
            } else {
                getSpaceSimulation().removeSimulated(Ship.this);
            }
        }

        //checks the structure recursive (helper for checkStructure())
        private void checkStructureRec(Component.ComponentModel component) {
            component.structureHelper = true;
            ComponentDef comDef = component.getComponentDef();
            //go around component
            //check left side
            if (comDef.getX() > 0) {
                //there is a left side
                for (int y = comDef.getY(); y < (comDef.getY() + comDef.getRealHeight()); y++) {
                    Component nextComponent = componentsMap[comDef.getX() - 1][y];
                    if (nextComponent != null && !nextComponent.model.structureHelper &&
                            nextComponent.model.attachComponentAtRaw(comDef.getX() - 1, y, Component.RIGHT_SIDE) &&
                            component.attachComponentAtRaw(comDef.getX(), y, Component.LEFT_SIDE))
                        checkStructureRec(nextComponent.model);
                }
            }
            //check bottom side
            if (comDef.getY() > 0) {
                //there is a top side
                for (int x = comDef.getX(); x < (comDef.getX() + comDef.getRealWidth()); x++) {
                    Component nextComponent = componentsMap[x][comDef.getY() - 1];
                    if (nextComponent != null && !nextComponent.model.structureHelper &&
                            nextComponent.model.attachComponentAtRaw(x, comDef.getY() - 1, Component.TOP_SIDE) &&
                            component.attachComponentAtRaw(x, comDef.getY(), Component.BOTTOM_SIDE))
                        checkStructureRec(nextComponent.model);
                }
            }
            //check right side
            if ((comDef.getX() + comDef.getRealWidth()) < (ShipDef.MAX_SIZE)) {
                //there is a right side
                for (int y = comDef.getY(); y < (comDef.getY() + comDef.getRealHeight()); y++) {
                    Component nextComponent = componentsMap[comDef.getX() + comDef.getRealWidth()][y];
                    if (nextComponent != null && !nextComponent.model.structureHelper &&
                            nextComponent.model.attachComponentAtRaw(comDef.getX() + comDef.getRealWidth(), y, Component.LEFT_SIDE) &&
                            component.attachComponentAtRaw(comDef.getX() + comDef.getRealWidth() - 1, y, Component.RIGHT_SIDE))
                        checkStructureRec(nextComponent.model);
                }
            }
            //check top side
            if ((comDef.getY() + comDef.getRealHeight()) < (ShipDef.MAX_SIZE)) {
                //there is a bottom side
                for (int x = comDef.getX(); x < (comDef.getX() + comDef.getRealWidth()); x++) {
                    Component nextComponent = componentsMap[x][comDef.getY() + comDef.getRealHeight()];
                    if (nextComponent != null && !nextComponent.model.structureHelper &&
                            nextComponent.model.attachComponentAtRaw(x, comDef.getY() + comDef.getRealHeight(), Component.BOTTOM_SIDE) &&
                            component.attachComponentAtRaw(x, comDef.getY() + comDef.getRealHeight() - 1, Component.TOP_SIDE))
                        checkStructureRec(nextComponent.model);
                }
            }
        }

        //region power system
        public void invalidatePowerLevelOrder() {
            isPowerLevelOrderCorrect = false;
            invalidatePowerDelivery();
        }

        public void invalidatePowerDelivery() {
            isPowerRequestDifferent = true;
        }

        //endregion

        //update the linear velocity, after some structure changes
        private void updateLinearVelocity(Body oldBody) {
            body.setLinearVelocity(oldBody.getLinearVelocityFromLocalPoint(body.getLocalCenter()));
        }

        public void act(float delta) {
            for (Component.ComponentModel component : components) {
                component.getProperties().values().forEach(ExternalProperty::update);
            }
            //check structure if necessary
            if (isStructureCheckNecessary) {
                checkStructure();
                isStructureCheckNecessary = false;
            }
            //check if power level order is correct
            if (!isPowerLevelOrderCorrect) {
                //change the order
                components.sort(Comparator.comparingInt(com -> com.requestLevel.get()));
                isPowerLevelOrderCorrect = true;
            }
            //update the power stuff
            if (isPowerRequestDifferent) {
                updatePowerDistribution();
                isPowerRequestDifferent = false;
            }

            //I know this is hacky, but it's the best I have
            for (Component.ComponentModel component : components) {
                component.getProperties().values()
                        .forEach(property -> property.startChangedHandler(getSpaceSimulation().getScriptingEngine(), globalVariables));
            }
        }

        //sets powerReceived on all components
        private void updatePowerDistribution() {
            //update how much power each component
            //check how much power is available
            float availablePower = 0f;
            for (Component.ComponentModel component : components) {
                //check if it consumes or delivers power
                if (component.powerRequested.get() < 0) {
                    availablePower -= component.powerRequested.get();
                }
            }
            float startAvailablePower = availablePower;
            //now check for every level how much power each individual component gets
            int i = 0;
            while (i < components.size()) {
                //is power available?
                if (availablePower > 0) {
                    //there is power available
                    int i0 = i;
                    int level = components.get(i).requestLevel.get();
                    float levelPowerRequest = 0f;
                    //check how much power is necessary for this level
                    while (i < components.size() && components.get(i).requestLevel.get() == level) {
                        if (components.get(i).powerRequested.get() > 0)
                            levelPowerRequest += components.get(i).powerRequested.get();
                        i++;
                    }
                    //is enough power available
                    if (levelPowerRequest <= availablePower) {
                        //enough power is available
                        availablePower -= levelPowerRequest;
                        for (int x = i0; x < i; x++) {
                            components.get(x).powerReceived.set(components.get(x).powerRequested.get());
                        }
                    } else {
                        //not enough power is available
                        float fac = availablePower / levelPowerRequest;
                        availablePower = 0f;
                        for (int x = i0; x < i; x++) {
                            components.get(x).powerReceived.set(components.get(x).powerRequested.get() * fac);
                        }
                    }
                } else {
                    //no power is available, set all to 0
                    while (i < components.size()) {
                        components.get(i).powerReceived.set(0f);
                        i++;
                    }
                }

            }
            //update the components which deliverPower
            float fac = (startAvailablePower - availablePower) / availablePower;
            for (Component.ComponentModel component : components) {
                //check if it consumes or delivers power
                if (component.powerRequested.get() < 0) {
                    component.powerReceived.set(component.powerRequested.get() * fac);
                }
            }
        }
    }

}
