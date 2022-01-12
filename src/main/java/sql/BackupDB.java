package sql;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BackupDB {

    private Guild guild;
    private long time;
    private List<DatabaseHandler> instances = new ArrayList<>();

    private String guildPath;

    private static List<BackupDB> backupInstances = new ArrayList<>();

    public BackupDB(Guild guild, long time) {
        this.guild = guild;
        this.time = time;
        guildPath = "db/" + guild.getId();
    }

    public synchronized DatabaseHandler getInstance() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String path = guildPath + "/" + sdf.format(new Date(this.time)) + ".db";
        Optional<DatabaseHandler> optionalDatabase = instances.stream().filter(database -> database.getName().equalsIgnoreCase(path)).findFirst();
        if (!optionalDatabase.isPresent()) {
            DatabaseHandler dbh = new DatabaseHandler(1);
            dbh.init(path);
            // create tables
            this.generateTables(dbh);
            instances.add(dbh);
            return dbh;
        }
        return optionalDatabase.get();
    }

    public synchronized DatabaseHandler getMsgDb(long channelId) {
        String path = guildPath + "/msgDb/" + channelId + ".db";
        Optional<DatabaseHandler> optionalDatabase = instances.stream().filter(database -> database.getName().equalsIgnoreCase(path)).findFirst();
        if (!optionalDatabase.isPresent()) {
            DatabaseHandler dbh = new DatabaseHandler(1);
            dbh.init(path);
            this.generateMsgTable(dbh);
            instances.add(dbh);
            return dbh;
        }
        return optionalDatabase.get();
    }

    public synchronized DatabaseHandler getMsgDb(TextChannel channel) {
        return getMsgDb(channel.getIdLong());
    }

    public synchronized DatabaseHandler getDbFromPath(String path) {
        Optional<DatabaseHandler> optionalDatabase = instances.stream().filter(database -> database.getName().equalsIgnoreCase(path)).findFirst();
        if (!optionalDatabase.isPresent()) {
            DatabaseHandler dbh = new DatabaseHandler(1);
            dbh.init(path);
            this.generateMsgTable(dbh);
            instances.add(dbh);
            return dbh;
        }
        return optionalDatabase.get();
    }

    public String getGuildPath() {
        return guildPath;
    }

    public List<DatabaseHandler> getAll() {
        return this.instances;
    }

    public static List<DatabaseHandler> getInstances() {
        List<DatabaseHandler> databases = new ArrayList<>();
        backupInstances.forEach( (backupDB -> databases.addAll(backupDB.getAll())));
        return databases;
    }

    private void generateMsgTable(DatabaseHandler dbh) {
        try (Connection conn = dbh.getConnection()) {
            String createMsgs = "CREATE TABLE IF NOT EXISTS messages(" +
                    "message_id bigint not null " +
                    "constraint messages_pk " +
                    "primary key, " +
                    "creation timestamp, " +
                    "member_avatar varchar, " +
                    "member_name varchar, " +
                    "message_content varchar, " +
                    "reactions varchar, " +
                    "pinned tinyint(1), " +
                    "embeds varchar, " +
                    "attachment_id bigint default -1, " +
                    "deleted tinyint(1) default 0, " +
                    "last_modified timestamp default null," +
                    "restored_id bigint default -1); " +
                    "create unique index messages_message_id_uindex " +
                    "on messages (message_id);";
            try (PreparedStatement ps = conn.prepareStatement(createMsgs)) {
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateTables(DatabaseHandler dbh) {
        try (Connection conn = dbh.getConnection()) {

            //TODO: Track deletions for roles, channels, and etc too!
            // Last modified support to categories, emotes, roles, text/voice channels, & settings

            // creates the server settings table
            String createSettings = "CREATE TABLE IF NOT EXISTS settings (" +
                    "guild_id BIGINT PRIMARY KEY UNIQUE NOT NULL, " +
                    "guild_name VARCHAR (50), " +
                    "guild_description VARCHAR, " +
                    "afk_channel_id BIGINT, " +
                    "default_channel_id BIGINT, " +
                    "system_channel_id BIGINT, " +
                    "afk_timeout_seconds SMALLINT, " +
                    "default_notif_level VARCHAR (25), " +
                    "explicit_content_level VARCHAR (25), " +
                    "region VARCHAR (25), " +
                    "verification_level VARCHAR (25), " +
                    "last_modified TIMESTAMP);";
            try (PreparedStatement ps = conn.prepareStatement(createSettings)) {
                ps.execute();
            }

            // creates the roles table
            String createRoles = "CREATE TABLE IF NOT EXISTS roles (" +
                    "role_id BIGINT PRIMARY KEY " +
                    "UNIQUE NOT NULL, " +
                    "role_name VARCHAR (50), " +
                    "role_color INT, " +
                    "hoisted TINYINT (1), " +
                    "mentionable TINYINT (1), " +
                    "role_order SMALLINT, " +
                    "permissions BIGINT, " +
                    "creation timestamp default CURRENT_TIMESTAMP, " +
                    "deleted TINYINT (1) default 0, " +
                    "last_modified timestamp default null);";
            try (PreparedStatement ps = conn.prepareStatement(createRoles)) {
                ps.execute();
            }

            // creates the members table
            String createMembers = "CREATE TABLE IF NOT EXISTS members (" +
                    "member_id BIGINT PRIMARY KEY UNIQUE NOT NULL, " +
                    "member_nick VARCHAR(75) default NULL, " +
                    "member_roles VARCHAR);";
            try (PreparedStatement ps = conn.prepareStatement(createMembers)) {
                ps.execute();
            }

            // creates the emotes table
            String createEmotes = "CREATE TABLE IF NOT EXISTS emotes (" +
                    "emote_id BIGINT UNIQUE PRIMARY KEY NOT NULL, " +
                    "emote_name VARCHAR (25), " +
                    "creation timestamp default CURRENT_TIMESTAMP, " +
                    "deleted TINYINT (1) default 0, " +
                    "last_modified timestamp default null);";
            try (PreparedStatement ps = conn.prepareStatement(createEmotes)) {
                ps.execute();
            }

            // creates the bans table
            String bannedUsers = "CREATE TABLE IF NOT EXISTS bans (" +
                    "user_id BIGINT PRIMARY KEY UNIQUE NOT NULL, " +
                    "user_tag VARCHAR(75), " +
                    "ban_reason VARCHAR, " +
                    // means ban time in this context
                    "creation timestamp default CURRENT_TIMESTAMP, " +
                    // in this context, deleted means unbanned
                    "deleted TINYINT (1) default 0, " +
                    // in this context last_modified refers to unban time
                    "last_modified timestamp default null);";
            try (PreparedStatement ps = conn.prepareStatement(bannedUsers)) {
                ps.execute();
            }


            // creates the categories table
            String createCategories = "CREATE TABLE IF NOT EXISTS categories (" +
                    "category_id BIGINT PRIMARY KEY UNIQUE NOT NULL, " +
                    "category_name VARCHAR (50), " +
                    "permissions VARCHAR, " +
                    "creation timestamp default CURRENT_TIMESTAMP, " +
                    "deleted TINYINT (1) default 0, " +
                    "last_modified timestamp default null);";
            try (PreparedStatement ps = conn.prepareStatement(createCategories)) {
                ps.execute();
            }

            // creates the voice channels table
            String createVoiceChannels = "CREATE TABLE IF NOT EXISTS voicechannels (" +
                    "channel_id BIGINT PRIMARY KEY UNIQUE NOT NULL, " +
                    "category_id BIGINT, " +
                    "channel_name VARCHAR (50), " +
                    "bitrate INT, " +
                    "user_limit SMALLINT, " +
                    "permission VARCHAR, " +
                    "creation timestamp default CURRENT_TIMESTAMP, " +
                    "deleted TINYINT (1) default 0," +
                    "last_modified timestamp default null);";
            try (PreparedStatement ps = conn.prepareStatement(createVoiceChannels)) {
                ps.execute();
            }

            // creates the text channels table
            String createTextChannels = "CREATE TABLE IF NOT EXISTS textchannels (" +
                    "channel_id bigint not null constraint channels_pk primary key, " +
                    "category_id BIGINT, " +
                    "channel_name varchar(50), " +
                    "channel_topic varchar, " +
                    "permissions varchar, " +
                    "slow_mode int," +
                    "creation timestamp default CURRENT_TIMESTAMP, " +
                    "deleted TINYINT (1) default 0, " +
                    "last_modified timestamp default null); " +
                    "CREATE UNIQUE INDEX channels_channel_id_uindex " +
                    "on channels (channel_id);";
            try (PreparedStatement ps = conn.prepareStatement(createTextChannels)) {
                ps.execute();
            }

            // creates the audit logs table (which is used to judge modification
            // TODO: Purge logs order then 90d
            String auditLogs = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "action_id BIGINT PRIMARY KEY UNIQUE NOT NULL, " +
                    "action_type SMALLINT, " +
                    "target_id BIGINT, " +
                    "guild_id BIGINT, " +
                    "user_id BIGINT, " +
                    "reason VARCHAR, " +
                    "changes VARCHAR, " +
                    "options VARCHAR, " +
                    "creation TIMESTAMP);";
            try (PreparedStatement ps = conn.prepareStatement(auditLogs)) {
                ps.execute();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
