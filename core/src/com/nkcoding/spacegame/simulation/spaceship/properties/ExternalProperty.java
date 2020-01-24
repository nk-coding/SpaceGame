package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.RunningState;
import com.nkcoding.interpreter.ScriptingEngine;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ExternalProperty<T> implements RunningState {

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
     * is the value changed
     */
    protected boolean changed = false;
    /**
     * method that is called when property is changed and notifyChange is activated
     */
    private MethodStatement changedMethodStatement = null;

    /**
     * is this running
     */
    protected volatile boolean isRunning = false;

    /**
     * are multiple instances of this allowed?
     */
    protected boolean allowParallel = false;

    public ExternalProperty(String name) {
        this.name = name;
    }

    public MethodStatement getChangedMethodStatement() {
        return changedMethodStatement;
    }

    public void setChangedMethodStatement(MethodStatement changedMethodStatement) {
        this.changedMethodStatement = changedMethodStatement;
    }

    public void startChangedHandler(ScriptingEngine engine, final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables) {
        if ((!isRunning || allowParallel) && notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), this, globalVariables, get2());
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

    @Override
    public void setRunningState(boolean runningState) {
        this.isRunning = runningState;
    }

    @Override
    public boolean getRunningState() {
        return isRunning;
    }

    public void allowParallel() {
        this.allowParallel = true;
    }
}
