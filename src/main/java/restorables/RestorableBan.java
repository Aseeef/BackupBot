package restorables;

import net.dv8tion.jda.api.entities.Guild;
import utils.Utils;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

public class RestorableBan extends Restorable {

    private long userId;
    private String banReason;

    public RestorableBan(Guild guild, long userId, String banReason, boolean deleted, Timestamp deletionTime) {
        super(guild, deleted, deletionTime);
        this.userId = userId;
        this.banReason = banReason;
    }

    public long getUserId() {
        return userId;
    }

    public String getBanReason() {
        return banReason;
    }

    @Override
    public void restore(long time) {
        if (this.isRestorable(time)) {
            this.guild.ban(String.valueOf(userId), 0, banReason).queue();
        }
    }
}
