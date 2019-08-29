package com.nkcoding.spacegame;

import com.badlogic.gdx.physics.box2d.Body;

//test implementation
public class  TestImp extends Component {

    protected TestImp(Component.ComponentDef componentDef, Ship ship) {
        super(ComponentType.TestType, componentDef, ship);
    }

    public static class ComponentDef extends Component.ComponentDef{
        public ComponentDef(){
            super(ComponentType.TestType);
        }

        @Override
        public String toString(){
            return "this is a string form a sub component";
        }

        @Override
        public ComponentType getType() {
            return null;
        }
    }


    public static Component.ComponentDef createDefinition() {
        return new ComponentDef();
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
