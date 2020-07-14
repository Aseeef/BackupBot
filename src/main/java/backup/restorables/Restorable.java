package backup.restorables;

import net.dv8tion.jda.api.entities.Guild;

import java.sql.Timestamp;

public abstract class Restorable {

    Guild guild;
    private boolean deleted;
    private Timestamp lastModified;

    public Restorable(Guild guild, boolean deleted, Timestamp lastModified) {
        this.guild = guild;
        this.deleted = deleted;
        this.lastModified = lastModified;
    }

    /** Get what kind of restoration is required for this object */
    public RestoreType getRestoreType(long time) {
        if (this.deleted && this.lastModified.toInstant().toEpochMilli() < time)
            return RestoreType.RESTORE;
        else if (this.lastModified.toInstant().toEpochMilli() < time)
            return RestoreType.EDIT;
        else return RestoreType.NONE;
    }

    public enum RestoreType {
        /** This restore type indicates that this object needs to be fully re-created and restored */
        RESTORE,
        /** This restore type indicates that this object needs to be edited to be restored */
        EDIT,
        /** This restore type indicates that no action is required */
        NONE,
    }

    /** Restore this restorable object if it was deleted after @param long time.
     * If this object wasn't deleted at all, it will be restored regardless.
     *
     * @return - Returns a completable future completed when the action is complete
     */
    public abstract void restore(long time);

}
