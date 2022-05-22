package dev.aseef.backup.wrappers.restorables;

import lombok.Getter;

import javax.annotation.Nullable;
import java.sql.Timestamp;

@Getter
public class RestorableBan extends Restorable {

    /*
    Strategy:
    On initial backup, the timestamp for all bans will be the same as the timestamp for the initial backup.
    From there onwards, timestamps will be kept updated on the go.
     */

    private final long userId;
    private final String banReason;
    private Timestamp creation;
    /**
     * Time the ban was removed; null value indicates an active ban
     */
    private @Nullable Timestamp removeTime;

    public RestorableBan(int guild, Timestamp timestamp, long userId, String banReason) {
        super(guild, timestamp);
        this.userId = userId;
        this.banReason = banReason;
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
