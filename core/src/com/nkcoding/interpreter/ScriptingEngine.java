package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptingEngine {

    //Queue of all ExternMethodFutures, they will be executed on the main tread
    //therefore, this queue must be concurrent
    private final ConcurrentLinkedQueue<ExternalMethodFuture> futureQueue = new ConcurrentLinkedQueue<>();

    public final ConcurrentLinkedQueue<ExternalMethodFuture> getFutureQueue() {
        return futureQueue;
    }

    //the ThreadPool where all scripts run
    //I probably replace this with a fixed size pool to reduce CPU performance impact, because these scripts run normally
    //at a relatively low priority
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * call a method async
     * @param methodStatement the method that is invoked
     * @param parameters the parameters for the method
     */
    public void runMethod(MethodStatement methodStatement,
                          final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables,
                          Object... parameters) {
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
                }
                catch (Exception fatal) {
                    System.out.println("fatal exception occurred");
                    fatal.printStackTrace();
                }
                stack.clearStackLevel();
                System.out.println("this is just a test");
            });
        }
        else if (parameters != null && parameters.length == methodStatement.getDefinition().getParameters().length) {
            //enough parameters
            executor.submit(() -> {
                stack.beginStackLevel();
                try {
                    //add all the parameters to the stack
                    TypeNamePair[] parameterNames = methodStatement.getDefinition().getParameters();
                    for (int x = 0; x < parameters.length; x++) {
                        switch (parameterNames[x].getType()) {
                            case DataTypes.Boolean:
                                stack.addToStack(parameterNames[x].getName(), (Boolean)parameters[x], DataTypes.Boolean);
                                break;
                            case DataTypes.Float:
                                stack.addToStack(parameterNames[x].getName(), (Float)parameters[x], DataTypes.Float);
                                break;
                            case DataTypes.Integer:
                                stack.addToStack(parameterNames[x].getName(), (Integer)parameters[x], DataTypes.Integer);
                                break;
                            case DataTypes.String:
                                stack.addToStack(parameterNames[x].getName(), (String)parameters[x], DataTypes.String);
                                break;
                            case DataTypes.Void:
                                throw new IllegalArgumentException("a parameter cannot be void");
                            default:
                                throw new IllegalArgumentException("cannot handle the DataType " + parameterNames[x].getType());
                        }
                    }
                    methodStatement.run(stack);
                } catch (ReturnException e) {
                    e.printStackTrace();
                }
                catch (Exception fatal) {
                    System.out.println("fatal exception occurred");
                    fatal.printStackTrace();
                }
                stack.clearStackLevel();
            });
        }
    }

}
