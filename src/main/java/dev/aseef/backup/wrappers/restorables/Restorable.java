package dev.aseef.backup.wrappers.restorables;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Timestamp;

@Getter
public abstract class Restorable {

    Guild guild;
    private Timestamp timestamp;

    public Restorable(int guildIndex, Timestamp timestamp) {

    }

    /**
     * @return Determine the restoration type needed for this object
     */
    protected abstract RestoreType determineRestoreType();

    /**
     * @return Run an edit restore type. In this restore type, only an edit to an existing object is needed to complete the restore.
     */
    protected abstract boolean editRestore();

    /**
     * @return Run a restore of type RESTORE. This restore type create a clone of the restore object.
     */
    protected abstract boolean createNewRestore();

    /** Restore this restorable object if it was deleted after @param long time.
     * If this object wasn't deleted at all, it will be restored regardless.
     *
     * @return - Returns a completable future completed when the action is complete
     */
    public void restore() {
        RestoreType type = determineRestoreType();
        if (type == RestoreType.RESTORE) {
            createNewRestore();
        } else if (type == RestoreType.EDIT) {
            editRestore();
        }
    }

    public enum RestoreType {
        /** This restore type indicates that this object needs to be fully re-created and restored */
        RESTORE,
        /** This restore type indicates that this object needs to be edited to be restored */
        EDIT,
        /** This restore type indicates that no action is required */
        NONE,
    }

}
