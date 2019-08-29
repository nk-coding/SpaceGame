package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Expression;

public abstract class BinaryExpressionBase<T> {

    protected Expression<T> firstExpression;

    public void setFirstExpression(Expression<T> firstExpression){
        this.firstExpression = firstExpression;
    }

    protected Expression<T> secondExpression;

    public void setSecondExpression(Expression<T> secondExpression){
        this.secondExpression = secondExpression;
    }
}
