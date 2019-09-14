package com.nkcoding.spacegame.spaceship;

//test implementation
public class  TestImp extends Component {

    public TestImp(ComponentDef componentDef, Ship ship) {
        super(ComponentType.TestType, componentDef, ship);
    }


    @Override
    public void addFixtures() {
        //TODO implementation
    }


    @Override
    public ComponentType getType() {
        return null;
    }
}
