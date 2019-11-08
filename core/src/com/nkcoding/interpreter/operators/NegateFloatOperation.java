package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataType;

public class NegateFloatOperation extends UnaryOperation<Float> {
    @Override
    public Float getResult(Stack stack) {
        return -firstExpression.getResult(stack);
    }

    @Override
    public DataType getType() {
        return DataType.FLOAT;
    }
}
