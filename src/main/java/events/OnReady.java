package events;

import backup.Backup;
import bot.Backups;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import org.jetbrains.annotations.NotNull;
import utils.Serializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OnReady extends ListenerAdapter {

    public void onReady (@NotNull ReadyEvent event) {

        // register events
        Backups.getJda().addEventListener(new MessageEvents());

        Guild guild = Backups.getJda().getGuildById(315652293410422791L);
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
                .thenCompose(backup::getAndSaveMessages)
                .thenCompose(v -> backup.getAuditLogs())
                .thenCompose(backup::saveAuditLogs);

         long start = System.currentTimeMillis();
         backup.getAuditLogsFromDb()
                 .thenAccept( logs -> {
                     System.out.println("Total entries: " + logs.size());
                     System.out.println("Completed in: " + (System.currentTimeMillis() - start));

                     logs.forEach( (l) -> {
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
