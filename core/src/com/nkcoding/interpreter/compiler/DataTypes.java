package com.nkcoding.interpreter.compiler;

public class DataTypes {
    public static final String Integer = "int";
    public static final String Float = "float";
    public static final String Boolean = "boolean";
    public static final String String = "String";
    //just used for methods
    public static final String Void = "void";

    //checks if this list contains the returntype
    public static boolean contains(String type) {
        return containsDataType(type) || type.equals(Void);
    }

    //check if it contains datatype
    public static boolean containsDataType(String type) {
        return type.equals(Integer) ||
                type.equals(Float) ||
                type.equals(Boolean) ||
                type.equals(String);
    }
}
