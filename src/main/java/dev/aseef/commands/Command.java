package dev.aseef.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import dev.aseef.config.Config;
import dev.aseef.Main;

public abstract class Command extends ListenerAdapter {

    private String command;
    private Permission permission;
    private Type type;

    public Command(String command, Permission permission, Type type) {
        this.command = command;
        this.permission = permission;
        this.type = type;
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        User user = e.getAuthor();
        String[] args = getArgs(msg);
        PrivateChannel channel = e.getChannel();

        Main.getJDA().getGuilds().get(0).retrieveMember(user).queue( (member -> {
            if (isCommand(msg, user, this.command)) {

                System.out.println("User "+ e.getAuthor().getAsTag()+
                        "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());

                // Check perms
                if ((permission != null && !member.hasPermission(permission)) || member.isOwner()) {
                    if (permission != null)
                        channel.sendMessage("**Sorry but you must have the permission " + this.permission.getName() + " to use this command!**").queue();
                    else channel.sendMessage("**Sorry but only the guild owner can use this command!**").queue();
                    return;
                }

                // Check type
                if (type == Type.DISCORD_ONLY) {
                    channel.sendMessage("**Sorry but this command can only be executed on the Server discord!**").queue();
                    return;
                }

                onCommandUse(e.getMessage(), member, channel, args);

            }
        }));

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        String[] args = getArgs(msg);
        TextChannel channel = e.getChannel();

        if (member != null && isCommand(msg, member.getUser(), this.command)) {

            System.out.println("User "+ e.getAuthor().getAsTag()+
                    "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());

            // Check perms
            if ((permission != null && !member.hasPermission(permission)) || member.isOwner()) {
                if (permission != null)
                    channel.sendMessage("**Sorry but you must have the permission " + this.permission.getName() + " to use this command!**").queue();
                else channel.sendMessage("**Sorry but only the guild owner can use this command!**").queue();
                return;
            }

            // Check type
            if (type == Type.DMS_ONLY) {
                channel.sendMessage("**Sorry but this command can only be executed in direct messages with me!**").queue();
                return;
            }

            onCommandUse(e.getMessage(), member, channel, args);

        }

    }

    /** This is the logic that occurs when this command is used
     * Note: To use TextChannel or PrivateChannel methods, use casting
     */
    public abstract void onCommandUse(Message message, Member member, MessageChannel channel, String[] args);

    public enum Type {
        /** Commands of this type can be executed anywhere by the user */
        ANYWHERE,
        /** Commands of this type can only be executed in the bot's direct messages */
        DMS_ONLY,
        /** Commands of this type can only be executed in the Server discord */
        DISCORD_ONLY,
    }

    public static String[] getArgs(String msg) {
        if (msg.split(" ").length == 1) return new String[0];
        else return msg.replaceFirst(Config.get().getCommandPrefix() + "[^ ]+ ", "").split(" ");
    }

    // Checks if its is a specific command
    public static boolean isCommand(String msg, User user, String command) {
        String[] args = msg.toLowerCase().split(" ");
        return args[0].equals(Config.get().getCommandPrefix() + command.toLowerCase()) &&
                !user.isBot();
    }

    // Checks if its any command
    public static boolean isCommand(String msg, User user) {
        // If the user is a bot, its not a command
        if (user.isBot())
            return false;
        else return msg.toLowerCase().startsWith(Config.get().getCommandPrefix());
    }
}
