package com.nkcoding.interpreter;

import java.util.concurrent.CompletableFuture;

public class ExternMethodFuture extends CompletableFuture<Object> {
    //the parameters for the method
    private Object[] parameters;

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters){
        this.parameters = parameters;
    }
}
