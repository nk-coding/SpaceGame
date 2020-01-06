package com.nkcoding.interpreter.compiler;

public class MethodDefinition {

    //the type of the method
    private final MethodType methodType;
    private TypeNamePair[] parameters;
    private String name;
    private DataType returnType;

    //could be used if necessary
    public MethodDefinition(MethodType methodType) {
        this.methodType = methodType;
    }

    //the complete constructor for the ones which use it
    public MethodDefinition(MethodType methodType, String name, DataType returnType, TypeNamePair... parameters) {
        this(methodType);
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public TypeNamePair[] getParameters() {
        return parameters;
    }

    public void setParameters(TypeNamePair[] parameters) {
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public void setReturnType(DataType returnType) {
        this.returnType = returnType;
    }

    public MethodType getMethodType() {
        return methodType;
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
            sb.append(parameters[x].getType());
            sb.append(' ');
            sb.append(parameters[x].getName());
        }
        sb.append(')');
        return sb.toString();
    }

}
