package backup.restorables;

import backup.Backup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.List;

public class RestorableRole extends Restorable {

    private long originalId;
    private String name;
    private Color color;
    private boolean hoisted;
    private boolean mentionable;
    private int roleOrder;
    private long permissions;

    private long newId;

    public RestorableRole(Guild guild, boolean deleted, Timestamp lastModified) {
        super(guild, deleted, lastModified);
    }

    @Override
    public void restore(long time, List<AuditLogEntry> logs) {

        if (this.getRestoreType(time) == RestoreType.RESTORE) {
            Role newRole = guild.createRole()
                    .setName(name)
                    .setColor(color)
                    .setHoisted(hoisted)
                    .setPermissions(permissions)
                    .setMentionable(mentionable).
                            complete();
            newId = newRole.getIdLong();
            guild.modifyRolePositions().selectPosition(this.roleOrder).complete();
        }

        else if (this.getRestoreType(time) == RestoreType.EDIT) {

        }


    }

}
