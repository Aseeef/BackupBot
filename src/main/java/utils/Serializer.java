package utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.audit.AuditLogOption;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.PermissionOverride;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public static String serializeLogChanges(Map<String, AuditLogChange> changes) {
        JSONObject changeData = new JSONObject();
        changes.forEach((key, value) -> changeData.put(key, value.toString()));
        return changeData.toString();
    }

    public static String serializeLogOptions(Map<String, Object> changes) {
        JSONObject optionData = new JSONObject();
        changes.forEach((key, value) -> optionData.put(key, value.toString()));
        return optionData.toString();
    }

    public static Map<String, AuditLogChange> deserializeLogChanges(String json) {
        JSONObject changesData = new JSONObject(json);
        Map<String, AuditLogChange> changes = new HashMap<>();
        changesData.keySet().forEach( (key) -> {
            //ALC:%s(%s -> %s)
            //ALC:name(? Online Players: 68 -> ? Online Players: 66)
            Pattern pattern = Pattern.compile("ALC:(.*)\\((.*) -> (.*)\\)");
            Matcher m = pattern.matcher(changesData.getString(key));
            if (m.matches())
                changes.put(key, new AuditLogChange(m.group(1), m.group(2), m.group(0)));
        });
        return changes;
    }

    public static Map<String, Object> deserializeLogOptions(String json) {
        JSONObject optionsData = new JSONObject(json);
        Map<String, Object> changes = new HashMap<>();
        optionsData.keySet().forEach( (key) -> {
            changes.put(key, optionsData.getString(key));
        });
        return changes;
    }

    public static Permission parsePermission(String string) {
        for (Permission perm : Permission.values()) {
            if (string.equalsIgnoreCase(perm.toString()))
                return perm;
        }
        return null;
    }

}
