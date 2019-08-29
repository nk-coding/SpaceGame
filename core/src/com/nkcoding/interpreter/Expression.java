package com.nkcoding.interpreter;

public interface Expression<T> {
    T getResult(Stack stack);

    String getType();
}
