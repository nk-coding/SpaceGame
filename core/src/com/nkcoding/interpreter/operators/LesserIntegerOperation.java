package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.Expression;
import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.compiler.DataTypes;

public class LesserIntegerOperation extends BinaryExpressionBase<Integer> implements Expression<Boolean> {
    @Override
    public Boolean getResult(Stack stack) {
        //System.out.println("lesser integer operation get result");
        return firstExpression.getResult(stack) < secondExpression.getResult(stack);
    }

    @Override
    public String getType() {
        return DataTypes.Boolean;
    }
}
