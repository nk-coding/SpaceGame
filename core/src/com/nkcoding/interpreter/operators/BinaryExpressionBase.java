package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Expression;

public abstract class BinaryExpressionBase<T> {

    protected Expression<T> firstExpression;
    protected Expression<T> secondExpression;

    public void setFirstExpression(Expression<T> firstExpression) {
        this.firstExpression = firstExpression;
    }

    public void setSecondExpression(Expression<T> secondExpression) {
        this.secondExpression = secondExpression;
    }
}
