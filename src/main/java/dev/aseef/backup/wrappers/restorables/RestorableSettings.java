package dev.aseef.backup.wrappers.restorables;

import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.sql.Timestamp;

public class RestorableSettings extends Restorable {

    private final String guildName;
    private final int afkChannelId;
    private final Guild.Timeout afkTimeOut;
    private final long defaultChannelId;
    private final long systemChannelId;
    private final String defaultNotifLevel;
    private final String explicitContentLevel;
    private final Guild.MFALevel verificationLevel;
    private final @Nullable Integer bannerAttachmentId;
    private final @Nullable Integer iconAttachmentId;

    public RestorableSettings(int guild, Timestamp timestamp, String guildName, int afkChannelIndexId, int afkTimeOut, long defaultChannelId, long systemChannelId, String defaultNotifLevel, String explicitContentLevel, String region, int verificationLevel, @Nullable Integer bannerAttachmentId, @Nullable Integer iconAttachmentId) {
        super(guild, timestamp);
        this.guildName = guildName;
        this.afkChannelId = afkChannelIndexId;
        this.afkTimeOut = Guild.Timeout.fromKey(afkTimeOut);
        this.defaultChannelId = defaultChannelId;
        this.systemChannelId = systemChannelId;
        this.defaultNotifLevel = defaultNotifLevel;
        this.explicitContentLevel = explicitContentLevel;
        this.verificationLevel = Guild.MFALevel.fromKey(verificationLevel);
        this.bannerAttachmentId = bannerAttachmentId;
        this.iconAttachmentId = iconAttachmentId;
    }

    @Override
    protected RestoreType determineRestoreType() {
        return null;
    }

    @Override
    protected boolean editRestore() {
        return false;
    }

    @Override
    protected boolean createNewRestore() {
        return false;
    }

}
