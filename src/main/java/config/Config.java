package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import utils.Utils;

import java.io.File;
import java.io.IOException;

public class Config {

    /** The bot's API token */
    private String botToken;
    /** Name of the bot */
    private String botName;
    /** The command prefix used for bot commands */
    private String commandPrefix;
    /** Should console be colored */
    private boolean coloredConsole;
    /** The permission required to use bot commands */
    private String usePermissionRequired;
    /** Backup related settings */
    private BackupSettings backupSettings;
    /** Settings about enabled backups */
    private EnabledBackups enabledBackups;

    private static Config config;


    public Config() {
    }

    public Config(String botToken, String botName, String commandPrefix, boolean coloredConsole, String usePermissionRequired, BackupSettings backupSettings, EnabledBackups enabledBackups) {
        this.botToken = botToken;
        this.botName = botName;
        this.commandPrefix = commandPrefix;
        this.coloredConsole = coloredConsole;
        this.usePermissionRequired = usePermissionRequired;
        this.backupSettings = backupSettings;
        this.enabledBackups = enabledBackups;
    }

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

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public boolean isColoredConsole() {
        return coloredConsole;
    }

    public String getUsePermissionRequired() {
        return usePermissionRequired;
    }

    public BackupSettings getBackupSettings() {
        return backupSettings;
    }

    public EnabledBackups getEnabledBackups() {
        return enabledBackups;
    }
}
