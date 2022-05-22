package dev.aseef.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.aseef.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Config {

    /** The bot's API token */
    private String botToken;
    /** Name of the bot */
    private String botName;
    /** The command prefix used for bot dev.aseef.commands */
    private String commandPrefix;
    /** Should dev.aseef.console be colored */
    private boolean coloredConsole;
    /** Size of the thread pool*/
    @JsonProperty("thread-pool") private int threadPool;
    /** The permission required to use bot dev.aseef.commands */
    private String usePermissionRequired;
    /** Backup related settings */
    private BackupSettings backupSettings;
    /** Settings about enabled backups */
    private EnabledBackups enabledBackups;

    private static Config config;

    public static Config get() {
        return config;
    }

    public static void load() {
        try {
            File file = Utils.getConfigFile();
            // Load the object back
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            config = mapper.readValue(file, Config.class);

            System.out.println("Successfully loaded bot configuration!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
