package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataTypes;

public class AndOperation extends BinaryOperation<Boolean> {
    @Override
    public Boolean getResult(Stack stack) {
        return firstExpression.getResult(stack) && secondExpression.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Boolean;
    }
}
