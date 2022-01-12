package dev.aseef.backup.restorables;

import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Timestamp;
import java.util.List;

public class RestorableMessage extends Restorable {

    private long originalId;

    public RestorableMessage(Guild guild, boolean deleted, Timestamp lastModified) {
        super(guild, deleted, lastModified);
    }

    @Override
    public void restore(long time, List<AuditLogEntry> logs) {

        if (this.getRestoreType(time) == RestoreType.RESTORE) {

        }


    }

}
