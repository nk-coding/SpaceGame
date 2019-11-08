package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

import java.util.concurrent.CompletableFuture;

public class ExternalMethodFuture extends CompletableFuture<Object> {
    //the name of the method
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private DataType type;

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    //the parameters for the method
    private Object[] parameters;

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("(");
        for (int x = 0; x < parameters.length; x++) {
            sb.append(parameters[x]);
            if (x < (parameters.length - 1)) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
