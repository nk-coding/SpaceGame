package com.nkcoding.interpreter.compiler;

public class Operation implements Comparable<Operation> {
    private OperatorType type;

    private int exp1;

    private int exp2;

    public Operation(OperatorType type, int exp1, int exp2) {
        this.type = type;
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    public OperatorType getType() {
        return type;
    }

    public int getExp1() {
        return exp1;
    }

    public int getExp2() {
        return exp2;
    }

    @Override
    public int compareTo(Operation o) {
        int priority1 = CompilerHelper.getPriority(type);
        int priority2 = CompilerHelper.getPriority(o.getType());
        if (priority1 > priority2) return 1;
        else if (priority1 < priority2) return -1;
        else {
            return Integer.compare(exp1, o.getExp1());
        }
    }
}
