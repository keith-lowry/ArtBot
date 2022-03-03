package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for handling front-end capabilities of
 * ArtBot.
 *
 * Responsible for sending embeds with
 * Twitter links to the currently displayed
 * artist or art piece.
 *
 * Responsible for listening and responding to
 * user commands.
 */
public class ArtListener extends ListenerAdapter {
    private final ArtCollection collection;
    private final EmbedBuilder eb;
    private static final Pattern twitPattern = Pattern.compile("^https://twitter\\.com/.+/status/.+");
    private static final Pattern fxPattern = Pattern.compile("^https://fxtwitter\\.com/.+/status/.+");


    public ArtListener(){
        collection = new ArtCollection();
        eb = new EmbedBuilder();
    }

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();

        //check if non-bot message and in proper channel
        if (!event.getAuthor().isBot() && channel.getId().equals(ArtBot.CHANNEL_ID)){
            String messageRaw = event.getMessage().getContentRaw();
            String prefix = messageRaw.substring(0, 1);

            if (messageRaw.length() < 20 || !prefix.equals(ArtBot.PREFIX)){
                return;
            }

            String link = messageRaw.substring(1);

            boolean isTwitterLink = isTwitterLink(link);

            if (isTwitterLink){
                channel.sendMessage("That is, indeed, a um, erm, twitter link...").queue();
            }
            else {
                channel.sendMessage("Link invalid. Make sure you are using Twitter *post* links.").queue();
            }
        }

    }

    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event){
        if (!event.getUser().isBot()){
            //TODO
        }
    }

    private boolean isTwitterLink(String message){
        Matcher twitMatcher = twitPattern.matcher(message);
        Matcher fxMatcher = fxPattern.matcher(message);
        return twitMatcher.find() || fxMatcher.find();
    }

}
