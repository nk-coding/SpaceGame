package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class CreateListExpression implements Expression<ListObject> {

    private DataType type;

    private Expression[] initExpressions;

    public CreateListExpression(Expression[] initExpressions) {
        this.initExpressions = initExpressions;
        //generate type
        type = DataType.fromExpressions(initExpressions);
    }

    @Override
    public ListObject getResult(Stack stack) {
        ListObject list = new ListObject(initExpressions.length);
        for (int x = 0; x < initExpressions.length; x++) {
            StackItem item = new StackItem(type.listTypes[x].getType());
            item.setValue(initExpressions[x].getResult(stack));
            list.items[x] = item;
        }
        return list;
    }

    @Override
    public DataType getType() {
        return type;
    }
}
