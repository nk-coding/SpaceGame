package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataTypes;

public class NegateDoubleOperation extends UnaryOperation<Double> {
    @Override
    public Double getResult(Stack stack){
        return -firstExpression.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Double;
    }
}
