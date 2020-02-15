package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;
import com.nkcoding.ui.ValueStatus;

import java.util.Collection;
import java.util.Map;

public class ExternalPropertySpecification {

    /**
     * the name of the property
     */
    public final String name;

    /**
     * the type of the property
     */
    public final DataType type;

    /**
     * can the value be modified by the user
     */
    public final boolean supportsRead;

    /**
     * can the user get the value
     */
    public final boolean supportsWrite;

    /**
     * is the changedHandler used
     */
    public final boolean supportsChangedHandler;

    public final boolean supportsConcurrentHandling;

    public final  String getterName;

    public final String setterName;


    /**
     * the default constructor
     */
    public ExternalPropertySpecification(String name, DataType type, boolean supportsRead, boolean supportsWrite,
                                         boolean supportsChangedHandler, boolean supportsConcurrentHandling,
                                         String getterName, String setterName) {
        //check if type is ok
        if (type.equals(DataType.VOID)) throw new IllegalArgumentException("type cannot be " + DataType.VOID_KW);
        this.name = name;
        this.type = type;
        this.supportsRead = supportsRead;
        this.supportsWrite = supportsWrite;
        this.supportsChangedHandler = supportsChangedHandler;
        this.supportsConcurrentHandling = supportsConcurrentHandling;
        this.getterName = getterName;
        this.setterName = setterName;
    }

    public ValueStatus verifyInit(String init) {
        if (init.equals("")) {
            return ValueStatus.OK;
        } else if (init.equals("~")) {
            return ValueStatus.WARNING;
        }
        switch (type.name) {
            case DataType.BOOLEAN_KW:
                return ValueStatus.of(init.equalsIgnoreCase("true")
                        || init.equalsIgnoreCase("false"));
            case DataType.FLOAT_KW:
                try {
                    if (init.isBlank()) return ValueStatus.OK;
                    Float.parseFloat(init);
                    return ValueStatus.OK;
                } catch (Exception e) {
                    return ValueStatus.ERROR;
                }
            case DataType.INTEGER_KW:
                try {
                    if (init.isBlank()) return ValueStatus.OK;
                    Integer.parseInt(init);
                    return ValueStatus.OK;
                } catch (Exception e) {
                    return ValueStatus.ERROR;
                }
            case DataType.STRING_KW:
                return ValueStatus.OK;
            default:
                throw new RuntimeException("not implemented");

        }
    }

    /**
     * checks if the method exists and has the correct signature
     *
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public ValueStatus verifyHandler(String handlerName, Map<String, ? extends MethodDefinition> methods) {
        if (handlerName.equals("")) {
            return ValueStatus.OK;
        } else if (handlerName.equals("~")) {
            return ValueStatus.WARNING;
        }
        boolean correctHandler = false;
        MethodDefinition def = methods.get(handlerName);
        if (def != null) {
            if (def.getParameters().length == 1 && def.getParameters()[0].getType().equals(type)) {
                correctHandler = true;
            }
        }

        return ValueStatus.of(correctHandler);
    }

    /**
     * checks for correct handler and init
     *
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verify(Map<String, ? extends MethodDefinition> methods, String initData, String handlerName) {
        return (supportsWrite || verifyInit(initData) != ValueStatus.ERROR)
                && (supportsChangedHandler || verifyHandler(handlerName, methods) != ValueStatus.ERROR);
    }

    //adds the external method definitions the list
    public void addExternalMethodDefs(Collection<? super MethodDefinition> list) {
        if (supportsWrite) {
            list.add(createSetter());
        }
        if (supportsRead) {
            list.add(createGetter());
        }
    }

    //helper for addExternalMethodDef
    private MethodDefinition createGetter() {
        return new MethodDefinition(MethodType.External, getterName, type, new TypeNamePair("id", DataType.STRING));
    }

    private MethodDefinition createSetter() {
        return new MethodDefinition(MethodType.External, setterName, DataType.VOID,
                new TypeNamePair("id", DataType.STRING),
                new TypeNamePair("value", type));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == ExternalPropertySpecification.class) {
            return ((ExternalPropertySpecification) obj).name.equals(name)
                    && ((ExternalPropertySpecification) obj).type.equals(type)
                    && ((ExternalPropertySpecification) obj).supportsWrite == supportsWrite
                    && ((ExternalPropertySpecification) obj).supportsRead == supportsRead
                    && ((ExternalPropertySpecification) obj).supportsChangedHandler == supportsChangedHandler
                    && ((ExternalPropertySpecification) obj).getterName.equals(getterName)
                    && ((ExternalPropertySpecification) obj).setterName.equals(setterName);
        } else {
            return false;
        }
    }

    public static ExternalPropertySpecificationBuilder builder(String name, DataType type) {
        return new ExternalPropertySpecificationBuilder(name, type);
    }

    public static class ExternalPropertySpecificationBuilder {
        public String name;
        public DataType type;
        public boolean supportsRead = false;
        public boolean supportsWrite = false;
        public boolean supportsChangedHandler = false;
        public boolean supportsConcurrentHandling = false;
        public String getterName = null;
        public String setterName = null;

        public ExternalPropertySpecificationBuilder(String name, DataType type) {
            this.name = name;
            this.type = type;
        }

        public ExternalPropertySpecificationBuilder read() {
            supportsRead = true;
            return this;
        }

        public ExternalPropertySpecificationBuilder write() {
            supportsWrite = true;
            return this;
        }

        public ExternalPropertySpecificationBuilder changedHandler() {
            supportsChangedHandler = true;
            return this;
        }

        public ExternalPropertySpecificationBuilder concurrent() {
            supportsConcurrentHandling = true;
            return this;
        }

        public ExternalPropertySpecificationBuilder getterName(String getterName) {
            this.getterName = getterName;
            return this;
        }

        public ExternalPropertySpecificationBuilder setterName(String setterName) {
            this.setterName = setterName;
            return this;
        }

        public ExternalPropertySpecification build() {
            return new ExternalPropertySpecification(name, type, supportsRead, supportsWrite,
                    supportsChangedHandler, supportsConcurrentHandling,
                    getterName != null ? getterName : "get" + name,
                    setterName != null ? setterName : "set" + name);
        }
    }
}
