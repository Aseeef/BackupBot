package config;

import java.util.List;

public class BackupSettings {

    /** These channels will not be backed up */
    private List<Long> blacklistedChannels;

    /** This is how old a message can be and the bot will still update it */
    private int messageUpdateTime;

    /** This is the max size for attachments that would still be backed up */
    private int maxAttachmentSize;

    /** Whether files should be zipped (compressed) */
    private boolean zipFiles;

    public BackupSettings() {
    }

    public BackupSettings(List<Long> blacklistedChannels, int messageUpdateTime, int maxAttachmentSize, boolean zipFiles) {
        this.blacklistedChannels = blacklistedChannels;
        this.messageUpdateTime = messageUpdateTime;
        this.maxAttachmentSize = maxAttachmentSize;
        this.zipFiles = zipFiles;
    }

    public List<Long> getBlacklistedChannels() {
        return blacklistedChannels;
    }

    public int getMessageUpdateTime() {
        return messageUpdateTime;
    }

    public int getMaxAttachmentSize() {
        return maxAttachmentSize;
    }

    public boolean isZipFiles() {
        return zipFiles;
    }
}
