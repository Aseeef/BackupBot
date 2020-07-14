package bot;

import console.Console;
import console.Logs;
import events.OnReady;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import config.Config;
import utils.Utils;

import javax.security.auth.login.LoginException;

public class Backups {

    private static JDA jda;

    public static void main(String[] args) {

        // Set console output settings
        System.setOut(new Logs.GeneralStream(System.out));
        System.setErr(new Logs.ErrorStream(System.err));
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in thread \"" + thread.getName() + "\" caused by " + throwable.initCause(throwable.getCause()));
            throwable.printStackTrace();
        });

        // System/Console settings
        Console.loadShutdownHook();
        Console.loadConsoleCommands();

        // load config
        Config.load();

        try {
            jda = JDABuilder.createDefault(Config.get().getBotToken())
                    .setEnabledIntents(
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_INVITES,
                            GatewayIntent.GUILD_BANS
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                    .build();

            // Set presence
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setIdle(false);

            // other configuration
            String name = Config.get().getBotName();
            if (!jda.getSelfUser().getName().equals(name))
                jda.getSelfUser().getManager().setName(name).queue();
            // TODO: avatar

            jda.addEventListener(new OnReady());

        } catch (LoginException e) {
            e.printStackTrace();
        }


    }

    public static JDA getJda() {
        return jda;
    }

}
