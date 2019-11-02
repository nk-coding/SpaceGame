package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Expression;

public abstract class UnaryOperation<T> implements Expression<T> {
    protected Expression<T> firstExpression;

    public void setFirstExpression(Expression<T> firstExpression) {
        this.firstExpression = firstExpression;
    }
}
