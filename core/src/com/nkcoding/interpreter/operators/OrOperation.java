package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataType;

public class OrOperation extends BinaryOperation<Boolean> {
    @Override
    public Boolean getResult(Stack stack) {
        return firstExpression.getResult(stack) || secondExpression.getResult(stack);
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }
}
