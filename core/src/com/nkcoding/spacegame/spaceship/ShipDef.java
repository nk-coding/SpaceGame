package com.nkcoding.spacegame.spaceship;

import java.util.ArrayList;

public class ShipDef {
    public class ShipDesignerHelper{
        private Component.ComponentDef[][] componentsMap = new Component.ComponentDef[MAX_SIZE][MAX_SIZE];

        //locate component if there is one
        public Component.ComponentDef GetComponent(int x, int y){
            return componentsMap[x][y];
        }

        //Add a component to componentsMap and componentsDefs
        //this requires that TryMoveComponent was called immediately before
        //otherwise this might lead to unexpected behaviour
        public void AddComponent(Component.ComponentDef componentDef, int x, int y, int rotation){
            componentDefs.add(componentDef);
            //update component
            componentDef.setRotation(rotation);
            componentDef.setX(x);
            componentDef.setY(y);
            //add new references
            for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); x++){
                for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); y++){
                    componentsMap[_x][_y] = componentDef;
                }
            }
        }

        //is it possible to move a component to a specific position
        public boolean TryMoveComponent(Component.ComponentDef componentDef, int x, int y, int rotation){
            //calculate resulting width and height
            int width = (rotation % 2 == 0) ? componentDef.getWidth() : componentDef.getHeight();
            int height = (rotation % 2 == 0) ? componentDef.getHeight() : componentDef.getWidth();
            boolean result = true;
            for (int _x = x; _x < (x + width); _x++){
                for (int _y = y; _y < (y + height); _y++){
                    //check if it is in range
                    //check if there is no component or (the not moved) same
                    result = result && (_x < MAX_SIZE && _y < MAX_SIZE && (componentsMap[_x][_y] == null || componentsMap[_x][_y] == componentDef));
                }
            }
            return result;
        }

        //actually move component
        //this requires that TryMoveComponent was called immediately before
        //otherwise this might lead to unexpected behaviour
        public void MoveComponent(Component.ComponentDef componentDef, int x, int y, int rotation){
            //delete the old references
            for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++){
                for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++){
                    componentsMap[_x][_y] = null;
                }
            }
            //update component
            componentDef.setRotation(rotation);
            componentDef.setX(x);
            componentDef.setY(y);
            //add new references
            for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++){
                for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++){
                    componentsMap[_x][_y] = componentDef;
                }
            }
        }
    }

    //the max width / height of a ship
    public static final int MAX_SIZE = 25;

    //the size of one unit in box2d
    public static final float UNIT_SIZE = 0.1f;

    final ArrayList<Component.ComponentDef> componentDefs = new ArrayList<>();

}
