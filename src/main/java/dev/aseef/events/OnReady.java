package dev.aseef.events;

import dev.aseef.backup.Backup;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import dev.aseef.Main;
import dev.aseef.utils.Serializer;

public class OnReady extends ListenerAdapter {

    public void onReady (@NotNull ReadyEvent event) {

        Guild guild = Main.getJDA().getGuildById(315652293410422791L);
        if (guild == null) return;

        Backup backup = new Backup(guild);
        Main.getJDA().addEventListener(new MessageEvents());
        backup.backupSettings()
                .thenCompose(v -> backup.backupMembers())
                .thenCompose(v -> backup.backupEmotes())
                .thenCompose(v -> backup.backupBanList())
                .thenCompose(v -> backup.backupRoles())
                .thenCompose(v -> backup.backupCategories())
                .thenCompose(v -> backup.backupVoiceChannels())
                .thenCompose(v -> backup.backupTextChannels())
                .thenCompose(backup::getAndSaveMessages)
                .thenCompose(v -> backup.getAuditLogs())
                .thenCompose(backup::saveAuditLogs)
                .thenCompose(backup::updateMessagesWithLogs);

        long start = System.currentTimeMillis();
        backup.getAuditLogsFromDb()
                .thenAccept(logs -> {
                    System.out.println("Total entries: " + logs.size());
                    System.out.println("Completed in: " + (System.currentTimeMillis() - start));

                    logs.forEach((l) -> {
                        System.out.println("Action Id: " + l.getIdLong());
                        System.out.println("Action Type: " + l.getType());
                        System.out.println("Target Id: " + l.getTargetIdLong());
                        System.out.println("Guild Id: " + l.getGuild().getIdLong());
                        System.out.println("User Id: " + l.getUser());
                        System.out.println("Reason: " + l.getReason());
                        System.out.println("Changes: " + Serializer.serializeLogChanges(l.getChanges()));
                        System.out.println("Options: " + Serializer.serializeLogOptions(l.getOptions()));
                        System.out.println("Creation: " + l.getTimeCreated());
                        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
                    });
                });


    }

}
