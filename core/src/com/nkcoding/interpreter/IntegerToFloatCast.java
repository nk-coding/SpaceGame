package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataTypes;

public class IntegerToFloatCast implements Expression<Float> {
    private Expression<Integer> toCast;

    public IntegerToFloatCast(Expression<Integer> toCast) {
        this.toCast = toCast;
    }

    @Override
    public Float getResult(Stack stack) {
        return (float)toCast.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Float;
    }
}
