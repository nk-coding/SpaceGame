package com.nkcoding.interpreter;

public interface Statement {
    void run(Stack stack) throws ReturnException;
}
