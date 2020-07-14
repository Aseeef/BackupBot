package restorables;

import net.dv8tion.jda.api.entities.Guild;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

public abstract class Restorable {

    Guild guild;
    boolean deleted;
    Timestamp deletionTime;

    public Restorable(Guild guild, boolean deleted, Timestamp deletionTime) {
        this.guild = guild;
        this.deleted = deleted;
        this.deletionTime = deletionTime;
    }

    public boolean isRestorable(long time) {
        return this.deleted && this.deletionTime.toInstant().toEpochMilli() < time;
    }

    /** Restore this restorable object if it was deleted after @param long time.
     * If this object wasn't deleted at all, it will be restored regardless.
     *
     * @return - Returns a completable future completed when the action is complete
     */
    public abstract void restore(long time);

}
