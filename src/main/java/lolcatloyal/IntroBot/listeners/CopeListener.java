package lolcatloyal.IntroBot.listeners;

import lolcatloyal.IntroBot.IntroBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.xml.soap.Text;
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

    private static final String COPE_RESPONSE = "**COPE AND SEETHE**";

    private static final String HELP_RESPONSE = "Commands: \ncope \ncope off \ncope on \ncope check \ncope help";

    private static final String ENABLED_RESPONSE = "Coping.";

    private static final String DISABLED_RESPONSE = "Not coping.";

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

        if(shouldRespond(message)){
            String messageRaw = message.getContentRaw();
            TextChannel channel = message.getTextChannel();

            //check status
            if(messageRaw.contains("cope check")){
                if(enabled){
                    channel.sendMessage(ENABLED_RESPONSE).queue();
                }
                else {
                    channel.sendMessage(DISABLED_RESPONSE).queue();
                }
            }
            //enable cope
            else if(messageRaw.contains("cope on") && message.getAuthor().getId().equals(IntroBot.MASTER_USER_ID)){
                enabled = true;
                channel.sendMessage(ENABLED_RESPONSE).queue();
            }
            //disable cope
            else if(messageRaw.contains("cope off") && message.getAuthor().getId().equals(IntroBot.MASTER_USER_ID)){
                enabled = false;
                channel.sendMessage(DISABLED_RESPONSE).queue();
            }
            else if(messageRaw.contains("cope help")){
                channel.sendMessage(HELP_RESPONSE).queue();
            }
            //standard cope functions when enabled
            else if (enabled){
                if(messageRaw.contains("cope add")){
                    String tenorLink = messageRaw.substring(8);

                    if (addGif(tenorLink, channel)) {
                        channel.sendMessage("Gif added!").queue();
                    }
                    else {
                        channel.sendMessage("Gif not added").queue();
                    }
                }
//                else if(messageRaw.contains("cope remove")){
//                    String tenorLink = messageRaw.substring(8);
//
//                    if(removeGif(tenorLink)){
//                        channel.sendMessage("Gif removed!").queue();
//                    }
//                    else {
//                        channel.sendMessage("Gif to remove not valid").queue();
//                    }
//                }
                //cope response
                else {
                    //TODO: make function for getting user mentions into a string from a message

                    String response = COPE_RESPONSE;

                    List<User> mentionedUsers = message.getMentionedUsers();

                    if (!mentionedUsers.isEmpty()){
                        StringBuilder s = new StringBuilder();

                        boolean firstMention = true;

                        for (User user: mentionedUsers){
                            if(firstMention){
                                s.append(user.getAsMention());
                                firstMention = false;
                            }
                            else {
                                s.append(" ");
                                s.append(user.getAsMention());
                            }
                        }

                        s.append(" ");
                        s.append(COPE_RESPONSE);
                        response = s.toString();
                    }

                    channel.sendMessage(response).queue();

                    channel.sendMessage(chooseGif()).queue();
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
    private boolean addGif(String tenorLink, TextChannel channel){
        if(tenorLinkIsValid(tenorLink, channel)){
            if (reactionGifs.add(tenorLink)){
                channel.sendMessage("List could add the link").queue();
                return true;
            }
            else {
                channel.sendMessage("List could not add the link").queue();
                return false;
            }
        }
        else {
            channel.sendMessage("Gif link not valid").queue();
            return false;
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
//    private boolean removeGif(String tenorLink){
//        if(!tenorLinkIsValid(tenorLink)){
//            return false;
//        }
//        else{
//            return reactionGifs.remove(tenorLink);
//        }
//    }

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
    private boolean tenorLinkIsValid(String tenorLink, TextChannel channel){
        String linkPrefix = tenorLink.substring(0, 22);

        return linkPrefix.equals("https://tenor.com/view");
    }

    /**
     * Checks whether the CopeListener should make a
     * response to a message event.
     *
     * The listener can respond if
     * the message is sent by
     * a non-bot user and contains "cope."
     *
     * @param message The message event that the listener might be
     *                able to respond to.
     * @return True if the listener should respond to the message.
     */
    private boolean shouldRespond(Message message){
        return (!message.getAuthor().isBot() && message.getContentRaw().contains("cope"));
    }
}
