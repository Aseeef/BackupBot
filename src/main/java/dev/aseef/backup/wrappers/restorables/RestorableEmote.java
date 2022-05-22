package dev.aseef.backup.wrappers.restorables;

import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class RestorableEmote extends Restorable {

    private final String emoteId;
    private final String emoteName;

    public RestorableEmote(int guild, Timestamp timestamp, String emoteId, String emoteName) {
        super(guild, timestamp);
        this.emoteId = emoteId;
        this.emoteName = emoteName;
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
