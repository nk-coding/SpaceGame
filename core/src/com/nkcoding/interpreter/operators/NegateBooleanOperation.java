package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataTypes;

public class NegateBooleanOperation extends UnaryOperation<Boolean> {
    @Override
    public Boolean getResult(Stack stack) {
        return !firstExpression.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Boolean;
    }
}
