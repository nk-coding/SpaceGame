package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataTypes;

public class IntegerToDoubleCast implements Expression<Double> {
    private Expression<Integer> toCast;

    public IntegerToDoubleCast(Expression<Integer> toCast) {
        this.toCast = toCast;
    }

    @Override
    public Double getResult(Stack stack) {
        return (double)toCast.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Double;
    }
}
