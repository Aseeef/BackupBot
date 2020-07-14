package config;

public class EnabledBackups {

    private boolean backupBans;
    private boolean backupEmojis;
    private boolean backupRoles;
    private boolean backupChannels;
    private boolean backupMembers;
    private boolean backupMessages;
    private boolean backupServerSettings;

    public EnabledBackups() {
    }

    public EnabledBackups(boolean backupBans, boolean backupEmojis, boolean backupRoles, boolean backupChannels, boolean backupMembers, boolean backupMessages, boolean backupServerSettings) {
        this.backupBans = backupBans;
        this.backupEmojis = backupEmojis;
        this.backupRoles = backupRoles;
        this.backupChannels = backupChannels;
        this.backupMembers = backupMembers;
        this.backupMessages = backupMessages;
        this.backupServerSettings = backupServerSettings;
    }

    public boolean isBackupBans() {
        return backupBans;
    }

    public boolean isBackupEmojis() {
        return backupEmojis;
    }

    public boolean isBackupRoles() {
        return backupRoles;
    }

    public boolean isBackupChannels() {
        return backupChannels;
    }

    public boolean isBackupMembers() {
        return backupMembers;
    }

    public boolean isBackupMessages() {
        return backupMessages;
    }

    public boolean isBackupServerSettings() {
        return backupServerSettings;
    }
}
