package com.nkcoding.interpreter;

public abstract class StackStatement implements Statement {

    @Override
    public void run(Stack stack) throws ReturnException {
        stack.beginStackLevel();
        try {
            runOverride(stack);
        } finally {
            stack.clearStackLevel();
        }
    }

    abstract void runOverride(Stack stack) throws ReturnException;
}
