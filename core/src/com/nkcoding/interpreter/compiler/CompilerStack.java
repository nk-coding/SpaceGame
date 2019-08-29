package com.nkcoding.interpreter.compiler;

import java.util.ArrayList;

//the CompilerStack exists because it is less run-time optimated than the real Stack
//this mainly means that there are less generics
public class CompilerStack {
    //subclass that is the item
    //this subclass will not be revealed to the compiler because it is unnecessary
    private static class CompilerStackItem {
        //the name of the variable
        String name;

        //the type of the variable
        String type;

        //the stack level
        int stackLevel;

        //the position where this was registered
        //this is necessary to throw exceptions correctly
        //not implemented yet

        CompilerStackItem(String name, String type, int stackLevel) {
            this.name = name;
            this.type = type;
            this.stackLevel = stackLevel;
        }

        @Override
        public String toString() {
            return type + " " + name;
        }
    }

    //list of all CompilerStackItems
    private ArrayList<CompilerStackItem> stack = new ArrayList<>();

    private int stackLevel = 0;

    public void beginStackLevel() {
        stackLevel++;
    }

    public void clearStackLevel() {
        stack.removeIf(item -> item.stackLevel == stackLevel);
        stackLevel--;
    }

    //should chack exists before
    public void addToStack(String name, String type) {
        stack.add(new CompilerStackItem(name, type, stackLevel));
    }

    public boolean exists(String name) {
        for(int x = stack.size() - 1; x >= 0; x--) {
            if (stack.get(x).name.equals(name)) return  true;
        }
        return false;
    }

    //should call exist before, otherwise it may throws an IllegalArgumentException
    public String getType(String name) {
        for(int x = stack.size() - 1; x >= 0; x--) {
            if (stack.get(x).name.equals(name)) return  stack.get(x).type;
        }
        throw new IllegalArgumentException(name + " is not registered as a variable");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("currently on compile stack: \n");
        for (int x = stack.size() - 1; x >= 0; x--) {
            sb.append(stack.get(x).toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}
