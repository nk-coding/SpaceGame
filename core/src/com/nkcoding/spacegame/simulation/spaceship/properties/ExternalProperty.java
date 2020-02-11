package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.RunningState;
import com.nkcoding.interpreter.ScriptingEngine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ExternalProperty<T> implements RunningState {

    /**
     * are setters allowed?
     */
    public boolean supportsWrite;

    /**
     * are getters allowed
     */
    public boolean supportsRead;

    /**
     * must changes be notified
     */
    public boolean supportsChangedHandler;

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

    /**
     * inits this ExternalProperty based on the data found in data
     * should be called in init
     */
    public void init(ExternalPropertyData data,  Map<String, MethodStatement> methods) {
        this.supportsRead = data.supportsRead;
        this.supportsWrite = data.supportsWrite;
        this.supportsChangedHandler = data.supportsChangedHandler;
        if (supportsWrite) {
            setInitValue(data.initData);
        }
        if (supportsChangedHandler && !data.handlerName.isBlank()) {
            this.setChangedMethodStatement(methods.get(data.handlerName));
        }
    }

    public MethodStatement getChangedMethodStatement() {
        return changedMethodStatement;
    }

    public void setChangedMethodStatement(MethodStatement changedMethodStatement) {
        this.changedMethodStatement = changedMethodStatement;
    }

    public void startChangedHandler(ScriptingEngine engine, final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables) {
        if ((!isRunning || allowParallel) && supportsChangedHandler && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), this, globalVariables, get2());
            changed = false;
        }
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
