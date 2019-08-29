package com.nkcoding.interpreter;

public class DoubleToIntegerCast implements Expression<Integer> {
    private Expression<Double> toCast;

    public DoubleToIntegerCast(Expression<Double> toCast){
        this.toCast = toCast;
    }

    @Override
    public Integer getResult(Stack stack) {
        return (int)Math.round(toCast.getResult(stack));
    }

    @Override
    public String getType() {
        return null;
    }
}
