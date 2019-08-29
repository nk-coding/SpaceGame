package com.nkcoding.interpreter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptingEngine {

    //Queue of all ExternMethodFutures, they will be executed on the main tread
    //therefore, this queue must be concurrent
    private ConcurrentLinkedQueue<ExternMethodFuture> futureQueue = new ConcurrentLinkedQueue<>();

    public ConcurrentLinkedQueue<ExternMethodFuture> getFutureQueue() {
        return futureQueue;
    }

    //the ThreadPool where all scripts run
    //I probably replace this with a fixed size pool to reduce CPU performance impact, because these scripts run normally
    //at a relatively low priority
    private ExecutorService executor = Executors.newCachedThreadPool();

    //currently, it only supports running methods with no return type and no parameters
    //maybe I will add parameters
    public void runMethod(MethodStatement methodStatement){
        //create the necessary stack
        Stack stack = new Stack(10, this);
        executor.submit(() -> {
            stack.beginStackLevel();
            try {
                methodStatement.run(stack);
            } catch (ReturnException e) {
                e.printStackTrace();
            }
            catch (Exception fatal) {
                System.out.println("fatal exception occured");
                fatal.printStackTrace();
            }
            stack.clearStackLevel();
        });
    }

}
