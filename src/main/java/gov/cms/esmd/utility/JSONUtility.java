package gov.cms.esmd.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JSONUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONUtility.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String serialize(Object object) {
        LOGGER.info("Start JSONUtility:serialize(Object)....");
        if (object == null) {
            return null;
        }
        LOGGER.info("End JSONUtility:serialize(Object)....");
        return gson.toJson(object);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        LOGGER.info("Start JSONUtility:deserialize(json, class)....");
        if (json == null || clazz == null) {
            return null;
        }
        LOGGER.info("End JSONUtility:deserialize(json, class)....");
        return gson.fromJson(json, clazz);
    }


}
