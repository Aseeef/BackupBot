package dev.aseef.backup.wrappers.restorables;

import java.sql.Timestamp;

public class RestorableTextChannel extends Restorable {

    public RestorableTextChannel(int guildIndex, Timestamp timestamp) {
        super(guildIndex, timestamp);
    }

    @Override
    protected RestoreType determineRestoreType() {
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
