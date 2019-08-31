package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataTypes;

public class NegateFloatOperation extends UnaryOperation<Float> {
    @Override
    public Float getResult(Stack stack){
        return -firstExpression.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Float;
    }
}
