package dev.aseef.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

    /**
     * @return - Quickly and efficiently sort out msgs after this history; Ordered from newest msg to oldest.
     */
    public static List<Message> getHistoryAfter(Message message) {
        List<Message> historyAfter = new ArrayList<>();
        boolean moreHistory = true;
        while (moreHistory) {
            List<Message> messages = message.getChannel().getHistoryAfter(message, 100).complete().getRetrievedHistory();
            moreHistory = messages.size() == 100;
            if (messages.size() != 0) {
                message = messages.get(0); // latest msg
                historyAfter.addAll(0, messages);
            }
        }
        return historyAfter;
    }

    /**
     * @return - Quickly and efficiently sort out msgs after this history; Ordered from newest msg to oldest.
     */
    public static List<Message> getHistoryAfter(TextChannel channel, long epochTime) {
        OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.systemDefault());

        Message latestMsg;

        latestMsg = channel.retrieveMessageById(channel.getLatestMessageIdLong()).complete();

        List<Message> historyAfter = new ArrayList<>();
        historyAfter.add(latestMsg);

        boolean moreHistory = true;
        while (moreHistory) {
            List<Message> msgs = channel.getHistoryBefore(latestMsg, 100).complete().getRetrievedHistory();
            latestMsg = msgs.get(msgs.size()-1);

            if (latestMsg.getTimeCreated().isBefore(time) //if the msg is older then we want
            || msgs.size() != 100 //or end of channel history reached
            ) moreHistory = false;

            if (!moreHistory && msgs.size() != 0)
                msgs = msgs.stream().filter( (m) -> m.getTimeCreated().isAfter(time)).collect(Collectors.toList());

            if (msgs.size() != 0)
            historyAfter.addAll(msgs);
        }
        return historyAfter;
    }

}
