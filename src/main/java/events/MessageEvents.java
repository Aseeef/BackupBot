package events;

import backup.Backup;
import bot.Backups;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import config.Config;
import org.jetbrains.annotations.NotNull;
import utils.Utils;

import javax.annotation.Nonnull;

/**
 * This class manages message related events which may require us to update the
 * database with new information about the message (e.g the message was edited).
 */
public class MessageEvents extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Utils.runAsync( () ->
                Backup.getInstance().backupMessages(event.getMessage()));
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Utils.runAsync( () ->
                Backup.getInstance().markMessageDeleted(event.getMessageIdLong(), System.currentTimeMillis()));
    }

    @Override
    public void onGuildMessageReactionRemoveAll (@NotNull GuildMessageReactionRemoveAllEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Utils.runAsync( () ->
                Backup.getInstance().backupMessages(
                        event.getChannel().retrieveMessageById(event.getMessageId()).complete()
                ));
    }

    @Override
    public void onGuildMessageReactionRemove (@NotNull GuildMessageReactionRemoveEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Utils.runAsync( () ->
                Backup.getInstance().backupMessages(
                        event.getChannel().retrieveMessageById(event.getMessageId()).complete()
                ));
    }

    @Override
    public void onGuildMessageReactionAdd (@NotNull GuildMessageReactionAddEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Utils.runAsync( () ->
                Backup.getInstance().backupMessages(
                        event.getChannel().retrieveMessageById(event.getMessageId()).complete()
                ));
    }

    @Override
    public void onGuildMessageUpdate (@NotNull GuildMessageUpdateEvent event) {
        if (Config.get().getBackupSettings().getBlacklistedChannels().contains(event.getChannel().getIdLong())) return;

        Utils.runAsync( () ->
                Backup.getInstance().backupMessages(
                        event.getChannel().retrieveMessageById(event.getMessageId()).complete()
                ));
    }

}