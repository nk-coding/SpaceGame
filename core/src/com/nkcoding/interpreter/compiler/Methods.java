package com.nkcoding.interpreter.compiler;

import com.nkcoding.interpreter.MethodStatement;

import java.util.*;
import java.util.function.Function;

//contains predefined methods and List of extern methods
public class Methods {
    //map of all predefined methods
    private HashMap<MethodDefinition, Function> predefinedMethods = new HashMap<>();

    //list of all external methods
    private ArrayList<MethodDefinition> externMethods = new ArrayList<>();

    //array of all normal methods
    private MethodStatement[] normalMethods = {};

    //set the predefined methods in the constructor
    public Methods() {
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "println", DataType.VOID, new TypeNamePair("str", DataType.STRING)),
                (Function<Object[], Void>) obj ->
                {
                    System.out.println((String) obj[0]);
                    return null;
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "currentTimeMillis", DataType.VOID),
                (Function<Object[], Void>) obj ->
                {
                    System.out.println(System.currentTimeMillis());
                    return null;
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "intToStr", DataType.STRING, new TypeNamePair("i", DataType.INTEGER)),
                (Function<Object[], String>) obj ->
                {
                    return String.valueOf((int) obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "floatToStr", DataType.STRING, new TypeNamePair("i", DataType.FLOAT)),
                (Function<Object[], String>) obj ->
                {
                    return String.valueOf((float) obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "boolToStr", DataType.STRING, new TypeNamePair("i", DataType.BOOLEAN)),
                (Function<Object[], String>) obj ->
                {
                    return String.valueOf((boolean) obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "sleep", DataType.VOID, new TypeNamePair("i", DataType.INTEGER)),
                (Function<Object[], String>) obj -> {
                    try {
                        Thread.sleep((int)obj[0]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                });

        //math functions
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "sin", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.sin((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "asin", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.asin((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "cos", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.cos((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "acos", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.acos((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "tan", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.tan((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "atan", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.atan((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "atan2", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT), new TypeNamePair("y", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.atan2((float)obj[0], (float)obj[1]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "random", DataType.FLOAT),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.random();
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "randomInt", DataType.FLOAT, new TypeNamePair("min", DataType.INTEGER), new TypeNamePair("max", DataType.INTEGER)),
                (Function<Object[], Integer>) obj ->
                {
                    int min = (int)obj[0];
                    int max = (int)obj[1];
                    return (int)(Math.random() * (max - min) + min);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "degToRad", DataType.FLOAT, new TypeNamePair("deg", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.toRadians((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "radToDeg", DataType.FLOAT, new TypeNamePair("rad", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.toDegrees((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "sqrt", DataType.FLOAT, new TypeNamePair("x", DataType.FLOAT)),
                (Function<Object[], Float>) obj ->
                {
                    return (float)Math.sqrt((float)obj[0]);
                });

    }

    public MethodStatement[] getNormalMethods() {
        return normalMethods;
    }

    public void setNormalMethods(MethodStatement[] normalMethods) {
        this.normalMethods = normalMethods;
    }

    //returns the corresponding method definition or null if it does not exist yet
    public MethodDefinition methodExists(String name) {
        MethodDefinition normalMethod = getNormalMethod(name);
        if (normalMethod != null) return normalMethod;
        else {
            Optional<MethodDefinition> externOpt = externMethods.stream().filter(def -> def.getName().equals(name)).findFirst();
            //check if it was found
            if (externOpt.isPresent()) {
                return externOpt.get();
            } else {
                Optional<MethodDefinition> predefOpt = predefinedMethods.keySet().stream().filter(def -> def.getName().equals(name)).findFirst();
                //check if it was found
                return predefOpt.orElse(null);
            }
        }
    }


    //gets the action for a predefined method
    //this throws an exception if thy type does not fit or the method does not exist!!!!
    public <T> Function<Object[], T> getPredefinedMethod(MethodDefinition def) {
        return (Function<Object[], T>) predefinedMethods.get(def);
    }

    //gets the statement for a normal method
    public MethodStatement getNormalMethod(MethodDefinition def) {
        for (MethodStatement normalMethod : normalMethods) {
            if (normalMethod.getDefinition() == def) return normalMethod;
        }
        return null;
    }

    //add an external method
    public void addExternalMethod(MethodDefinition definition) {
        externMethods.add(definition);
    }

    //add an array of external methods
    public void addExternalMethods(Collection<MethodDefinition> definitions) {
        if (definitions != null) externMethods.addAll(definitions);
    }

    //helper for normal methods
    private MethodDefinition getNormalMethod(String name) {
        for (MethodStatement normalMethod : normalMethods) {
            if (normalMethod.getDefinition().getName().equals(name)) return normalMethod.getDefinition();
        }
        return null;
    }
}
