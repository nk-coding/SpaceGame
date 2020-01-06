package com.nkcoding.interpreter.compiler;

import java.util.Objects;

public class TypeNamePair {

    private String name;
    private DataType type;

    //simple constructor
    public TypeNamePair() {
    }

    //constructor
    public TypeNamePair(String name, DataType dataType) {
        this.name = name;
        this.type = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
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

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(TypeNamePair.class)) {
            return Objects.equals(((TypeNamePair) obj).getName(), (getName()))
                    && Objects.equals(((TypeNamePair) obj).getType(), (getType()));
        } else {
            return false;
        }
    }
}
