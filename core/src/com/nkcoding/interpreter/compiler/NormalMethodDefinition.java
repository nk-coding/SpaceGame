package com.nkcoding.interpreter.compiler;

public class NormalMethodDefinition extends MethodDefinition {

    private int line;

    public NormalMethodDefinition(int line) {
        super(MethodType.Normal);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
