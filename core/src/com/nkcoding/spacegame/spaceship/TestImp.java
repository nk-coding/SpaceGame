package com.nkcoding.spacegame.spaceship;

//test implementation
public class  TestImp extends Component {

    public static final String ModifiableKey = "Modifiable";

    public TestImp(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
    }


    @Override
    public void addFixtures() {
        super.addFixtures();
        //TODO implementation
    }


    @Override
    public ComponentType getType() {
        return null;
    }
}
