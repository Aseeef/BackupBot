package dev.aseef.backup.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@AllArgsConstructor @Getter
public class WrapperGuildProfile {
    /**
     * The internal, auto-incremented index id for this guild.
     */
    private int indexId;
    /**
     * The discord-set "snowflake" id of the guilde
     */
    private long discordSnowflake;
    /**
     * Whether to store this backup type for this guild
     */
    private boolean banBackups, emojiBackups, roleBackups, channelBackups, membersBackups, messagesBackups, settingsBackups;
    /**
     * Whether to enable the nuke-prevention setting. When this setting is enabled, if an admin deletes a "protected channel"
     * all their permissions will be removed. This setting only works if the bot has a higher role then the target.
     */
    private boolean antiNuke;
    /**
     * The maximum size an attachment may have to be saved into the database. The size is in kilobytes.
     * For your reference: 1000 bytes = 1 kb, 1000 kb = 1 mb, 1000 mb = 1gb.
     */
    private int maxAttachmentSize;
    /**
     * When the bot is restarted, it will check if while it was offline, if any known messages where updated (or edited). How far should it
     * look back to see if the message was updated. The time unit is in SECONDS.
     *
     * Remember, the chances that someone would edit a 1 yr old message thousands of messages ago is very little. So this setting can save
     * valuable CPU resources. Set to "-1" if you want to check ALL known messages to see if they were updated.
     */
    private int messageKeepUpdatedTime;
    /**
     * The oldest available backup. Corresponds to when the bot first was enabled.
     */
    private Timestamp initialBackup;
    /**
     * The latest backup. The last time the bot saved anything.
     */
    private Timestamp latestBackup;

    public boolean isChannelBackups() {
        return channelBackups && roleBackups;
    }

    public boolean isMembersBackups() {
        return roleBackups && membersBackups;
    }

    public boolean isMessagesBackups() {
        return messagesBackups && emojiBackups && isChannelBackups() && isMembersBackups();
    }

    public boolean isServerSettings() {
        return isChannelBackups() && settingsBackups;
    }
}
