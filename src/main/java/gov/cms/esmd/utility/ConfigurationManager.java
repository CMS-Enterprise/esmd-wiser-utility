package gov.cms.esmd.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigurationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
    private static Map<String, Object> obj;

    private ConfigurationManager() {

    }
    private static void loadYamlFile() {
        LOGGER.info("Start ConfigurationManager:loadYamlFile() load configuration yml file...");
        try (InputStream inputStream = ConfigurationManager.class.getClassLoader().getResourceAsStream("api-properties.yml")) {
            if (inputStream == null) {
                throw new IllegalStateException("Configuration file 'api-properties.yml' not found in classpath");
            }
            
            // Create safe YAML loader with restricted types
            Yaml safeYaml = new Yaml();
            Object loaded = safeYaml.load(inputStream);
            
            if (loaded == null) {
                throw new IllegalStateException("Configuration file is empty or invalid");
            }
            
            if (!(loaded instanceof Map)) {
                throw new IllegalStateException("Configuration file must contain a valid YAML map structure");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> loadedMap = (Map<String, Object>) loaded;
            obj = loadedMap;
            LOGGER.info("End ConfigurationManager:loadYamlFile() load configuration yml file...");
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration file", e);
            throw new IllegalStateException("Failed to load configuration file: " + e.getMessage(), e);
        }
    }



/*
YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(new PathResource(file.toPath()));
    factory.afterPropertiesSet();
 */
    public static synchronized Map<String, Object> getInstance() {
        LOGGER.info("Start ConfigurationManager:getInstance() ...");
        if (obj == null) {
            loadYamlFile();
        }
        LOGGER.info("End ConfigurationManager:getInstance() ...");
        return obj;
    }

}
