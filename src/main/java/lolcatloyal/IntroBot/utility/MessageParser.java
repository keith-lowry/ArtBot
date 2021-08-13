package lolcatloyal.IntroBot.utility;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class MessageParser {

    /**
     * Removes the mentions from a Message's raw string
     * value and returns the resulting String starting
     * from the first non-blank space character in the
     * String.
     *
     * @param message The message from whose String mentions
     *                must be removed.
     * @return The message's String value without mentions.
     * @precond The message must not be null.
     */
    public static String parseMessageWithoutMentions(Message message){
        String messageRaw = message.getContentRaw();
        List<IMentionable> mentions = message.getMentions();

        //no mentions are in this string
        if (mentions.isEmpty()){
            return messageRaw;
        }

        // split messageRaw at last > character somehow
            //iterate over char array?
            //count size of mentions list; count number of > characters

        return null;
    }
}
