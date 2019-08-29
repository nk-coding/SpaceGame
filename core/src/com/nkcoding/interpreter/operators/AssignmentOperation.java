package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.ReturnException;
import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.StackItem;
import com.nkcoding.interpreter.Statement;

//technically a BinaryOperator, but it is implemented via UnaryOperator because the first is always a StackItem
public class AssignmentOperation<T> extends UnaryOperation<T> implements Statement {
    private String name;

    private String type;

    public AssignmentOperation(String name, String type){
        this.name = name;
        this.type = type;
    }



    @Override
    public T getResult(Stack stack) {
        T res = firstExpression.getResult(stack);
        ((StackItem<T>) stack.getFromStack(name)).setValue(res);
        return res;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }


    @Override
    public String getType() {
        return type;
    }
}
