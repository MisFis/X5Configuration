package com.misfis.x5.runner.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.misfis.x5.runner.Main;
import com.misfis.x5.runner.service.ServiceContainer;
import com.misfis.x5.runner.model.ConfigurationData;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;


@Log
@Getter
public class ConfigurationReader {
    private ConfigurationData properties;


    public ConfigurationReader(String pathToFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            this.properties = mapper
                    .readValue(getConfigurationFile(pathToFile),
                            ConfigurationData.class);
        } catch (IOException e) {
            log.log(Level.WARNING, String.format("file can't read %s", e.getMessage()));
            System.exit(1);
        }

    }

    public int getThreadNumber () {
        return this.properties.getThreadsCount();
    }

    public ServiceContainer createContainer() {
       return new ServiceContainer(this.getThreadNumber(), this.properties.getServices());
    }

    private InputStream getConfigurationFile(String pathToFile) {
        InputStream in = Main.class.getResourceAsStream(pathToFile);
        // return new File(pathToFile);
        return in;
    }

}
