package events;

import backup.Backup;
import bot.Backups;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OnReady extends ListenerAdapter {

    public void onReady (@NotNull ReadyEvent event) {

        // register events
        Backups.getJda().addEventListener(new MessageEvents());

        Guild guild = Backups.getJda().getGuildById(410204006569213955L);
        if (guild == null) return;

        Backup backup = new Backup(guild);
                backup.init()
                .thenCompose(v -> backup.backupSettings())
                .thenCompose(v -> backup.backupMembers())
                .thenCompose(v -> backup.backupEmotes())
                .thenCompose(v -> backup.backupBanList())
                .thenCompose(v -> backup.backupRoles())
                .thenCompose(v -> backup.backupCategories())
                .thenCompose(v -> backup.backupVoiceChannels())
                .thenCompose(v -> backup.backupTextChannels())
                .thenCompose(backup::getAndSaveMessages);

    }

}
