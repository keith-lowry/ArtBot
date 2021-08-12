package lolcatloyal.IntroBot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;

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

    private final List<String> reactionGifs;
    private final Random r;

    public CopeListener(){
        reactionGifs = new LinkedList<String>();
        r = new Random();

        Collections.addAll(reactionGifs, DEFAULT_GIFS);
    }

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        Message message = event.getMessage();

        if (!message.getAuthor().isBot()){
            TextChannel channel = event.getChannel();
            String messageRaw = message.getContentRaw().toLowerCase();

            if (messageRaw.contains("cope")){
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
        }
    }

    /**
     * Choose a random Tenor gif link from
     * the list of reaction gifs.
     *
     * @return A randomly chosen reaction gif link.
     */
    private String chooseGif(){
        int index = r.nextInt(reactionGifs.size());

        return reactionGifs.get(index);
    }

    /**
     * Add a new reaction gif to the pool
     * of reaction gifs.
     *
     * @param gifLink The tenor link for the gif to be
     *                added.
     */
    private void addGif(String gifLink){
        reactionGifs.add(gifLink);
    }
}
