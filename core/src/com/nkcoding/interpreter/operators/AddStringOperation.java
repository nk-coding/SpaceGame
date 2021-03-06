package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataType;

public class AddStringOperation extends BinaryOperation<String> {
    @Override
    public String getResult(Stack stack) {
        return firstExpression.getResult(stack) + secondExpression.getResult(stack);
    }

    @Override
    public DataType getType() {
        return DataType.STRING;
    }
}
