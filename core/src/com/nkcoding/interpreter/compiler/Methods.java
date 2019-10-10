package com.nkcoding.interpreter.compiler;

import com.nkcoding.interpreter.MethodStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

//contains predefined methods and List of extern methods
public class Methods {
    //map of all predefined methods
    private HashMap<MethodDefinition, Function> predefinedMethods = new HashMap<>();

    //list of all extern methods
    private ArrayList<MethodDefinition> externMethods = new ArrayList<>();

    //array of all normal methods
    private MethodStatement[] normalMethods = {};

    public void setNormalMethods(MethodStatement[] normalMethods) {
        this.normalMethods = normalMethods;
    }

    public MethodStatement[] getNormalMethods() {
        return normalMethods;
    }

    //set the predefined methods in the constructor
    public Methods() {
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "println", DataTypes.Void, new TypeNamePair("str", DataTypes.String)),
                (Function<Object[], Void>) obj ->
                {
                    System.out.println((String)obj[0]);
                    return null;
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "intToStr", DataTypes.String, new TypeNamePair("i", DataTypes.Integer)),
                (Function<Object[], String>) obj ->
                {
                    return String.valueOf((int)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "floatToStr", DataTypes.String, new TypeNamePair("i", DataTypes.Float)),
                (Function<Object[], String>) obj ->
                {
                    return String.valueOf((float)obj[0]);
                });
        predefinedMethods.put(new MethodDefinition(MethodType.Predefined, "boolToStr", DataTypes.String, new TypeNamePair("i", DataTypes.Boolean)),
                (Function<Object[], String>) obj ->
                {
                    return String.valueOf((boolean)obj[0]);
                });

    }

    //returns the corresponding method definition or null if it does not exist yet
    public MethodDefinition methodExists(String name){
        MethodDefinition normalMethod = getNormalMethod(name);
        if (normalMethod != null) return normalMethod;
        else{
            Optional<MethodDefinition> externOpt = externMethods.stream().filter(def -> def.getName().equals(name)).findFirst();
            //check if it was found
            if (externOpt.isPresent()) {
                return externOpt.get();
            }
            else {
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
    public MethodStatement getNormalMethod(MethodDefinition def){
        for (MethodStatement normalMethod : normalMethods){
            if (normalMethod.getDefinition() == def) return normalMethod;
        }
        return null;
    }

    //add an extern method
    public void addExternMethod(MethodDefinition definition) {
        externMethods.add(definition);
    }

    //add an array of extern methods
    public void addExternMethods(MethodDefinition[] definitions) {
        if (definitions != null) externMethods.addAll(Arrays.asList(definitions));
    }

    //helper for normal methods
    private MethodDefinition getNormalMethod(String name) {
        for (MethodStatement normalMethod : normalMethods) {
            if (normalMethod.getDefinition().getName().equals(name)) return normalMethod.getDefinition();
        }
        return null;
    }
}
