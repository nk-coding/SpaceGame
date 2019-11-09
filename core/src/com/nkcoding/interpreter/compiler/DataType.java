package com.nkcoding.interpreter.compiler;

public final class DataType {

    //keywords
    public static final String INTEGER_KW = "int";
    public static final String FLOAT_KW = "float";
    public static final String BOOLEAN_KW = "boolean";
    public static final String STRING_KW = "string";
    public static final String LIST_KW = "list";
    public static final String VOID_KW = "void";

    //data types for simple types
    public static final DataType INTEGER = new DataType(INTEGER_KW);
    public static final DataType FLOAT = new DataType(FLOAT_KW);
    public static final DataType BOOLEAN = new DataType(BOOLEAN_KW);
    public static final DataType STRING = new DataType(STRING_KW);
    //just used for methods
    public static final DataType VOID = new DataType(VOID_KW);

    //name for the this dataType
    public final String name;

    /**if it is a list, is it already defined?*/
    public boolean isInit = true;

    public TypeNamePair[] listTypes = null;


    /**
     * constructor for simple DataTypes like String, int, float, boolean, void
     * no need for
     * @param name name for the DataType
     */
    private DataType(String name) {
        this.name = name;
    }

    public static DataType fromName(String str) {
        switch (str) {
            case INTEGER_KW:
                return INTEGER;
            case FLOAT_KW:
                return FLOAT;
            case BOOLEAN_KW:
                return BOOLEAN;
            case STRING_KW:
                return STRING;
            case LIST_KW:
                //only the list keyword, so it is not specified
                DataType dataType = new DataType(LIST_KW);
                dataType.isInit = false;
                return dataType;
            case VOID_KW:
                return VOID;
            default:
                return parseList(str);
        }
    }

    private static DataType parseList(String str) {

        return null;
    }

    public static boolean contains(String type) {
        return fromName(type) != null;
    }

    //check if it contains datatype
    public static boolean containsDataType(String type) {
        return contains(type) && !type.equals(VOID_KW);
    }

    @Override
    public String toString() {
        return name;
    }
}
