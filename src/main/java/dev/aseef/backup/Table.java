package dev.aseef.backup;

import dev.aseef.database.DatabaseHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum Table {

    /**
     * The database table storing information about discord bans
     */
    BANS("bans", ""), //continuous
    /**
     * The database table storing information about discord categories
     */
    CATEGORIES("categories", ""),//periodic
    /**
     * The database table storing information about all members
     */
    MEMBERS("members", ""), //continuous
    /**
     * The database table storing information about all custom emotes
     */
    EMOTES("emotes", ""),//periodic
    /**
     * The database table storing all roles as well as their associated permissions
     */
    ROLES("roles", ""),//periodic
    /**
     * The database table storing the global discord server settings
     */
    SETTINGS("settings", ""),//periodic
    /**
     * The database table storing all text channels and their configurations
     */
    TEXT_CHANNELS("text_channels", ""),//periodic
    /**
     * The database table storing all thread channels and their configurations
     */
    THREADS("threads", ""),//periodic
    /**
     * The database table storing all voice channels and their configurations
     */
    VOICE_CHANNELS("voice_channels", ""),//periodic
    /**
     * The database table storing all stage channels and their configurations
     */
    STAGE_CHANNELS("stage_channels", ""),//periodic
    /**
     * The database table storing information about all sent messages
     */
    MESSAGES("messages", ""), //continuous
    /**
     * The database table storing
     */
    GUILD_PROFILE("guilds", createGuildProfileStatement()), //once
    /**
     * The database table tracking all excluded text channels for which messages should not be saved
     */
    EXCLUDED_TEXTCHANNELS("excluded_channels", createExcludedChannelsStatement()), //once
    /**
     * The database table containing blob data and hashes for all attachments
     */
    ATTACHMENTS("attachments", createAttachmentsStatement()) //continuous
    ;

    private String tableName;
    private String createTableStatement;

    Table (String tableName, String createTableStatement) {
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
    }

    private static String createCategoryStatement() {

    }

    private static String createAttachmentsStatement() {
        return "CREATE TABLE `attachments` " +
                "( `attachment_index` MEDIUMINT NOT NULL AUTO_INCREMENT , " +
                "`content_hash` VARCHAR NOT NULL , " +
                "`origin_guild_index` SMALLINT NOT NULL , " +
                "`attachment_content` LONGBLOB NOT NULL , " +
                "`creation` TIMESTAMP NOT NULL , " +
                "PRIMARY KEY (`attachment_index`), " +
                "INDEX (`creation`), " +
                "INDEX (`origin_guild_index`), " +
                "UNIQUE (`content_hash`)) " +
                "COMMENT = 'Table contains all attachments for all guilds and it\\'s hash';";
    }

    private static String createGuildProfileStatement() {
        return "CREATE TABLE `guilds` " +
                "( `guild_index` SMALLINT(5) NOT NULL AUTO_INCREMENT , " +
                "`discord_snowflake` BIGINT(20) NOT NULL , " +
                "`ban_backup` TINYINT(1) NOT NULL , " +
                "`emoji_backup` TINYINT(1) NOT NULL , " +
                "`role_backup` TINYINT(1) NOT NULL , " +
                "`channel_backup` TINYINT(1) NOT NULL , " +
                "`member_backup` TINYINT(1) NOT NULL , " +
                "`message_backup` TINYINT(1) NOT NULL , " +
                "`settings_backup` TINYINT(1) NOT NULL , " +
                "`anti_nuke` TINYINT(1) NOT NULL , " +
                "`max_attachment_size` INT(6) NOT NULL COMMENT 'Attachment sizes are in kilobyte' , " +
                "`messages_keep_updated` INT(10) NOT NULL COMMENT 'Updating time is in seconds' , " +
                "`initial_backup` Timestamp NOT NULL , " +
                "`latest_backup` Timestamp NOT NULL , " +
                "PRIMARY KEY (`index_id`), " +
                "UNIQUE (`discord_id`)) " +
                "COMMENT = 'This table stores guild backup profiles and settings';";
    }

    private static String createExcludedChannelsStatement() {
        return "CREATE TABLE `plan`.`excluded_channels` " +
                "( `guild_index` SMALLINT NOT NULL , " +
                "`channel_index` MEDIUMINT(7) NOT NULL , " +
                "PRIMARY KEY (`guild_index`, `channel_index`)) " +
                "COMMENT = 'Tracks all channels excluded from message backups';";
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
