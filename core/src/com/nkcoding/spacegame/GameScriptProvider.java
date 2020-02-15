package com.nkcoding.spacegame;

import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ExternalMethodHandler;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.spacegame.simulation.SimulatedType;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertySpecification;
import com.nkcoding.util.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class GameScriptProvider implements ExternalMethodHandler {
    //Queue of all ExternalMethodFutures, they will be executed on the main tread
    //therefore, this queue must be concurrent
    protected final ConcurrentLinkedQueue<ExternalMethodFuture> futureQueue = new ConcurrentLinkedQueue<>();
    // map with all objects that can receive futures
    protected final ConcurrentMap<String, ExternalMethodHandler> externalMethodHandlers = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, Tuple<Boolean, Consumer<ExternalMethodFuture>>> externalMethodsMap = new ConcurrentHashMap<>();
    //Methods which saves all ExternalMethodDefinitions
    private Set<MethodDefinition> externalMethodDefinitions = new LinkedHashSet<>();

    public GameScriptProvider() {
        //init Simulated
        for (SimulatedType type : SimulatedType.values()) {
            type.typeInitializer.accept(this);
        }
    }

    public ExternalMethodHandler getExternalMethodHandler(String key) {
        return externalMethodHandlers.get(key);
    }

    public void addExternalMethod(MethodDefinition definition, boolean concurrentSupport, Consumer<ExternalMethodFuture> methodHandler) {
        externalMethodsMap.put(definition.getName(), new Tuple<>(concurrentSupport, methodHandler));
        externalMethodDefinitions.add(definition);
    }

    @Override
    public void handleExternalMethod(ExternalMethodFuture future) {
        //stub
    }

    public Compiler createCompiler(String code) {
        //create the external method statements for the components
        HashMap<String, ExternalPropertySpecification> externalPropertyDatas = new HashMap<>();
//        for (ComponentType com : ComponentType.values()) {
//            for (ExternalPropertySpecification data : com.propertySpecifications) {
//                if (!externalPropertyDatas.containsKey(data.name)) {
//                    externalPropertyDatas.put(data.name, data);
//                }
//            }
//        }

//        ArrayList<MethodDefinition> methodDefinitions = new ArrayList<>();
//        for (ExternalPropertySpecification data : externalPropertyDatas.values()) {
//            data.addExternalMethodDefs(methodDefinitions);
//        }
        String[] lines = code.split("\\r?\\n");

        return new Compiler(lines, externalMethodDefinitions);
    }
}
