package lt.esdc.tunnel.config;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public class ConfigLoader {
    private static final String CONFIG_FILE_NAME = "config.json";

    public static AppConfig loadConfig() throws IOException {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)))) {

            Gson gson = new Gson();
            return gson.fromJson(reader, AppConfig.class);
        }
    }
}
