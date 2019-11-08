package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.ScriptingEngine;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ExternalProperty<T> {

    /**
     * are setters allowed?
     */
    public final boolean readonly;

    /**
     * should changed be notified?
     */
    public final boolean notifyChanges;

    /**
     * the name of the property
     */
    public final String name;

    /**
     * method that is called when property is changed and notifyChange is activated
     */
    private MethodStatement changedMethodStatement = null;

    /**
     * is the value changed
     */
    protected boolean changed = false;

    public MethodStatement getChangedMethodStatement() {
        return changedMethodStatement;
    }

    public void setChangedMethodStatement(MethodStatement changedMethodStatement) {
        this.changedMethodStatement = changedMethodStatement;
    }

    public ExternalProperty(boolean readonly, boolean notifyChanges, String name) {
        this.readonly = readonly;
        this.notifyChanges = notifyChanges;
        this.name = name;
    }

    public void startChangedHandler(ScriptingEngine engine, final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables) {
        if (notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), globalVariables, get2());
            changed = false;
        }
    }

    /**
     * a simple clone method
     * should be overwritten, if cloning should be done differently or not at all
     *
     * @param from the ExternalProperty to clone
     */
    public void copyFrom(ExternalProperty<T> from) {
        set(from.get2());
        setChangedMethodStatement(from.getChangedMethodStatement());
    }

    public abstract void setInitValue(String value);

    public abstract void set(T value);

    public abstract T get2();

}
