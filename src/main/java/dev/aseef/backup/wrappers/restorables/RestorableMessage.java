package dev.aseef.backup.wrappers.restorables;

import lombok.Getter;

import javax.annotation.Nullable;
import java.sql.Timestamp;

@Getter
public class RestorableMessage extends Restorable {

    private final long originalId;
    private @Nullable Long currentId;
    private final int channelIndexId;
    private final int memberIndexId;
    private final String message;
    private final @Nullable Integer attachmentIndex;

    public RestorableMessage(int guild, Timestamp timestamp, long originalId, @Nullable Long currentId, int channelIndexId, int memberIndexId, String message, @Nullable Integer attachmentIndex) {
        super(guild, timestamp);
        this.originalId = originalId;
        this.currentId = currentId;
        this.channelIndexId = channelIndexId;
        this.memberIndexId = memberIndexId;
        this.message = message;
        this.attachmentIndex = attachmentIndex;
    }

    public long getCreationTime() {
        return (originalId >> 22) + 1420070400000L;
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
