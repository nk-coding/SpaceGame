package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataType;

public class NegateBooleanOperation extends UnaryOperation<Boolean> {
    @Override
    public Boolean getResult(Stack stack) {
        return !firstExpression.getResult(stack);
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }
}
