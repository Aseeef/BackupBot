package dev.aseef.backup.wrappers.restorables;

import lombok.Getter;

import javax.annotation.Nullable;
import java.sql.Timestamp;

@Getter
public class RestorableMember extends Restorable {

    private final int memberIndexId;
    private final long discordSnowflake;
    private final String nick;
    private final @Nullable LeaveReason leaveReason;

    public RestorableMember(int guildIndex, Timestamp timestamp, int memberIndexId, long discordSnowflake, String nick, @Nullable LeaveReason leaveReason) {
        super(guildIndex, timestamp);
        this.memberIndexId = memberIndexId;
        this.discordSnowflake = discordSnowflake;
        this.nick = nick;
        this.leaveReason = leaveReason;
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

    public enum LeaveReason {
        BANNED,
        KICKED,
        LEFT
    }
}
