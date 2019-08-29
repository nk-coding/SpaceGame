package com.nkcoding.interpreter.compiler;

public class MethodDefinition {

    private TypeNamePair[] parameters;

    public void setParameters(TypeNamePair[] parameters){
        this.parameters = parameters;
    }

    public TypeNamePair[] getParameters() {
        return parameters;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String returnType;

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnType() {
        return returnType;
    }

    //the type of the method
    private final MethodType methodType;

    public MethodType getMethodType() {
        return methodType;
    }

    //could be used if necessary
    public MethodDefinition(MethodType methodType) {
        this.methodType = methodType;
    }

    //the complete constructor for the ones which use it
    public MethodDefinition(MethodType methodType, String name, String returnType, TypeNamePair... parameters) {
        this(methodType);
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType);
        sb.append(' ');
        sb.append(name);
        sb.append('(');
        for (int x = 0; x < parameters.length; x++) {
            if (x > 0) sb.append(", ");
            sb.append(parameters[x].getDataType());
            sb.append(' ');
            sb.append(parameters[x].getName());
        }
        sb.append(')');
        return sb.toString();
    }

}
