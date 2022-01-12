package dev.aseef.backup;

import dev.aseef.config.Config;
import dev.aseef.utils.threads.ThreadUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import dev.aseef.sql.BackupDB;
import dev.aseef.Main;
import dev.aseef.utils.Serializer;
import dev.aseef.utils.Utils;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Backup {

    private static List<Backup> instances = new ArrayList<>();

    private Guild guild;
    private BackupDB db;

    // Long = TextChannel id
    // List<Long> = time taken
    private ConcurrentMap<String, List<Long>> etaCalculationMap = new ConcurrentHashMap<>();

    /**
     * @param guild - The guild which to create a back up of.
     */
    public Backup(Guild guild) {
        this.guild = guild;
        this.db = new BackupDB(guild, System.currentTimeMillis());
        instances.add(this);
    }

    public static Optional<Backup> getInstance(Guild guild) {
        return instances.stream().filter( (instance) -> instance.guild.getIdLong() == guild.getIdLong()).findFirst();
    }

    public CompletableFuture<Void> backupSettings() {
        CompletableFuture<Void> futureCompletion = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupServerSettings() &&
                Config.get().getEnabledBackups().isBackupChannels())
            ThreadUtil.runAsync(() -> {

                System.out.println("Saving and updating server settings to the database...");
                long start = System.currentTimeMillis();

                try (Connection conn = this.db.getInstance().getConnection()) {

                    String query = "INSERT OR REPLACE INTO settings (guild_id, guild_name, guild_description, afk_channel_id, default_channel_id, system_channel_id, afk_timeout_seconds, default_notif_level, explicit_content_level, region, verification_level)  VALUES  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setLong(1, guild.getIdLong());
                        ps.setString(2, guild.getName());
                        ps.setString(3, guild.getDescription());
                        long afkChannelId = guild.getAfkChannel() == null ? -1 : guild.getAfkChannel().getIdLong();
                        ps.setLong(4, afkChannelId);
                        long defaultChannelId = guild.getDefaultChannel() == null ? -1 : guild.getDefaultChannel().getIdLong();
                        ps.setLong(5, defaultChannelId);
                        long systemChannelId = guild.getSystemChannel() == null ? -1 : guild.getSystemChannel().getIdLong();
                        ps.setLong(6, systemChannelId);
                        ps.setInt(7, guild.getAfkTimeout().getSeconds());
                        ps.setString(8, guild.getDefaultNotificationLevel().toString());
                        ps.setString(9, guild.getExplicitContentLevel().toString());
                        ps.setString(10, guild.getRegion().toString());
                        ps.setString(11, guild.getVerificationLevel().toString());

                        // save icon and banner
                        if (guild.getIconUrl() != null)
                            Utils.saveImage(guild.getIconUrl(), "server", "icon");
                        if (guild.getBannerUrl() != null)
                            Utils.saveImage(guild.getBannerUrl(), "server", "banner");
                        if (guild.getSplashUrl() != null)
                            Utils.saveImage(guild.getSplashUrl(), "server", "splash");

                        ps.executeUpdate();
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                System.out.println("Successfully saved server settings to the database in " + (System.currentTimeMillis() - start) + " ms!");

                futureCompletion.complete(null);
            });
        else {
            System.out.println("Skipping dev.aseef.backup for Server settings because server settings dev.aseef.backup or one of its dependencies are disabled...");
            futureCompletion.complete(null);
        }

        return futureCompletion;
    }

    public CompletableFuture<List<Member>> backupMembers() {
        CompletableFuture<List<Member>> futureMembers = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupMembers() &&
                Config.get().getEnabledBackups().isBackupRoles())
            ThreadUtil.runAsync(() -> {
                System.out.println("Saving and updating all members to the database...");
                guild.loadMembers().onSuccess((members) -> {
                    int i = 0;

                    long start = System.currentTimeMillis();
                    for (Member member : members) {

                        try (Connection conn = this.db.getInstance().getConnection()) {

                            String query = "INSERT OR REPLACE INTO members (member_id, member_nick, member_roles) VALUES (?, ?, ?);";

                            try (PreparedStatement ps = conn.prepareStatement(query)) {
                                ps.setLong(1, member.getIdLong());
                                ps.setString(2, member.getEffectiveName());
                                ps.setString(3, member.getRoles().toString());

                                ps.executeUpdate();
                            }

                            if (i != 0 && i % 100 == 0) {
                                long timeleft = ((members.size() - i) * 10) / 1000;
                                System.out.println("Saved and updated " + i + " / " + members.size() + " members to the database...  [ETA: " + timeleft + " seconds left]");
                            }
                            i++;

                        } catch (SQLException e) {
                            e.printStackTrace();
                            return;
                        }

                    }
                    System.out.println("Successfully saved and updated all members to the database in " + (System.currentTimeMillis() - start) + " ms!");

                    futureMembers.complete(members);
                });
            });
        else {
            System.out.println("Skipping dev.aseef.backup for Members because member dev.aseef.backup or one of its dependencies are disabled...");
            futureMembers.complete(null);
        }

        return futureMembers;
    }

    public CompletableFuture<List<Emote>> backupEmotes() {
        CompletableFuture<List<Emote>> futureEmotes = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupEmojis())
            ThreadUtil.runAsync(() -> {
                List<Emote> emotes = guild.getEmotes();

                System.out.println("Saving server emotes to the dev.aseef.backup...");
                long start = System.currentTimeMillis();
                for (Emote emote : emotes) {

                    try (Connection conn = this.db.getInstance().getConnection()) {

                        String query = "INSERT OR REPLACE INTO emotes (emote_id, emote_name, creation) VALUES (?, ?, ?);";

                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setLong(1, emote.getIdLong());
                            ps.setString(2, emote.getName());
                            ps.setTimestamp(3, new Timestamp(emote.getTimeCreated().toInstant().toEpochMilli()));
                            Utils.saveImage(emote.getImageUrl(), "emotes", emote.getId());
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }

                }

                System.out.println("Successfully backed up all server emotes to the database in " + (System.currentTimeMillis() - start) + " ms!");

                futureEmotes.complete(emotes);
            });
        else {
            System.out.println("Skipping dev.aseef.backup for emotes because emoji dev.aseef.backup is disabled!");
            futureEmotes.complete(null);
        }

        return futureEmotes;
    }

    public CompletableFuture<List<Guild.Ban>> backupBanList() {
        CompletableFuture<List<Guild.Ban>> futureBannedMembers = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupBans())
            ThreadUtil.runAsync(() -> {
                List<Guild.Ban> bans = guild.retrieveBanList().complete();

                System.out.println("Saving server bans to the database...");

                int i = 0;
                long start = System.currentTimeMillis();
                etaCalculationMap.putIfAbsent("bans", new ArrayList<>());
                for (Guild.Ban ban : bans) {
                    long start1 = System.currentTimeMillis();

                    try (Connection conn = this.db.getInstance().getConnection()) {
                        String query = "INSERT OR REPLACE INTO bans (user_id, user_tag, ban_reason) VALUES (?, ?, ?);";

                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setLong(1, ban.getUser().getIdLong());
                            ps.setString(2, ban.getUser().getAsTag());
                            ps.setString(3, ban.getReason());
                            ps.executeUpdate();
                        }

                        long end = System.currentTimeMillis() - start1;
                        List<Long> timeValues = etaCalculationMap.get("bans");
                        timeValues.add(end);
                        System.out.println(end);

                        if (i != 0 && i % 100 == 0) {
                            float avg = Utils.calculateListAvg(timeValues);
                            System.out.println(avg);
                            long timeleft = (long) (((bans.size() - i) * avg) / 1000);
                            System.out.println("Saved " + i + " / " + bans.size() + " server bans to the database... [ETA: " + timeleft + " seconds left]");
                        }
                        i++;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                System.out.println("Successfully backed up server bans in " + (System.currentTimeMillis() - start) + " ms!");

                futureBannedMembers.complete(bans);
            });
        else {
            System.out.println("Skipping dev.aseef.backup for bans because they are disabled!");
            futureBannedMembers.complete(null);
        }

        return futureBannedMembers;
    }

    public CompletableFuture<List<Role>> backupRoles() {
        CompletableFuture<List<Role>> futureRoles = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupRoles())
            ThreadUtil.runAsync(() -> {
                List<Role> roles = guild.getRoles();

                System.out.println("Saving " + roles.size() + " server emotes to the database...");

                long start = System.currentTimeMillis();
                for (Role role : roles) {

                    try (Connection conn = this.db.getInstance().getConnection()) {

                        String query = "INSERT OR REPLACE INTO roles (role_id, role_name, role_color, hoisted, mentionable, role_order, permissions, creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setLong(1, role.getIdLong());
                            ps.setString(2, role.getName());
                            ps.setInt(3, role.getColorRaw());
                            ps.setBoolean(4, role.isHoisted());
                            ps.setBoolean(5, role.isMentionable());
                            ps.setInt(6, role.getPosition());
                            ps.setLong(7, Permission.getRaw(role.getPermissions()));
                            ps.setTimestamp(8, new Timestamp(role.getTimeCreated().toInstant().toEpochMilli()));
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }

                    System.out.println("Saved server role " + role.getName() + " to the database...");
                }
                System.out.println("Successfully backed up all server roles in " + (System.currentTimeMillis() - start) + " ms!");

                futureRoles.complete(roles);
            });
        else {
            System.out.println("Skipping dev.aseef.backup for roles because they are disabled!");
            futureRoles.complete(null);
        }

        return futureRoles;

    }

    public CompletableFuture<List<Category>> backupCategories() {
        CompletableFuture<List<Category>> futureCategories = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupChannels() &&
                Config.get().getEnabledBackups().isBackupRoles())
            ThreadUtil.runAsync(() -> {
                List<Category> categories = guild.getCategories();

                System.out.println("Saving " + categories.size() + " categories to the database...");
                long start = System.currentTimeMillis();
                for (Category category : categories) {

                    try (Connection conn = this.db.getInstance().getConnection()) {

                        String query = "INSERT OR REPLACE INTO categories (category_id, category_name, permissions, creation) VALUES (?, ?, ?, ?);";

                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setLong(1, category.getIdLong());
                            ps.setString(2, category.getName());
                            ps.setString(3, Serializer.serializePermOverrides(category.getPermissionOverrides()));
                            ps.setTimestamp(4, new Timestamp(category.getTimeCreated().toInstant().toEpochMilli()));
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }

                    System.out.println("Saved category " + category.getName() + " to the database...");

                }
                System.out.println("Successfully backed up all channel categories to the database in " + (System.currentTimeMillis() - start) + " ms!");

                futureCategories.complete(categories);

            });
        else {
            System.out.println("Skipping dev.aseef.backup for categories because channel dev.aseef.backup or one of its dependencies is disabled!");
            futureCategories.complete(null);
        }

        return futureCategories;
    }

    public CompletableFuture<List<VoiceChannel>> backupVoiceChannels() {
        CompletableFuture<List<VoiceChannel>> futureVoiceChannels = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupChannels() &&
                Config.get().getEnabledBackups().isBackupRoles())
            ThreadUtil.runAsync(() -> {
                List<VoiceChannel> voiceChannels = guild.getVoiceChannels();

                System.out.println("Saving " + voiceChannels.size() + " voice channels to the database...");
                long start = System.currentTimeMillis();
                for (VoiceChannel channel : voiceChannels) {

                    try (Connection conn = this.db.getInstance().getConnection()) {
                        String query = "INSERT OR REPLACE INTO voicechannels (channel_id, category_id, channel_name, bitrate, user_limit, permission, creation) VALUES (?, ?, ?, ?, ?, ?, ?);";

                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setLong(1, channel.getIdLong());
                            boolean inCategory = channel.getParent() != null;
                            ps.setLong(2, inCategory ? channel.getParent().getIdLong() : -1);
                            ps.setString(3, channel.getName());
                            ps.setInt(4, channel.getBitrate());
                            ps.setInt(5, channel.getUserLimit());
                            ps.setString(6, Serializer.serializePermOverrides(channel.getPermissionOverrides()));
                            ps.setTimestamp(7, new Timestamp(channel.getTimeCreated().toInstant().toEpochMilli()));
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }

                    System.out.println("Saved voice channel " + channel.getName() + " to the database...");

                }

                System.out.println("Successfully backed up all voice channel to the database in " + (System.currentTimeMillis() - start) + " ms!");

                futureVoiceChannels.complete(voiceChannels);
            });
        else {
            System.out.println("Skipping dev.aseef.backup for voice channels because channel dev.aseef.backup or one of its dependencies is disabled!");
            futureVoiceChannels.complete(null);
        }


        return futureVoiceChannels;
    }

    public CompletableFuture<List<TextChannel>> backupTextChannels() {
        CompletableFuture<List<TextChannel>> futureTextChannels = new CompletableFuture<>();

        if (Config.get().getEnabledBackups().isBackupChannels() &&
                Config.get().getEnabledBackups().isBackupRoles())
            ThreadUtil.runAsync(() -> {
                List<TextChannel> textChannels = guild.getTextChannels();

                System.out.println("Saving " + textChannels.size() + " voice channels to the database...");
                long start = System.currentTimeMillis();
                for (TextChannel channel : textChannels) {

                    try (Connection conn = this.db.getInstance().getConnection()) {

                        String query = "INSERT OR REPLACE INTO `textchannels` (channel_id, category_id, channel_name, channel_topic, permissions, slow_mode, creation) VALUES (?, ?, ?, ?, ?, ?, ?);";

                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setLong(1, channel.getIdLong());
                            boolean inCategory = channel.getParent() != null;
                            ps.setLong(2, inCategory ? channel.getParent().getIdLong() : -1);
                            ps.setString(3, channel.getName());
                            ps.setString(4, channel.getTopic());
                            ps.setString(5, Serializer.serializePermOverrides(channel.getPermissionOverrides()));
                            ps.setInt(6, channel.getSlowmode());
                            ps.setTimestamp(7, new Timestamp(channel.getTimeCreated().toInstant().toEpochMilli()));
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }
                    System.out.println("Saved text channel " + channel.getName() + " to the database...");

                }

                System.out.println("Successfully backed up all text channel to the database in " + (System.currentTimeMillis() - start) + " ms!");

                futureTextChannels.complete(textChannels);

            });
        else {
            System.out.println("Skipping dev.aseef.backup for text channels because channel dev.aseef.backup or one of its dependencies is disabled!");
            futureTextChannels.complete(null);
        }


        return futureTextChannels;
    }

    public CompletableFuture<Void> getAndSaveMessages(List<TextChannel> channels) {
        CompletableFuture<Void> futureMessages = new CompletableFuture<>();


        if (Config.get().getEnabledBackups().isBackupChannels() &&
                Config.get().getEnabledBackups().isBackupRoles() &&
                Config.get().getEnabledBackups().isBackupEmojis() &&
                Config.get().getEnabledBackups().isBackupMessages() &&
                channels != null) {

            System.out.println("Starting backups for channel messages to the database...");
            long start = System.currentTimeMillis();

            // program uses this counter to keep track when the backups are done
            final int channelCount = channels.size();
            AtomicInteger counter = new AtomicInteger(0);

            for (TextChannel channel : channels) {

                if (Config.get().getBackupSettings().getBlacklistedChannels().contains(channel.getIdLong())) {
                    System.out.println("Skipping messages dev.aseef.backup for Text channel #" + channel.getName() + " because it is black listed in the dev.aseef.config.");
                    continue;
                }

                //TODO: Max thread option
                ExecutorService executor = Executors.newFixedThreadPool(4);
                executor.submit(() -> {
                    List<Message> messages = new ArrayList<>();

                    long start1 = System.currentTimeMillis();

                    System.out.println("Loading all messages for text channel #" + channel.getName() + ". This may take a while for large channels...");
                    channel.getIterableHistory().forEach(messages::add);
                    // order messages from oldest to newest so if the dev.aseef.backup stops, it would continue right where it left
                    Collections.reverse(messages);
                    System.out.println("Successfully loaded " + (messages.size()) + " messages from text channel #" + channel.getName() + " in " + (System.currentTimeMillis() - start1) + " ms!");

                    Timestamp lastSavedTimestamp = this.getLastSavedMessageTime(channel);
                    if (lastSavedTimestamp != null) {
                        OffsetDateTime lastSavedMsg = OffsetDateTime.ofInstant(lastSavedTimestamp.toInstant(), ZoneId.systemDefault());
                        // TODO: Add support for -1 to dev.aseef.config option messageUpdateTime
                        OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() - 1000 * 60 * 60 * Config.get().getBackupSettings().getMessageUpdateTime()), ZoneId.systemDefault());
                        time = lastSavedMsg.isBefore(time) ? lastSavedMsg : time;
                        OffsetDateTime finalTime = time;
                        messages.removeIf(msg -> msg.getTimeCreated().isBefore(finalTime));
                        System.out.println("Inserting and updating " + (messages.size()) + " messages from text channel #" + channel.getName() + "!");
                    }


                    System.out.println("Saving messages to database...");
                    long start2 = System.currentTimeMillis();
                    backupMessages(messages);
                    System.out.println("Successfully backed up text channel #" + channel.getName() + " in " + (System.currentTimeMillis() - start2) + " ms!");

                    if (counter.addAndGet(1) == channelCount) {
                        System.out.println("Successfully backed up all text messages for all channels in " + (System.currentTimeMillis() - start) + " ms!");
                        futureMessages.complete(null);
                    }
                });

            }

        }
        else {
            System.out.println("Skipping dev.aseef.backup for messages because message dev.aseef.backup or one of its dependencies is disabled!");
            futureMessages.complete(null);
        }

        return futureMessages;
    }

    public void backupMessages (Message message) {
        backupMessages(Collections.singletonList(message));
    }

    public void backupMessages(List<Message> messages) {

        // Note: The for loop is outside the the get connection so the message backups won't freeze dp writes (because this allows for connection to be released every msg)
        int i = 0;
        for (Message message : messages) {
            TextChannel channel = message.getTextChannel();

            String serializedEmbed = Serializer.serializeEmbeds(message.getEmbeds());
            // TODO: Welcome messages from system channel don't get any content copied : TEST FIX ->
            // don't save empty msgs
            if (message.getContentRaw().equalsIgnoreCase("") && serializedEmbed.equalsIgnoreCase("")) return;

            long start = System.currentTimeMillis();
            try (Connection conn = this.db.getMsgDb(channel).getConnection()) {

                String query = "INSERT OR REPLACE INTO `messages` (message_id, creation, member_avatar, member_name, message_content, reactions, pinned, embeds, attachment_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

                try (PreparedStatement ps = conn.prepareStatement(query)) {

                    ps.setLong(1, message.getIdLong());
                    ps.setTimestamp(2, new Timestamp(message.getTimeCreated().toInstant().toEpochMilli()));
                    ps.setString(3, message.getAuthor().getAvatarUrl());
                    String memberName = message.getMember() == null ? message.getAuthor().getName() : message.getMember().getEffectiveName();
                    ps.setString(4, memberName);
                    ps.setString(5, message.getContentRaw());
                    ps.setString(6, Serializer.serializeReactions(message.getReactions()));
                    ps.setBoolean(7, message.isPinned());
                    ps.setString(8, serializedEmbed);

                    boolean attachments = message.getAttachments().size() > 0;
                    ps.setLong(9, attachments ? Utils.saveAttachment(message.getAttachments().get(0)) : -1);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                long end = System.currentTimeMillis() - start;
                etaCalculationMap.putIfAbsent(channel.getId(), new ArrayList<>());
                List<Long> timeValues = etaCalculationMap.get(channel.getId());
                timeValues.add(end);

                if (i != 0 && i % 100 == 0) {
                    float avg = Utils.calculateListAvg(timeValues);

                    long timeleft = (long) (((messages.size() - i) * avg) / 1000);
                    System.out.println("Saved " + i + " / " + messages.size() + " from #" + message.getChannel().getName() + " to the database... [ETA: " + timeleft + " seconds left]");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            i++;
        }

    }


    public CompletableFuture<List<AuditLogEntry>> getAuditLogs() {

        CompletableFuture<List<AuditLogEntry>> futureAuditLogs = new CompletableFuture<>();

        ThreadUtil.runAsync( () -> {
            // get audit logs
            PaginationAction.PaginationIterator<AuditLogEntry> logsIter = guild.retrieveAuditLogs().iterator();
            List<AuditLogEntry> logs = new ArrayList<>();
            while (logsIter.hasNext()) {
                AuditLogEntry log = logsIter.next();

                // TODO: Add dev.aseef.config to change this
                OffsetDateTime logLimit = OffsetDateTime.ofInstant(Instant.now().minusSeconds(60*60*24*3), ZoneId.systemDefault());
                Timestamp lastLogTimestamp = this.getLastLogTime();
                OffsetDateTime lastLog = lastLogTimestamp == null ? null : OffsetDateTime.ofInstant(lastLogTimestamp.toInstant(), ZoneId.systemDefault());
                // works since the iterator goes from newest log to oldest log
                if (log.getTimeCreated().isBefore(logLimit) || (lastLog != null && log.getTimeCreated().isBefore(lastLog))) {
                    break;
                }

                // don't add logs for useless types...
                if (log.getType() == ActionType.MEMBER_VOICE_MOVE || log.getType() == ActionType.MEMBER_VOICE_KICK || log.getType() == ActionType.INVITE_CREATE || log.getType() == ActionType.INVITE_DELETE)
                    continue;

                logs.add(log);
            }

            futureAuditLogs.complete(logs);
        });


        return futureAuditLogs;
    }

    public CompletableFuture<List<AuditLogEntry> > saveAuditLogs (List<AuditLogEntry> logs) {
        CompletableFuture<List<AuditLogEntry> > futureLogs = new CompletableFuture<>();

        ThreadUtil.runAsync( () -> {
            System.out.println("Saving audit logs...");
            int i = 0;
            for (AuditLogEntry log : logs) {
                try (Connection conn = this.db.getInstance().getConnection()) {

                    String query = "INSERT OR REPLACE INTO audit_logs (action_id, action_type, target_id, guild_id, user_id, reason, changes, options, creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setLong(1, log.getIdLong());
                        ps.setInt(2, log.getTypeRaw());
                        ps.setLong(3, log.getTargetIdLong());
                        ps.setLong(4, guild.getIdLong());
                        ps.setLong(5, log.getUser() == null ? -1 : log.getUser().getIdLong());
                        ps.setString(6, log.getReason());
                        ps.setString(7, Serializer.serializeLogChanges(log.getChanges()));
                        ps.setString(8, Serializer.serializeLogOptions(log.getOptions()));
                        ps.setTimestamp(9, new Timestamp(log.getTimeCreated().toInstant().toEpochMilli()));


                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (i != 0 && i % 100 == 0) {
                        long timeleft = (long) (((logs.size() - i) * 13.5) / 1000);
                        System.out.println("Saved " + i + " / " + logs.size() + " log entries to the database... [ETA: " + timeleft + " seconds left]");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                i++;
            }

            futureLogs.complete(logs);
        });

        return futureLogs;
    }

    public CompletableFuture<Void> updateMessagesWithLogs(List<AuditLogEntry> logs) {
        CompletableFuture<Void> futureCompletion = new CompletableFuture<>();

        System.out.println("Updating deleted messages using audit log data...");
        ThreadUtil.runAsync( () -> {
            List<AuditLogEntry> deletedMsgLog = logs.stream().filter( (l) -> l.getType() == ActionType.MESSAGE_DELETE).collect(Collectors.toList());
            for (AuditLogEntry log : deletedMsgLog) {
                this.markMessageDeleted(log.getTargetIdLong(), log.getTimeCreated().toInstant().toEpochMilli());
            }

            futureCompletion.complete(null);
        });

        return futureCompletion;
    }


    public Timestamp getLastLogTime () {

        try (Connection conn = this.db.getInstance().getConnection()) {

            String query = "SELECT * FROM audit_logs WHERE guild_id=? ORDER BY creation DESC;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setLong(1, guild.getIdLong());
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        try {
                            return result.getTimestamp("creation");
                        } catch (ErrorResponseException e) {
                            System.out.println("Latest msg was deleted.. trying to find another one...");
                        }
                    }
                }
            }

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }

        return null;

    }

    public CompletableFuture<List<AuditLogEntry>> getAuditLogsFromDb() {

        CompletableFuture<List<AuditLogEntry>> futureAuditLogs = new CompletableFuture<>();

        ThreadUtil.runAsync( () -> {
            // get audit logs
            List<AuditLogEntry> logs = new ArrayList<>();

            try (Connection conn = this.db.getInstance().getConnection()) {

                String query = "SELECT * FROM audit_logs WHERE guild_id=? AND creation>?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setLong(1, guild.getIdLong());
                    // TODO: Config support & Test this ->
                    ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
                    try (ResultSet result = ps.executeQuery()) {

                        // create a user cache because many times its a same ppl in audit logs this makes the program faster!
                        // For some reason jda is not always cacheing retrieved users
                        HashMap<Long, UserImpl> retrievedUserCache = new HashMap<>();

                        while (result.next()) {

                            int rawAction = result.getInt("action_type");

                            long userId = result.getLong("user_id");
                            if (!retrievedUserCache.containsKey(userId)) {
                                retrievedUserCache.put(userId, (UserImpl) Main.getJDA().retrieveUserById(userId).complete());
                            }

                            AuditLogEntry log = new AuditLogEntry(
                                    ActionType.from(rawAction),
                                    rawAction,
                                    result.getLong("action_id"),
                                    result.getLong("target_id"),
                                    (GuildImpl) guild,
                                    retrievedUserCache.get(userId),
                                    null,
                                    result.getString("reason"),
                                    Serializer.deserializeLogChanges(result.getString("changes")),
                                    Serializer.deserializeLogOptions(result.getString("options"))
                            );

                            logs.add(log);
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            futureAuditLogs.complete(logs);
        });


        return futureAuditLogs;
    }


    /**
     * @return - Time stamp of the last message that was saved in the database for this channel
     */
    public Timestamp getLastSavedMessageTime (TextChannel channel) {

        try (Connection conn = this.db.getMsgDb(channel).getConnection()) {

            String query = "SELECT * FROM messages ORDER BY creation DESC;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        try {
                            return result.getTimestamp("creation");
                        } catch (ErrorResponseException e) {
                            System.out.println("Latest msg was deleted.. trying to find another one...");
                        }
                    }
                }
            }

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void markMessageDeleted (long messageId, long time) {

        try (Connection conn = this.db.getMsgDb(getChannelIdFromMsg(messageId)).getConnection()) {

            String query = "UPDATE `messages` SET deleted=?, deletion_time=? WHERE message_id=?;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setBoolean(1, true);
                ps.setTimestamp(2, new Timestamp(time));
                ps.setLong(3, messageId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public long getChannelIdFromMsg (long messageId) {
        File[] files = new File(this.db.getGuildPath(), "msgDb").listFiles();
        if (files == null) return -1;

        for (File channelDb : files)
        try (Connection conn = this.db.getDbFromPath(channelDb.getPath()).getConnection()) {

            String query = "SELECT `messages` WHERE message_id=?;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setLong(1, messageId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return Long.parseLong(channelDb.getName().replace(".db", ""));
                }
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @return - All msg ids after specified epoch time ordered from newest msg to oldest.
     */
    public CompletableFuture<List<Long>> getHistoryAfterDatabase(TextChannel channel, long epochTime) {

        CompletableFuture<List<Long>> futureMessages = new CompletableFuture<>();

        List<Long> databaseMsgsIds = new ArrayList<>();

        ThreadUtil.runAsync( () -> {

            System.out.println("Checking if any deleted messages in #" + channel.getName() + " have not been updated to the database...");
            try (Connection conn = this.db.getInstance().getConnection()) {

                String query = "SELECT `message_id` FROM messages WHERE channel_id=? AND creation>? ORDER BY creation ASC;";

                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setLong(1, channel.getIdLong());
                    ps.setTimestamp(2, new Timestamp(epochTime));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            databaseMsgsIds.add(rs.getLong("message_id"));
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            futureMessages.complete(databaseMsgsIds);
        });

        return futureMessages;
    }

}
