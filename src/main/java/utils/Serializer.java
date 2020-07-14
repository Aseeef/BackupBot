package utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.PermissionOverride;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Serializer {


    public static String serializeReactions(List<MessageReaction> reactions) {
        JSONObject reactionData = new JSONObject();

        reactions.forEach( (mr) -> {
            MessageReaction.ReactionEmote reactionEmote = mr.getReactionEmote();
            reactionData.put("code", reactionEmote.getAsReactionCode())
                    .put("data", new JSONObject().put("isEmoji", reactionEmote.isEmoji()).put("count", mr.getCount()));
        });

        return reactionData.toString();
    }

    public static String serializeEmbeds(List<MessageEmbed> embeds) {
        JSONObject embedsData = new JSONObject();

        int i = 0;
        for (MessageEmbed embed : embeds) {
            if (embed.getType() != EmbedType.RICH) continue;
            embedsData.put(String.valueOf(i), embed.toData().toString());
            i++;
        }

        return embedsData.toString();
    }

    public static String serializePerms(EnumSet<Permission> permissions) {
        return permissions.toString();
    }

    public static String serializePermOverrides(List<PermissionOverride> permissionOverrides) {

        JSONObject permsData = new JSONObject();

        List<PermissionOverride> memberOverrides = permissionOverrides.stream().filter(PermissionOverride::isMemberOverride).collect(Collectors.toList());
        List<PermissionOverride> roleOverrides = permissionOverrides.stream().filter(PermissionOverride::isRoleOverride).collect(Collectors.toList());

        memberOverrides.forEach( (mpo) -> {
            if (mpo.getMember() == null) return;
            permsData.put("memberOverrides", new JSONObject()
                    .put("memberId", mpo.getMember().getIdLong())
                    .put("allowed", mpo.getAllowed().toString())
                    .put("denied", mpo.getDenied().toString())
            );
        });

        roleOverrides.forEach( (rpo) -> {
            if (rpo.getRole() == null) return;
            permsData.put("roleOverrides", new JSONObject()
                    .put("role", rpo.getRole().getIdLong())
                    .put("allowed", rpo.getAllowed().toString())
                    .put("denied", rpo.getDenied().toString())
            );
        });

        return permsData.toString();
    }

    public static Permission parsePermission(String string) {
        for (Permission perm : Permission.values()) {
            if (string.equalsIgnoreCase(perm.toString()))
                return perm;
        }
        return null;
    }

}
