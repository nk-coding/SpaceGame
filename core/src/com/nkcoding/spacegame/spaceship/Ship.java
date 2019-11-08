package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.compiler.CompileException;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.Program;
import com.nkcoding.spacegame.SpaceSimulation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Ship extends Simulated implements ExternalPropertyHandler {
    //region keys for the properties
    public static final String KeyDownKey = "KeyDown";
    public static final String KeyUpKey = "KeyUp";
    public static final String AngularVelocityKey = "AngularVelocity";
    public static final String VelocityKey = "Velocity";
    public static final String CameraFocusKey = "CameraFocus";
    //endregion

    private HashMap<String, ExternalProperty> properties = new HashMap<>();

    @Override
    public Map<String, ExternalProperty> getProperties() {
        return properties;
    }

    //the list of components which compose the ship
    private List<Component> components;

    //copied list for iteration
    private List<Component> iterationList;

    //has the normal list changed
    private boolean componentsChanged = true;

    //the map of the components
    private Component[][] componentsMap;

    //corresponds the order of the components to the order of the PowerLevel?
    private boolean isPowerLevelOrderCorrect = false;

    //did the power request change?
    private boolean isPowerRequestDifferent = true;

    //is a structure check necessary
    private boolean isStructureCheckNecessary = true;

    //the name
    private String name;

    @Override
    public String getName() {
        return name;
    }

    //global variables
    private final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables;

    //region properties
    //virtual property when a key is pressed
    public final NotifyProperty<String> keyDown = register(new NotifyProperty<>(KeyDownKey));
    //virtual property when key is released
    public final NotifyProperty<String> keyUp = register(new NotifyProperty<>(KeyUpKey));
    //wrapper for the angularRotation from Body
    public final FloatProperty angularVelocity = register(new FloatProperty(true, true, AngularVelocityKey));
    //wrapper for the velocity from Body
    public final FloatProperty velocity = register(new FloatProperty(true, true, VelocityKey));
    //focus from SpaceSimulation
    public final VirtualProperty<Boolean> cameraFocus = register(new VirtualProperty<>(true, CameraFocusKey) {
        @Override
        public void set(Boolean value) {
            super.set(value);
            if (value) getSpaceSimulation().setCameraSimulated(Ship.this);
        }

        @Override
        public Boolean get2() {
            return getSpaceSimulation().getCameraSimulated() == Ship.this;
        }
    });
    //endregion

    //construct Ship out of ShipDef (public constructor)
    public Ship(ShipDef def, SpaceSimulation spaceSimulation) {
        super(spaceSimulation, BodyDef.BodyType.DynamicBody, 1);
        name = def.getName();
        spaceSimulation.addExternalPropertyHandler(this);
        if (!def.getValidated()) throw new IllegalArgumentException("shipDef is not validated");
        //receives key inputs
        setReceivesKeyInput(true);
        //compile the script
        Compiler compiler = new Compiler(def.code, def);
        Program program = null;
        try {
            program = compiler.compile();
        } catch (CompileException e) {
            e.printStackTrace();
        }
        globalVariables = program.globalVariables;
        HashMap<String, MethodStatement> methods = new HashMap<>();
        for (MethodStatement statement : program.methods) {
            methods.put(statement.getDefinition().getName(), statement);
        }
        //init the externalProperties
        this.initProperties(def.properties.values(), methods);
        //init new list with all the components
        components = new ArrayList<>(def.componentDefs.size());
        //init the map
        componentsMap = new Component[ShipDef.MAX_SIZE][ShipDef.MAX_SIZE];
        for (ComponentDef comDef : def.componentDefs) {
            createComponent(comDef, methods);
        }
        //init center pos
        updateCenterPos();
    }

    //package-private constructor to construct Ship out of components (used to split up a ship)
    //pass other ship to copy important stuff (external method stuff etc.)
    Ship(Ship oldShip, List<Component> components) {
        super(oldShip.getSpaceSimulation(), BodyDef.BodyType.DynamicBody, 1);
        //check for new name
        String nameStart = oldShip.getName();
        while (getSpaceSimulation().containsExternalPropertyHandler(nameStart += "#")) ;
        name = nameStart;
        getSpaceSimulation().addExternalPropertyHandler(this);
        //receives key inputs
        setReceivesKeyInput(true);
        //set globalVariables
        this.globalVariables = oldShip.globalVariables;
        //init the externalProperties
        this.cloneProperties(oldShip.getProperties().values());
        //set the components
        this.components = new ArrayList<>(components.size());
        //init the map
        componentsMap = new Component[ShipDef.MAX_SIZE][ShipDef.MAX_SIZE];
        for (Component component : components) {
            addComponent(component);
            component.setShip(this);
        }
        //init center pos
        updateCenterPos();
        Body oldBody = oldShip.getBody();
        Body body = getBody();
        body.setTransform(oldBody.getPosition(), oldBody.getAngle());
        updateLinearVelocity(oldBody);
        body.setAngularVelocity(oldBody.getAngularVelocity());
    }

    private void createComponent(ComponentDef comDef, Map<String, MethodStatement> methods) {
        Component component = comDef.createComponent(this);
        component.initProperties(comDef.properties.values(), methods);
        addComponent(component);
    }

    /**
     * adds a Component to this ship
     * handles Actor.addActor, components, componentsMap
     *
     * @param component the Component to add
     */
    private void addComponent(Component component) {
        componentsChanged = true;
        ComponentDef def = component.getComponentDef();
        components.add(component);
        getSpaceSimulation().addExternalPropertyHandler(component);
        //add to map
        for (int _x = def.getX(); _x < (def.getX() + def.getRealWidth()); _x++) {
            for (int _y = def.getY(); _y < (def.getY() + def.getRealHeight()); _y++) {
                componentsMap[_x][_y] = component;
            }
        }
    }

    /**
     * removes a Component from this ship
     * handles Actor.removeActor, components, componentsMap
     *
     * @param component the Component to add
     */
    private void removeComponent(Component component) {
        componentsChanged = true;
        components.remove(component);
        component.removeFixtures();
        getSpaceSimulation().removeExternalPropertyHandler(component);
        //remove from map
        ComponentDef comDef = component.getComponentDef();
        for (int _x = comDef.getX(); _x < (comDef.getX() + comDef.getRealWidth()); _x++) {
            for (int _y = comDef.getY(); _y < (comDef.getY() + comDef.getRealHeight()); _y++) {
                componentsMap[_x][_y] = null;
            }
        }
        updateCenterPos();
    }

    /**
     * removes a component and performs a structure check if necessary
     *
     * @param component the Component to destroy
     */
    void destroyComponent(Component component) {
        removeComponent(component);
        isStructureCheckNecessary = true;
    }

    //checks if the ship structure is intact or constructs partial ships otherwise
    private void checkStructure() {
        if (components.size() > 0) {
            checkStructureRec(components.get(0));
            List<Component> otherComponents = components.stream().filter(com -> !com.structureHelper).collect(Collectors.toList());
            //remove other components
            otherComponents.forEach(this::removeComponent);
            //reset remaining
            components.forEach(component -> component.structureHelper = false);
            //create new ship
            if (otherComponents.size() > 0) getSpaceSimulation().addSimulated(new Ship(this, otherComponents));
            updateLinearVelocity(getBody());
        } else {
            System.out.println("this ship has no component?!");
        }
    }

    //checks the structure recursive (helper for checkStructure())
    private void checkStructureRec(Component component) {
        component.structureHelper = true;
        ComponentDef comDef = component.getComponentDef();
        //go around component
        //check left side
        if (comDef.getX() > 0) {
            //there is a left side
            for (int y = comDef.getY(); y < (comDef.getY() + comDef.getRealHeight()); y++) {
                Component nextComponent = componentsMap[comDef.getX() - 1][y];
                if (nextComponent != null && !nextComponent.structureHelper &&
                        nextComponent.attachComponentAtRaw(comDef.getX() - 1, y, Component.RIGHT_SIDE) &&
                        component.attachComponentAtRaw(comDef.getX(), y, Component.LEFT_SIDE))
                    checkStructureRec(nextComponent);
            }
        }
        //check bottom side
        if (comDef.getY() > 0) {
            //there is a top side
            for (int x = comDef.getX(); x < (comDef.getX() + comDef.getRealWidth()); x++) {
                Component nextComponent = componentsMap[x][comDef.getY() - 1];
                if (nextComponent != null && !nextComponent.structureHelper &&
                        nextComponent.attachComponentAtRaw(x, comDef.getY() - 1, Component.TOP_SIDE) &&
                        component.attachComponentAtRaw(x, comDef.getY(), Component.BOTTOM_SIDE))
                    checkStructureRec(nextComponent);
            }
        }
        //check right side
        if (comDef.getX() < (ShipDef.MAX_SIZE - 1)) {
            //there is a right side
            for (int y = comDef.getY(); y < (comDef.getY() + comDef.getRealHeight()); y++) {
                Component nextComponent = componentsMap[comDef.getX() + comDef.getRealWidth()][y];
                if (nextComponent != null && !nextComponent.structureHelper &&
                        nextComponent.attachComponentAtRaw(comDef.getX() + comDef.getRealWidth(), y, Component.LEFT_SIDE) &&
                        component.attachComponentAtRaw(comDef.getX() + comDef.getRealWidth() - 1, y, Component.RIGHT_SIDE))
                    checkStructureRec(nextComponent);
            }
        }
        //check top side
        if (comDef.getY() < (ShipDef.MAX_SIZE - 1)) {
            //there is a bottom side
            for (int x = comDef.getX(); x < (comDef.getX() + comDef.getRealWidth()); x++) {
                Component nextComponent = componentsMap[x][comDef.getY() + comDef.getRealHeight()];
                if (nextComponent != null && !nextComponent.structureHelper &&
                        nextComponent.attachComponentAtRaw(x, comDef.getY() + comDef.getRealHeight(), Component.BOTTOM_SIDE) &&
                        component.attachComponentAtRaw(x, comDef.getY() + comDef.getRealHeight() - 1, Component.TOP_SIDE))
                    checkStructureRec(nextComponent);
            }
        }
    }

    //region power system
    void invalidatePowerLevelOrder() {
        isPowerLevelOrderCorrect = false;
        invalidatePowerDelivery();
    }

    void invalidatePowerDelivery() {
        isPowerRequestDifferent = true;
    }

    //endregion

    //region key input

    @Override
    public boolean keyDown(int keycode) {
        keyDown.set(Input.Keys.toString(keycode));
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keyUp.set(Input.Keys.toString(keycode));
        return true;
    }

    @Override
    public void setCameraFocus(boolean cameraFocus) {
        System.out.println("set focus: " + name + ", " + cameraFocus);
        this.cameraFocus.set(cameraFocus);
    }

    //endregion

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

    //update the linear velocity, after some structure changes
    private void updateLinearVelocity(Body oldBody) {
        body.setLinearVelocity(oldBody.getLinearVelocityFromLocalPoint(body.getLocalCenter()));
    }

    @Override
    public void act(float time) {
        super.act(time);
        //check if list has changed
        if (componentsChanged) {
            iterationList = List.copyOf(components);
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
        //update properties
        angularVelocity.set(body.getAngularVelocity());
        //property changed
        for (ExternalProperty property : getProperties().values()) {
            property.startChangedHandler(getSpaceSimulation().getScriptingEngine(), globalVariables);
        }

        //I know this is hacky, but it's the best I have
        for (Component component : iterationList) {
            component.act(time);
            component.getProperties().values()
                    .forEach(property -> property.startChangedHandler(getSpaceSimulation().getScriptingEngine(), globalVariables));
        }
    }

    //sets powerReceived on all components
    private void updatePowerDistribution() {
        //update how much power each component
        //check how much power is available
        float availablePower = 0f;
        for (Component component : components) {
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
                    float fac = levelPowerRequest / availablePower;
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
        for (Component component : components) {
            //check if it consumes or delivers power
            if (component.powerRequested.get() < 0) {
                component.powerReceived.set(component.powerRequested.get() * fac);
            }
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        for (Component component : components) component.draw(batch);
    }
}
