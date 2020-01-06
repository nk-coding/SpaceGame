package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Stack {
    //map with all globalVariables
    final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables;
    //the list where all the items on the Stack are stored in
    private ArrayList<StackItem> stackItems;
    //the stack level
    private int stackLevel = 0;
    //the scriptingEngine is mainly used for the requestExternalMethod
    private ScriptingEngine scriptingEngine;
    //sets the debug mode
    private boolean debugMode = false;

    //constructor
    //wants a initial capacity for the Stack
    public Stack(int initialCapacity, ScriptingEngine scriptingEngine,
                 final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables) {
        stackItems = new ArrayList<StackItem>(initialCapacity);
        this.scriptingEngine = scriptingEngine;
        this.globalVariables = globalVariables;
    }

    public <T> void addToStack(String name, T value, DataType type) {
        //System.out.println("stack: add to stack: " + name);
        StackItem<T> item = new StackItem<T>(type);
        item.setName(name);
        item.setValue(value);
        item.setStackLevel(stackLevel);
        stackItems.add(item);
    }

    public StackItem getFromStack(String name) {
        for (int x = stackItems.size() - 1; x >= 0; x--) {
            if (stackItems.get(x).getName().equals(name)) {
                return stackItems.get(x);
            }
        }
        return globalVariables.get(name);
    }


    public void beginStackLevel() {
        stackLevel++;
        if (debugMode) System.out.println("begin stack level " + stackLevel);
    }

    public void clearStackLevel() {
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
    public void requestExternalMethod(ExternalMethodFuture future) {
        scriptingEngine.getFutureQueue().add(future);
    }
}
