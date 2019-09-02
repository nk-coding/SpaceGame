package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.lang.reflect.Method;

public class ExternalProperty<T> {
    /**the type of this property*/
    public final String type;

    /**the name of this property*/
    public final String name;

    /**are setters allowed?*/
    public final boolean readonly;

    /**should changed be notified?*/
    public final boolean notifyChanges;

    /**method that is called when property is changed and notifyChange is activated*/
    private MethodStatement changedMethodStatement = null;

    public MethodStatement getChangedMethodStatement() {
        return changedMethodStatement;
    }

    public void setChangedMethodStatement(MethodStatement changedMethodStatement) {
        this.changedMethodStatement = changedMethodStatement;
    }

    public ExternalProperty(String type, String name, boolean readonly, boolean notifyChanges) {
        this.type = type;
        this.name = name;
        this.readonly = readonly;
        this.notifyChanges = notifyChanges;
    }

    /**
     * create the getter
     * @return the MethodDefinition for the getter
     */
    public MethodDefinition createGetter() {
         return new MethodDefinition(MethodType.External, "get" + name, type, new TypeNamePair("id", DataTypes.String));
    }

    /**
     * create the setter if possible
     * @return the MethodDefinition if !readonly or null if readonly
     */
    public MethodDefinition createSetter() {
        return readonly ? null :
                new MethodDefinition(MethodType.External, "set" + name, DataTypes.Void,
                        new TypeNamePair("id", DataTypes.String), new TypeNamePair("value", type));
    }
}
