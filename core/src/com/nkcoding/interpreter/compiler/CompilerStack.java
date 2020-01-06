package com.nkcoding.interpreter.compiler;

import com.nkcoding.interpreter.ConcurrentStackItem;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

//the CompilerStack exists because it is less run-time optimated than the real Stack
//this mainly means that there are less generics
public class CompilerStack {
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
    public void addToStack(String name, DataType type) {
        stack.add(new CompilerStackItem(name, type, stackLevel));
    }

    public boolean exists(String name) {
        for (int x = stack.size() - 1; x >= 0; x--) {
            if (stack.get(x).name.equals(name)) return true;
        }
        return false;
    }

    //should call exist before, otherwise it may throws an IllegalArgumentException
    public DataType getType(String name) {
        for (int x = stack.size() - 1; x >= 0; x--) {
            if (stack.get(x).name.equals(name)) return stack.get(x).type;
        }
        throw new IllegalArgumentException(name + " is not registered as a variable");
    }

    public ConcurrentHashMap<String, ConcurrentStackItem> getGlobalVariables() {
        ConcurrentHashMap<String, ConcurrentStackItem> variables = new ConcurrentHashMap<>();

        for (CompilerStackItem item : stack) {
            ConcurrentStackItem v;
            switch (item.type.name) {
                case DataType.BOOLEAN_KW:
                    v = new ConcurrentStackItem<>(DataType.BOOLEAN, false);
                    break;
                case DataType.FLOAT_KW:
                    v = new ConcurrentStackItem<>(DataType.FLOAT, 0f);
                    break;
                case DataType.INTEGER_KW:
                    v = new ConcurrentStackItem<>(DataType.INTEGER, 0);
                    break;
                case DataType.STRING_KW:
                    v = new ConcurrentStackItem<>(DataType.STRING, "");
                    break;
                default:
                    throw new UnsupportedOperationException("cannot handle DataType + " + item.type);
            }
            v.setName(item.name);
            variables.put(item.name, v);
        }

        return variables;
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

    //subclass that is the item
    //this subclass will not be revealed to the compiler because it is unnecessary
    private static class CompilerStackItem {
        //the name of the variable
        String name;

        //the type of the variable
        DataType type;

        //the stack level
        int stackLevel;

        //the position where this was registered
        //this is necessary to throw exceptions correctly
        //not implemented yet

        CompilerStackItem(String name, DataType type, int stackLevel) {
            this.name = name;
            this.type = type;
            this.stackLevel = stackLevel;
        }

        @Override
        public String toString() {
            return type + " " + name;
        }
    }
}
