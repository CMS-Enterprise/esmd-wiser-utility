package gov.cms.esmd.utility;

import org.yaml.snakeyaml.Yaml;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.*;



public class PropertiesUtils {

    private static Properties properties ;
    public static Properties loadProperties() {
        if( properties == null ){
            properties = loadProperties(null);
        }
        return properties;
    }

    public static Properties loadProperties(String profile) {
        try {
            String yamlContent = resourceToString("/api-properties.yml", defaultCharset());
            if (isBlank(yamlContent)) {
                throw new IllegalStateException("Configuration file is empty");
            }
            
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(yamlContent);
            
            if (loaded == null) {
                throw new IllegalStateException("Invalid YAML configuration");
            }
            
            if (!(loaded instanceof Map)) {
                throw new IllegalStateException("Configuration must be a valid YAML map");
            }
            
            Properties properties = new Properties();
            @SuppressWarnings("unchecked")
            Map<String, Object> loadedMap = (Map<String, Object>) loaded;
            properties.putAll(getFlattenedMap(loadedMap));
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load properties: " + e.getMessage(), e);
        }
    }

    private static final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (!isBlank(path))
                key = path + (key.startsWith("[") ? key : '.' + key);
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                buildFlattenedMap(result, (Map<String, Object>) value, key);
            } else if (value instanceof Collection) {
                int count = 0;
                for (Object object : (Collection<?>) value)
                    buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
            } else {
                result.put(key, value != null ? "" + value : "");
            }
        });
    }
    /**
     * Gets a required property from the configuration.
     * Throws an exception if the property is not found or empty.
     *
     * @param key the property key
     * @return the property value
     * @throws IllegalStateException if the property is not found or empty
     */
    public static  String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property not found or empty: " + key);
        }
        return value.trim();
    }
    public static void main(String []args)throws Exception{
        Properties properties = loadProperties();
        System.out.println(properties.get("api.environment.uat"));
    }
}
