package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.EmbedBuilder;
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
 *
 * Commands:
 * -add [Twitter Link]   -- add a Twitter link to the bot's collection
 * -showArtists          -- show the bot's current collection of Artists and their handles
 * -showCollection       -- show the currently displayed Artist's collection of art (if one is displayed)
 * -clearCollection      -- empty the collection of all entries
 * -help                 -- show command help
 *
 */
public class ArtListener extends ListenerAdapter {
    private ArtCollection collection;
    private final EmbedBuilder EB;

    //Regex Patterns for Input Analysis -- THREAD SAFE
    private static final Pattern TWIT_PATTERN = Pattern.compile("^https://twitter\\.com/.+/status/.+");
    private static final Pattern FX_PATTERN = Pattern.compile("^https://fxtwitter\\.com/.+/status/.+");
    private static final Pattern ADD_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "add \\s*\\w++");


    public ArtListener(){
        collection = new ArtCollection();
        EB = new EmbedBuilder();
    }

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();

        //check if non-bot message and in proper channel
        if (!event.getAuthor().isBot() && channel.getId().equals(ArtBot.CHANNEL_ID)){
            String messageRaw = event.getMessage().getContentRaw();

            Matcher addMatcher = ADD_COMMAND_PATTERN.matcher(messageRaw);

            if (addMatcher.find()) {
                channel.sendMessage("I'll try to add that for you!").queue();
                String link = trimLink(messageRaw.substring(5));
                if (isTwitterLink(link)) {
                    channel.sendMessage("Added!").queue();
                    channel.sendMessage(link).queue();
                }
                else{
                    channel.sendMessage("That's not a twitter link!").queue();
                }
            }

        }

    }

    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event){
        //TODO: check if not discord bot author
    }

    //TODO: turn into "determineLinkType"
    private boolean isTwitterLink(String message){
        Matcher twitMatcher = TWIT_PATTERN.matcher(message);
        Matcher fxMatcher = FX_PATTERN.matcher(message);
        return twitMatcher.find() || fxMatcher.find();
    }

    /**
     *
     */
    private void clearCollection(){
        collection = new ArtCollection();
    }

    private String trimLink(String link){
        int i = 0;
        String result = link.trim();

        while (i < result.length()){
            if (result.charAt(i) == ' '){
                result = result.substring(0, i);
                return result;
            }
            i++;
        }

        return result;
    }

}
