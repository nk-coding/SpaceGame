package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataType;

public class ModIntegerOperation extends BinaryOperation<Integer> {
    @Override
    public Integer getResult(Stack stack) {
        return firstExpression.getResult(stack) % secondExpression.getResult(stack);
    }

    @Override
    public DataType getType() {
        return DataType.INTEGER;
    }
}
