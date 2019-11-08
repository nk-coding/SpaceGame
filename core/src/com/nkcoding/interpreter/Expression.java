package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public interface Expression<T> {
    T getResult(Stack stack);

    DataType getType();
}
