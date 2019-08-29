package com.nkcoding.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Stack {
    //the list where all the items on the Stack are stored in
    private ArrayList<StackItem> stackItems;

    //the stack level
    private int stackLevel = 0;

    //the scriptingEngine is mainly used for the requestExternMethod
    private ScriptingEngine scriptingEngine;

    //sets the debug mode
    private boolean debugMode = false;

    //constructor
    //wants a initial capacity for the Stack
    public Stack(int initialCapacity, ScriptingEngine scriptingEngine){
        stackItems = new ArrayList<StackItem>(initialCapacity);
        this.scriptingEngine = scriptingEngine;
    }

    public <T> void addToStack(String name, T value, String type){
        //System.out.println("stack: add to stack: " + name);
        StackItem<T> item = new StackItem<T>(type);
        item.setName(name);
        item.setValue(value);
        item.setStackLevel(stackLevel);
        stackItems.add(item);
    }

    public StackItem getFromStack(String name){
        //System.out.println("stack: getFromStack, " + stackItems.size() + " items in stack");
        for (int x = stackItems.size() - 1; x >= 0; x--){
            //System.out.println("stack: found item with name " + stackItems.get(x).getName());
            if (stackItems.get(x).getName().equals(name)){
                //System.out.println("stack: found in stack");
                return stackItems.get(x);
            }
        }
        return null;
    }


    public void beginStackLevel(){
        stackLevel++;
        if (debugMode) System.out.println("begin stack level " + stackLevel);
    }

    public void clearStackLevel(){
        if (debugMode) {
            System.out.println("clear stack level " + stackLevel);
            List<StackItem> itemsToRemove = stackItems.stream().filter(item -> item.getStackLevel() == stackLevel).collect(Collectors.toList());
            for (StackItem item : itemsToRemove) {
                System.out.println(item.toString());
            }
        }
        stackItems.removeIf(item -> item.getStackLevel() == stackLevel);
        stackLevel--;
    }

    //should add this to the list on the main thread
    public void requestExternMethod(ExternMethodFuture future){
        scriptingEngine.getFutureQueue().add(future);
    }
}
