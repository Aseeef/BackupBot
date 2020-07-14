package commands;

import config.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.Serializer;

public class RestoreCommand extends Command {

    public RestoreCommand() {
        super("restore", Serializer.parsePermission(Config.get().getUsePermissionRequired()), Type.DMS_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, MessageChannel channel, String[] args) {

    }

}
