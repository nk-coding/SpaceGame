package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.*;
import com.nkcoding.interpreter.compiler.DataType;

//technically a BinaryOperator, but it is implemented via UnaryOperator because the first is always a StackItem
public class AssignmentOperation<T> extends UnaryOperation<T> implements Statement {
    private GetStackItem getItem;

    private DataType type;

    public AssignmentOperation(GetStackItem getItem, DataType type) {
        this.getItem = getItem;
        this.type = type;
    }

    public AssignmentOperation(GetStackItem getItem, DataType type, Expression<T> firstExpression) {
        this(getItem, type);
        setFirstExpression(firstExpression);
    }


    @Override
    public T getResult(Stack stack) {
        T res = firstExpression.getResult(stack);
        getItem.getStackItem(stack).setValue(res);
        return res;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }


    @Override
    public DataType getType() {
        return type;
    }
}
