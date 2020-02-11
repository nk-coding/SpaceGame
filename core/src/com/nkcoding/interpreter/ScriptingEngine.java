package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptingEngine {

    //Queue of all ExternMethodFutures, they will be executed on the main tread
    //therefore, this queue must be concurrent
    private final ConcurrentLinkedQueue<ExternalMethodFuture> futureQueue = new ConcurrentLinkedQueue<>();
    //the ThreadPool where all scripts run
    //I probably replace this with a fixed size pool to reduce CPU performance impact, because these scripts run normally
    //at a relatively low priority
    private ExecutorService executor = Executors.newFixedThreadPool( 10,
            r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });

    public final ConcurrentLinkedQueue<ExternalMethodFuture> getFutureQueue() {
        return futureQueue;
    }

    /**
     * call a method async
     *
     * @param methodStatement the method that is invoked
     * @param runningState if not null, is set to true when it starts and is set false when method returns
     * @param globalVariables map with all the global variables in it
     * @param parameters      the parameters for the method
     */
    public void runMethod(MethodStatement methodStatement, RunningState runningState,
                          final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables,
                          Object... parameters) {
        if (runningState != null) {
            runningState.setRunningState(true);
        }
        //create the necessary stack
        Stack stack = new Stack(10, this, globalVariables);
        //no parameters and that's ok
        if (methodStatement.getDefinition().getParameters().length == 0 && (parameters == null || parameters.length == 0)) {
            executor.submit(() -> {
                stack.beginStackLevel();
                try {
                    methodStatement.run(stack);
                } catch (ReturnException e) {
                    e.printStackTrace();
                } catch (Exception fatal) {
                    System.out.println("fatal exception occurred");
                    fatal.printStackTrace();
                }
                if (runningState != null) {
                    runningState.setRunningState(false);
                }
                stack.clearStackLevel();
            });
        } else if (parameters != null && parameters.length == methodStatement.getDefinition().getParameters().length) {
            //enough parameters
            executor.submit(() -> {
                stack.beginStackLevel();
                try {
                    //add all the parameters to the stack
                    TypeNamePair[] parameterNames = methodStatement.getDefinition().getParameters();
                    for (int x = 0; x < parameters.length; x++) {
                        switch (parameterNames[x].getType().name) {
                            case DataType.BOOLEAN_KW:
                                stack.addToStack(parameterNames[x].getName(), parameters[x], DataType.BOOLEAN);
                                break;
                            case DataType.FLOAT_KW:
                                stack.addToStack(parameterNames[x].getName(), parameters[x], DataType.FLOAT);
                                break;
                            case DataType.INTEGER_KW:
                                stack.addToStack(parameterNames[x].getName(), parameters[x], DataType.INTEGER);
                                break;
                            case DataType.STRING_KW:
                                stack.addToStack(parameterNames[x].getName(), parameters[x], DataType.STRING);
                                break;
                            case DataType.VOID_KW:
                                throw new IllegalArgumentException("a parameter cannot be void");
                            default:
                                throw new IllegalArgumentException("cannot handle the DataType " + parameterNames[x].getType());
                        }
                    }
                    methodStatement.run(stack);
                } catch (ReturnException e) {
                    e.printStackTrace();
                } catch (Exception fatal) {
                    System.out.println("fatal exception occurred");
                    fatal.printStackTrace();
                }
                stack.clearStackLevel();
                if (runningState != null) {
                    runningState.setRunningState(false);
                }
            });
        }
    }

}
