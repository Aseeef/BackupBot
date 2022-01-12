package dev.aseef.backup.restorables;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import dev.aseef.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestorableEmote extends Restorable {

    private String emoteId;
    private String emoteName;

    public RestorableEmote(Guild guild, String emoteId, String emoteName, boolean deleted, Timestamp deletionTime) {
        super(guild, deleted, deletionTime);
        this.emoteId = emoteId;
        this.emoteName = emoteName;
    }

    @Override
    /**
     * @param List<AuditLogEntry> logs - The logs MUST be provided in chronological order from oldest to latest.
     */
    public void restore(long time, List<AuditLogEntry> logs) {
        if (this.getRestoreType(time) == RestoreType.RESTORE) {
            Optional<File> file = Utils.getImageFile("emotes", emoteId);
            try {
                if (file.isPresent())
                    this.guild.createEmote(emoteName, Icon.from(file.get())).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.getRestoreType(time) == RestoreType.EDIT) {
            logs = logs.stream().filter( (l) -> l.getType() == ActionType.EMOTE_UPDATE).collect(Collectors.toList());
            Optional<AuditLogEntry> optionalLog = logs.stream().filter( l -> l.getTimeCreated().toInstant().toEpochMilli() > time).findFirst();

            if (optionalLog.isPresent() && optionalLog.get().getChanges().containsKey("name")) {
                AuditLogEntry log = optionalLog.get();
                String oldName = log.getChanges().get("name").getOldValue().toString();
                this.guild.retrieveEmoteById(this.emoteId).complete().getManager().setName(oldName).queue();
                //TODO update this data in the database But make a dev.aseef.backup.?
            }
        }
    }
}
