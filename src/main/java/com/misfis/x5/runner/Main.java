package com.misfis.x5.runner;

import com.misfis.x5.runner.config.ConfigurationReader;
import com.misfis.x5.runner.service.ServiceContainer;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static ExecutorService configurationRunners = Executors.newFixedThreadPool(4);

    static String[] configurationFiles = {
            ClassLoader.getSystemResource("configuration.yaml").getPath(),
            ClassLoader.getSystemResource("configuration_2.yaml").getPath(),
            ClassLoader.getSystemResource("configuration_3.yaml").getPath()
    };

    @SneakyThrows
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<Callable<Object>> configurationTask = new ArrayList<>();
        // first group
        for(String config: configurationFiles) {
            configurationTask.add(Executors.callable(() -> startConfiguration(config)));
        }
        configurationRunners.invokeAll(configurationTask);
        long end = System.currentTimeMillis();
        System.out.println("finish " + ((end - start) / 1000));
        System.exit(0);
    }

    public static void startConfiguration (String configurationPath) {
        ConfigurationReader configurationReader = new ConfigurationReader(configurationPath);
        ServiceContainer serviceContainer = configurationReader.createContainer();
        System.out.println("thread count " + configurationReader.getThreadNumber());
        serviceContainer.run();
    }
}
