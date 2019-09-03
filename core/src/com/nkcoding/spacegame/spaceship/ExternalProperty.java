package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.lang.reflect.Method;

public abstract class ExternalProperty {

    /**are setters allowed?*/
    public final boolean readonly;

    /**should changed be notified?*/
    public final boolean notifyChanges;

    /**method that is called when property is changed and notifyChange is activated*/
    private MethodStatement changedMethodStatement = null;

    /**is the value changed*/
    protected boolean changed = false;

    public MethodStatement getChangedMethodStatement() {
        return changedMethodStatement;
    }

    public void setChangedMethodStatement(MethodStatement changedMethodStatement) {
        this.changedMethodStatement = changedMethodStatement;
    }

    public ExternalProperty(boolean readonly, boolean notifyChanges) {
        this.readonly = readonly;
        this.notifyChanges = notifyChanges;
    }

    public abstract void StartChangedHandler(ScriptingEngine engine);

}
