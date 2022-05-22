package dev.aseef.backup.wrappers.restorables;

import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nullable;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class RestorableRole extends Restorable {

    private long originalId;
    private @Nullable long currentId;
    private String name;
    private Color color;
    private boolean hoisted;
    private boolean mentionable;
    private int roleOrder;
    private long permissions;

    private long newId;

    public RestorableRole(int guild, Timestamp timestamp) {
        super(guild, timestamp);
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
