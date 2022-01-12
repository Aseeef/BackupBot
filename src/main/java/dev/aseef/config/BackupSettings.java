package dev.aseef.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor @NoArgsConstructor @Getter
public class BackupSettings {

    /** These channels will not be backed up */
    private List<Long> blacklistedChannels;

    /** This is how old a message can be and the bot will still update it */
    private int messageUpdateTime;

    /** This is the max size for attachments that would still be backed up */
    private int maxAttachmentSize;

    /** Whether files should be zipped (compressed) */
    private boolean zipFiles;
}
