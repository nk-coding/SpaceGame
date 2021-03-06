package com.nkcoding.interpreter.compiler;

import com.nkcoding.interpreter.Expression;
import com.nkcoding.interpreter.ListObject;
import com.nkcoding.interpreter.StackItem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /**
     * if it is a list, is it already defined?
     */
    public boolean isInit = true;

    public TypeNamePair[] listTypes = null;


    /**
     * constructor for simple DataTypes like String, int, float, boolean, void
     * no need for
     *
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
                if (str.length() == 0) return null;
                if (str.charAt(0) == '[') {
                    DataType list = parseList(str);
                    return list;
                } else {
                    return null;
                }
        }
    }

    public static DataType fromExpressions(Expression[] expressions) {
        DataType type = new DataType(LIST_KW);
        TypeNamePair[] lt = new TypeNamePair[expressions.length];
        for (int x = 0; x < expressions.length; x++) {
            lt[x] = new TypeNamePair(null, expressions[x].getType());
        }
        type.listTypes = lt;
        return type;
    }

    private static DataType parseList(String str) {
        ArrayDeque<String> deque = new ArrayDeque<>(List.of(str.split("[ ]|(?=[,=\\[\\]])|(?<=[,=\\[\\]])")));
        deque.removeIf(String::isEmpty);
        deque.pollFirst(); //get rid of the first bracket
        return parseListInternal(deque);
    }

    private static DataType parseListInternal(ArrayDeque<String> tokens) {
        ArrayList<TypeNamePair> types = new ArrayList<>();
        while (!tokens.isEmpty()) {
            String token = tokens.pollFirst();
            TypeNamePair type = new TypeNamePair();
            switch (token) {
                case "[":
                    type.setType(parseListInternal(tokens));
                    break;
                case "]":
                    DataType list = new DataType(LIST_KW);
                    list.listTypes = types.toArray(TypeNamePair[]::new);
                    return list;
                default:
                    DataType t = fromName(token);
                    if (t == null) {
                        throw new IllegalArgumentException("expected: type found: " + token);
                    } else if (t.name.equals(LIST_KW) || t.equals(VOID)) {
                        throw new IllegalArgumentException("illegal type in list declaration: " + t);
                    } else {
                        type.setType(t);
                    }

                    break;
            }
            types.add(type);
            //check for name
            String possibleName = tokens.peekFirst();
            if (possibleName.equals(",")) {
                tokens.pollFirst();
            } else if (!possibleName.equals("]")) {
                //TODO check if identifier is correct
                type.setName(possibleName);
                tokens.pollFirst();
                String next = tokens.peekFirst();
                if (next.equals(",")) {
                    tokens.pollFirst();
                } else if (!next.equals("]")) {
                    throw new IllegalArgumentException("expected: , or ] found: " + next);
                }
            }
        }
        return null;
    }

    public static boolean contains(String type) {
        return contains(type, false);
    }

    public static boolean contains(String type, boolean uninit) {
        try {
            DataType returnType = fromName(type);
            if (uninit) {
                return returnType != null;
            } else {
                return returnType != null && returnType.isInit;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    //check if it contains datatype
    public static boolean containsDataType(String type) {
        return containsDataType(type, false);
    }

    public static boolean containsDataType(String type, boolean uninit) {
        return contains(type, uninit) && !type.equals(VOID_KW);
    }

    private static ListObject createDefaultList(DataType listType) {
        ListObject list = new ListObject(listType.listTypes.length);
        for (int x = 0; x < listType.listTypes.length; x++) {
            DataType type = listType.listTypes[x].getType();
            StackItem stackItem = new StackItem(type);
            stackItem.setValue(type.getDefaultValue());
            list.items[x] = stackItem;
        }
        return list;
    }

    public Object getDefaultValue() {
        switch (name) {
            case FLOAT_KW:
                return 0f;
            case INTEGER_KW:
                return 0;
            case BOOLEAN_KW:
                return false;
            case STRING_KW:
                return "";
            case LIST_KW:
                return createDefaultList(this);
            case VOID_KW:
                return null;
            default:
                System.out.println(name);
                throw new IllegalStateException("cannot create alternative value");
        }
    }

    public boolean isAssignableFrom(DataType dataType) {
        if (this.name.equals(LIST_KW)) {
            //check for everything except the names
            if (dataType.name.equals(LIST_KW)) {
                if (listTypes.length == dataType.listTypes.length) {
                    boolean allAssignable = true;
                    for (int x = 0; x < listTypes.length; x++) {
                        allAssignable &= listTypes[x].getType().isAssignableFrom(dataType.listTypes[x].getType());
                    }
                    return allAssignable;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return equals(dataType);
        }
    }

    @Override
    public String toString() {
        if (listTypes == null || !name.equals(LIST_KW)) {
            return name;
        } else {
            StringBuilder builder = new StringBuilder();
            for (TypeNamePair pair : listTypes) {
                builder.append(pair.getType());
                builder.append(" ");
                if (pair.getName() != null) {
                    builder.append(pair.getName());
                }
                builder.append(", ");
            }
            return "[" + builder.substring(0, builder.length() - 2) + "]";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(DataType.class)) {
            boolean arrayEqual = listTypesEquals(((DataType) obj).listTypes);
            return name.equals(((DataType) obj).name) && arrayEqual;
        } else {
            return false;
        }
    }

    private boolean listTypesEquals(TypeNamePair[] otherListTypes) {
        if (listTypes == otherListTypes) return true;
        if (listTypes == null ^ otherListTypes == null) return false;
        if (listTypes.length != otherListTypes.length) return false;
        for (int i = 0; i < listTypes.length; i++) {
            if (!listTypes[i].getType().equals(otherListTypes[i].getType())) return false;
        }
        return true;
    }
}
