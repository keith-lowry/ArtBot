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

            //-add [Twitter Link]
            if (addMatcher.find()) {
                channel.sendMessage("I'll try to add that for you!").queue();

                String link = trimLink(messageRaw.substring(5));
                int linkType = determineLinkType(link);

                if (linkType == -1){ //Invalid link
                    channel.sendMessage("Invalid Link. Are you sure you're using a Twitter *post* link?").queue();
                    return;
                }
                else if (linkType == 0) { //Twitter Link --> turn into FXTwitter Link
                    link = twitToFXLink(link);
                }

                channel.sendMessage("Added! \n" + link).queue(); //Send FXTwitter Link confirmation
            }

        }

    }

    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event){
        //TODO: check if not discord bot author
    }

    /**
     * Determines the type of a given Twitter link.
     *
     * A link can be:
     * 0 - a Twitter post link,
     * 1 - a FXTwitter post link, or
     * -1 - Invalid.
     *
     * Note: Links to users' profiles are considered
     * invalid.
     * @param link Link to determine type of.
     * @return 0 for Twitter link, 1 for FXTwitter link,
     * or -1 for an Invalid link.
     */
    private int determineLinkType(String link){
        Matcher twitMatcher = TWIT_PATTERN.matcher(link);
        Matcher fxMatcher = FX_PATTERN.matcher(link);

        if (twitMatcher.find()){
            return 0;
        }
        else if (fxMatcher.find()){
            return 1;
        }

        return -1;
    }

    /**
     * Clears the ArtListener's collection of art links.
     */
    private void clearCollection(){
        collection = new ArtCollection();
    }

    /**
     * Trims a link of white space and any following
     * characters.
     *
     * @precond There are no characters preceding the given link besides
     * whitespace.
     * @param link The desired link to trim.
     * @return The given link without any whitespace or following
     * characters.
     */
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

    /**
     * Returns the FXTwitter version of a given Twitter link by
     * inserting "fx" into it.
     *
     * @precond twitLink is a Twitter link.
     * @param twitLink The desired Twitter link to turn into an FXTwitter link.
     * @return An FXTwitter version of a desired Twitter link.
     */
    private String twitToFXLink(String twitLink){
        StringBuilder result = new StringBuilder(twitLink);
        result.insert(8, "fx");
        return result.toString();
    }

}
