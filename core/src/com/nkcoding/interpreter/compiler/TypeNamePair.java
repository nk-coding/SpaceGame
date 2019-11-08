package com.nkcoding.interpreter.compiler;

public class TypeNamePair {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private DataType type;

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    //simple constructor
    public TypeNamePair() {
    }

    //constructor
    public TypeNamePair(String name, DataType dataType) {
        this.name = name;
        this.type = dataType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(type);
        sb.append(", ");
        sb.append(name);
        sb.append(')');
        return sb.toString();
    }

}
