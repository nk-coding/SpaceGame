package com.nkcoding.interpreter.compiler;

public class ProgramPosition {
    private int line;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    private int column;

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    //constructor
    public ProgramPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(line);
        sb.append(", ");
        sb.append(column);
        sb.append(')');
        return sb.toString();
    }

    public ProgramPosition getClone() {
        return new ProgramPosition(line, column);
    }

}
