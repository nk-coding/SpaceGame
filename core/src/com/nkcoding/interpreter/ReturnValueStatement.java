package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.operators.AssignmentOperation;

public class ReturnValueStatement implements Statement {
    private AssignmentOperation assignment;

    public ReturnValueStatement(MethodDefinition definition, Expression value) {
        if (!definition.getReturnType().equals(DataType.VOID)) {
            assignment = new AssignmentOperation(new GetVariableItemGet(definition.getName() + "$result",
                    definition.getReturnType()), definition.getReturnType(), value);
        }
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        if (assignment != null) assignment.run(stack);
        throw new ReturnException();
    }
}
