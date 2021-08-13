package lolcatloyal.IntroBot.listeners;

import lolcatloyal.IntroBot.utility.MessageParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Listener that sends a random cope gif and a cope and seethe
 * message response when a server member says "cope."
 */
public class CopeListener extends ListenerAdapter {

    /**
     * Default array of reaction gifs to be added to the bot's
     * pool of reaction gifs.
     */
    private static final String[] DEFAULT_GIFS =
            {"https://tenor.com/view/cope-seethe-cope-seethe-cope-cope-seethe-cope-gif-20867322",
                    "https://tenor.com/view/cope-seethe-gif-21366850",
                    "https://tenor.com/view/cope-seeth-nero-dmc-devil-may-cry-gif-20738347",
                    "https://tenor.com/view/oomfie-twitter-mya-birdy-moots-gif-21657255"} ;

    private static final String RESPONSE = "**COPE AND SEETHE**";

    private static final String ENABLED_RESPONSE = "I'm listening! owo";

    private static final String DISABLED_RESPONSE = "I'm sleeping... uwu";

    private boolean enabled;

    private final List<String> reactionGifs;

    /**
     * Creates a new CopeListener.
     */
    public CopeListener(){
        reactionGifs = new LinkedList<String>();
        enabled = true;

        Collections.addAll(reactionGifs, DEFAULT_GIFS); //add default gifs to gif pool
    }

    /**
     * Method called when a message is sent in a text channel.
     *
     * @param event The event for a message sent in a text channel.
     */
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        Message message = event.getMessage();

        //message must not be sent by a bot
        if (!message.getAuthor().isBot()){
            TextChannel channel = event.getChannel();
            String messageRaw = message.getContentRaw().toLowerCase();
            String messageWithoutMentions = MessageParser.parseMessageWithoutMentions(message);

            //send cope response to a message that is "cope" or
            //"cope and seethe"
            if (shouldCope(messageRaw)){
                String response = RESPONSE;

                List<User> mentionedUsers = message.getMentionedUsers();

                if (!mentionedUsers.isEmpty()){
                    StringBuilder s = new StringBuilder();

                    boolean firstMention = true;

                    for (User user:
                            mentionedUsers) {

                        if (firstMention){
                            s.append(user.getAsMention());
                            firstMention = false;
                        }
                        else {
                            s.append(" ");
                            s.append(user.getAsMention());
                        }
                    }

                    s.append(" ");
                    s.append(RESPONSE);
                    response = s.toString();
                }

                channel.sendMessage(response).queue();

                channel.sendMessage(chooseGif()).queue();

            }
            //disable cope responses
            else if(messageRaw.equals("cope disable")){
                enabled = false;
                channel.sendMessage(DISABLED_RESPONSE).queue();
            }
            //enable cope responses
            else if(messageRaw.equals("cope enable")){
                enabled = true;
                channel.sendMessage(ENABLED_RESPONSE).queue();
            }
            //check if cope response is disabled or enabled
            else if(messageRaw.equals("cope check")){
                if (enabled){
                    channel.sendMessage(ENABLED_RESPONSE).queue();
                }
                else {
                    channel.sendMessage(DISABLED_RESPONSE).queue();
                }
            }
        }
    }

    /**
     * Choose a random Tenor gif link from
     * the list of reaction gifs.
     *
     * @return A randomly chosen reaction gif link.
     */
    private String chooseGif(){
        Random r = new Random();
        int index = r.nextInt(reactionGifs.size());

        return reactionGifs.get(index);
    }

    /**
     * Adds a new tenor gif link to the list
     * of reaction gifs if the link is valid
     * and not already in the list.
     *
     * @param tenorLink The tenor link for the gif to be
     *                  added to the list of reaction gifs.
     * @return True if the tenor link is valid and not
     *         already in the list of reaction gifs.
     */
    private boolean addGif(String tenorLink){
        if(tenorLinkIsValid(tenorLink)){
            return false;
        }
        else {
            return reactionGifs.add(tenorLink);
        }
    }

    /**
     * Removes a tenor gif link from the list
     * of reaction gifs if the link is valid and
     * is already in the list.
     *
     * @param tenorLink The tenor link for the gif to be
     *                  removed from the list of reaction gifs.
     * @return True if the tenor link is valid and already
     *         in the list of reaction gifs.
     */
    private boolean removeGif(String tenorLink){
        if(tenorLinkIsValid(tenorLink)){
            return false;
        }
        else{
            return reactionGifs.remove(tenorLink);
        }
    }

    /**
     * Checks whether a given tenor link is valid.
     *
     * A tenor link is considered valid if it is nonnull
     * and contains the tenor.com/view url.
     *
     * @param tenorLink The String tenor link to be checked for
     *                  validity.
     * @return True if the tenor link is valid.
     */
    private boolean tenorLinkIsValid(String tenorLink){
        return (tenorLink == null || !tenorLink.contains("tenor.com/view"));
    }

    /**
     * Checks whether the listener should make a
     * "cope and seethe" response to a non-bot
     * message.
     *
     * The listener can make this response if
     * it is enabled and the message sent by
     * a non-bot user is "cope" or "cope and
     * seethe."
     *
     * @param messageRaw The non-bot message to respond to.
     * @return True if the listener should make a "cope and seethe"
     *         response.
     */
    private boolean shouldCope(String messageRaw){
        return (enabled && (messageRaw.equals("cope") || messageRaw.equals("cope and seethe")));
    }
}
