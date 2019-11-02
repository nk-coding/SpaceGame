package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Expression;

public abstract class BinaryOperation<T> extends UnaryOperation<T> {
    protected Expression<T> secondExpression;

    public void setSecondExpression(Expression<T> secondExpression) {
        this.secondExpression = secondExpression;
    }
}
