package com.misfis.x5.runner.service;

import com.misfis.x5.runner.model.ConfigurationData;
import com.misfis.x5.runner.model.Service;
import lombok.*;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;


@ToString
@Log
public class ServiceContainer {
    private final ExecutorService executorService;
    private final Map<String, ConfigurationData.ConfigurationProperty> services;
    private final Map<String, ServiceWorker> maps = new HashMap<>();


    public ServiceContainer(int threadNumber, Map<String, ConfigurationData.ConfigurationProperty> services) {
        this.executorService = Executors.newFixedThreadPool(threadNumber);
        this.services = services;
        this.createTressDependency(services.keySet());
    }


    private void createTressDependency(Set<String> serviceNames) {
        try {
            serviceNames.forEach(this::createServiceTrees);
        } catch (RuntimeException error) {
            log.log(Level.WARNING, error.getMessage());
            System.exit(-1);
        }

    }

    public void run() {
        System.out.println("Количестов сервисов:" + maps.size());
        while (!maps.values().stream().allMatch(ServiceWorker::isFinish)) {
            this.startExecution();
        }

    }

    @SneakyThrows
    private void startExecution() {
        maps.values()
                .forEach(serviceWorker -> {
                    serviceWorker.runService(executorService);
                });
    }

    private ServiceWorker createServiceTrees(String serviceName) {
        if (maps.get(serviceName) != null) return maps.get(serviceName);
        ConfigurationData.ConfigurationProperty configurationProperty = services.get(serviceName);

        checkServiceContainer(serviceName);

        List<String> dependsOn = configurationProperty.getDependsOn();
        ServiceWorker serviceWorkerRoot = new ServiceWorker(serviceName, configurationProperty.getTimeout());

        if (dependsOn != null) {
            dependsOn.forEach(s -> {
                checkServiceContainer(s);
                ConfigurationData.ConfigurationProperty dependProp = services.get(s);
                if (hasRecursiveService(serviceName, dependProp.getDependsOn()))
                    throw new RuntimeException(String.format("Services have recursive dependency, service %s depend %s", serviceName, s));
                ServiceWorker serviceWorker = createServiceTrees(s);
                serviceWorkerRoot.addToDependsTo(serviceWorker);
            });
        }

        maps.put(serviceName, serviceWorkerRoot);
        return serviceWorkerRoot;
    }

    private void checkServiceContainer (String serviceName) {
        ConfigurationData.ConfigurationProperty configurationProperty = services.get(serviceName);
        if (configurationProperty == null) throw new NullPointerException(String.format("Service %s not found", serviceName));
    }

    private boolean hasRecursiveService(String serviceName, List<String> dependOn) {
        return dependOn != null && dependOn.contains(serviceName);
    }

    @Data()
    @ToString(exclude = {"isFinish", "isWorking", "timeout"})
    private class ServiceWorker {
        private int timeout;
        private List<ServiceWorker> dependsTo = new ArrayList<>();
        private String name;

        private boolean isWorking;
        private boolean isFinish = false;


        public ServiceWorker(String name, int timeout) {
            this.name = name;
            this.timeout = timeout;
        }

        public void addToDependsTo(ServiceWorker serviceWorker) {
            if (serviceWorker != null) dependsTo.add(serviceWorker);
        }

        public void runService(ExecutorService executorService) {
            if (isAllFinishDepends() && !isFinish && !isWorking) {
                isWorking = true;

                executorService.submit(() -> {
                    try {
                        new Service().run(name, getTimeout());
                        this.isFinish = true;
                    } catch (InterruptedException e) {
                        log.log(Level.WARNING, e.getMessage());
                        this.isFinish = true;
                    }
                });
            }
        }

        private boolean isAllFinishDepends() {
            return dependsTo.stream().allMatch(serviceWorker -> serviceWorker.isFinish);
        }
    }
}
