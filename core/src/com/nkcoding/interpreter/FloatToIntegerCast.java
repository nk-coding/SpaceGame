package com.nkcoding.interpreter;

public class FloatToIntegerCast implements Expression<Integer> {
    private Expression<Float> toCast;

    public FloatToIntegerCast(Expression<Float> toCast){
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
