package dev.aseef.backup.restorables;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RestorableSettings extends Restorable {

    private String guildName;
    private long afkChannelId;
    private long defaultChannelId;
    private long systemChannelId;
    private String defaultNotifLevel;
    private String explicitContentLevel;
    private String region;
    private String verificationLevel;

    public RestorableSettings(Guild guild, boolean deleted, Timestamp lastModified) {
        super(guild, deleted, lastModified);
    }

    @Override
    public void restore(long time, List<AuditLogEntry> logs) {

        logs = logs.stream().filter((l) -> l.getType() == ActionType.GUILD_UPDATE).collect(Collectors.toList());
        logs = logs.stream().filter(l -> l.getTimeCreated().toInstant().toEpochMilli() > time).collect(Collectors.toList());

        List<AuditLogKey> alreadyFound = new ArrayList<>();
        logs.forEach( (l) -> {
            l.getChanges().keySet().forEach( (change) -> {
                AuditLogKey key = AuditLogKey.valueOf(change);
                if (alreadyFound.contains(key)) return;

                if (key == AuditLogKey.GUILD_AFK_CHANNEL) {
                    //TODO: Get AFK channel even if its been deleted
                    //guild.getManager().setAfkChannel()
                } else if (key == AuditLogKey.GUILD_AFK_TIMEOUT) {
                    //TODO: Test
                    guild.getManager().setAfkTimeout(Guild.Timeout.fromKey(l.getChangeByKey(key).getOldValue())).queue();
                } else if (key == AuditLogKey.GUILD_EXPLICIT_CONTENT_FILTER) {
                    //TODO: Test
                    guild.getManager().setExplicitContentLevel(Guild.ExplicitContentLevel.valueOf(l.getChangeByKey(key).getOldValue())).queue();
                } else if (key == AuditLogKey.GUILD_ICON) {

                } else if (key == AuditLogKey.GUILD_MFA_LEVEL) {

                } else if (key == AuditLogKey.GUILD_NAME) {

                } else if (key == AuditLogKey.GUILD_NOTIFICATION_LEVEL) {

                } else if (key == AuditLogKey.GUILD_REGION) {

                } else if (key == AuditLogKey.GUILD_SPLASH) {

                } else if (key == AuditLogKey.GUILD_SYSTEM_CHANNEL) {

                }

                alreadyFound.add(key);
            });
        });

    }

}
