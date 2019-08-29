package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.operators.AssignmentOperation;

public class ReturnValueStatement implements Statement {
    private AssignmentOperation assignment;

    public ReturnValueStatement(MethodDefinition definition, Expression value){
        if (!definition.getReturnType().equals(DataTypes.Void)) {
            assignment = new AssignmentOperation(definition.getName() + "$result", definition.getReturnType());
            assignment.setFirstExpression(value);
        }
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        if (assignment != null) assignment.run(stack);
        throw new ReturnException();
    }
}
