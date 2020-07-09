package com.misfis.x5.runner.model;

public class Service {
    public void run(String name, long timeout) throws InterruptedException {
        System.out.println("Service: " + name + " started.");
        Thread.sleep(timeout);
        System.out.println("Service: " + name + " finished.");
    }
}

