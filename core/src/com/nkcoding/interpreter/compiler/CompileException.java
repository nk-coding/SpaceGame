package com.nkcoding.interpreter.compiler;


public class CompileException extends Exception {
    //in which line and column is the error
    private ProgramPosition position;

    public ProgramPosition getPosition() {
        return position;
    }

    //constructor
    public CompileException(String msg, ProgramPosition position){
        super(msg);
        this.position = position;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Error (");
        sb.append(position.getLine() + 1);
        sb.append(", ");
        sb.append(position.getColumn());
        sb.append("): ");
        sb.append(getMessage());
        return sb.toString();
    }
}
