package dev.aseef.events;

import dev.aseef.backup.Backup;
import dev.aseef.utils.threads.ThreadUtil;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import dev.aseef.config.Config;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * This class manages message related dev.aseef.events which may require us to update the
 * database with new information about the message (e.g the message was edited).
 */
public class MessageEvents extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Backup backup = Backup.getInstance(event.getGuild()).orElse(null);
        ThreadUtil.runAsync( () -> {
            if (backup != null)
                backup.backupMessages(event.getMessage());
            else System.out.println("Back up is null!");
        });
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Backup backup = Backup.getInstance(event.getGuild()).orElse(null);
        ThreadUtil.runAsync( () -> {
            if (backup != null)
                backup.markMessageDeleted(event.getMessageIdLong(), System.currentTimeMillis());
            else System.out.println("Back up is null!");
        });
    }

    @Override
    public void onGuildMessageReactionRemoveAll (@NotNull GuildMessageReactionRemoveAllEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Backup backup = Backup.getInstance(event.getGuild()).orElse(null);
        ThreadUtil.runAsync( () -> {
            if (backup != null)
                backup.backupMessages(event.getChannel().retrieveMessageById(event.getMessageId()).complete());
            else System.out.println("Back up is null!");
        });
    }

    @Override
    public void onGuildMessageReactionRemove (@NotNull GuildMessageReactionRemoveEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Backup backup = Backup.getInstance(event.getGuild()).orElse(null);
        ThreadUtil.runAsync( () -> {
            if (backup != null)
                backup.backupMessages(event.getChannel().retrieveMessageById(event.getMessageId()).complete());
            else System.out.println("Back up is null!");
        });
    }

    @Override
    public void onGuildMessageReactionAdd (@NotNull GuildMessageReactionAddEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Backup backup = Backup.getInstance(event.getGuild()).orElse(null);
        ThreadUtil.runAsync( () -> {
            if (backup != null)
                backup.backupMessages(event.getChannel().retrieveMessageById(event.getMessageId()).complete());
            else System.out.println("Back up is null!");
        });
    }

    @Override
    public void onGuildMessageUpdate (@NotNull GuildMessageUpdateEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Backup backup = Backup.getInstance(event.getGuild()).orElse(null);
        ThreadUtil.runAsync( () -> {
            if (backup != null)
                backup.backupMessages(event.getMessage());
            else System.out.println("Back up is null!");
        });
    }

}
