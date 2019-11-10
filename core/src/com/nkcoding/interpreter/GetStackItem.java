package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public interface GetStackItem {
    StackItem getStackItem(Stack stack);

    DataType getType();
}
