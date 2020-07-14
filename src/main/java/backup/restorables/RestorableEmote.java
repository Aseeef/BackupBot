package backup.restorables;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

public class RestorableEmote extends Restorable {

    private String emoteId;
    private String emoteName;

    public RestorableEmote(Guild guild, String emoteId, String emoteName, boolean deleted, Timestamp deletionTime) {
        super(guild, deleted, deletionTime);
        this.emoteId = emoteId;
        this.emoteName = emoteName;
    }

    @Override
    public void restore(long time) {
        if (this.getRestoreType(time) == RestoreType.RESTORE) {
            Optional<File> file = Utils.getImageFile("emotes", emoteId);
            try {
                if (file.isPresent())
                    this.guild.createEmote(emoteName, Icon.from(file.get())).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.getRestoreType(time) == RestoreType.EDIT) {

        }
    }
}
