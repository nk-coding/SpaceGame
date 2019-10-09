package com.nkcoding.interpreter.compiler;

import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.MethodStatement;

import java.util.concurrent.ConcurrentHashMap;

public class Program {
    /**
     * array with all declared methods
     */
    public final MethodStatement[] methods;

    /**
     * map with all global variables (inclusive default values)
     */
    public final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables;

    /**
     * default constructor
     */
    public Program(MethodStatement[] methods, ConcurrentHashMap<String, ConcurrentStackItem> globalVariables) {
        this.methods = methods;
        this.globalVariables = globalVariables;
    }

}
