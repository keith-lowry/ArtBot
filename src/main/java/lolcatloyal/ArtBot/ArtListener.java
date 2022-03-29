package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for handling front-end capabilities of
 * ArtBot.
 *
 * Responsible for sending embeds with
 * Twitter links for the currently displayed
 * Art piece or Artist.
 *
 * Responsible for listening and responding to
 * user commands.
 *
 * Commands:
 * -add [Twitter Link]   -- add a Twitter link to the bot's collection
 * -showArtists          -- show the bot's current collection of Artists and their handles
 * -showCollection       -- show the currently displayed Artist's collection of art (if one is displayed)
 * -clearCollection      -- empty the collection of all entries
 * -help                 -- show commands
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ArtListener extends ListenerAdapter {
    private final MultiValueMap<String, String> m; //collection of art -- Artist links are keys, Art links are values
    private final EmbedBuilder eb;
    private String displayedArtist; //String link to currently displayed Artist
    private ArrayIterator<String> displayedLinks; //Iterator for currently displayed links
    private DisplayModeEnum displayMode;

    //Regex Patterns for Input Analysis -- THREAD SAFE
    private static final Pattern TWIT_PATTERN = Pattern.compile("^https://twitter\\.com/.+/status/.+");
    private static final Pattern FX_PATTERN = Pattern.compile("^https://fxtwitter\\.com/.+/status/.+");
    private static final Pattern ADD_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "add \\s*\\w++");
    private static final Pattern SHOW_COLL_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "showCollection$");

    //TODO: add methods for handling each individual command :3

    /**
     * Enum determining what the Bot is currently displaying.
     *
     * DisplayOff: nothing is displayed
     * DisplayArtists: Artist embeds are being displayed
     * DisplayArt: Art embeds for a particular Artist are being displayed
     */
    public enum DisplayModeEnum{
        DisplayOff,
        DisplayArtists,
        DisplayArt
    }

    /**
     * Creates a new ArtListener with
     * an empty collection.
     */
    public ArtListener(){
        m = new MultiValueMap<>();
        eb = new EmbedBuilder();
        displayedLinks = new ArrayIterator<>(new String[0]);
        displayMode = DisplayModeEnum.DisplayOff;
    }

    /**
     * Reacts to a non-bot click on a button attached
     * to a displayed link.
     *
     * @param event A buttoninteractionevent.
     */
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        if (event.getChannel().getId().equals(ArtBot.CHANNEL_ID)){
            //TODO: do stuff
        }
    }

    /**
     * Reacts to non-bot author message events
     * in the bot's text channel.
     *
     * @param event A messagereceivedevent.
     */
    public void onMessageReceived(@Nonnull MessageReceivedEvent event){
        MessageChannel channel = event.getChannel();

        //check if non-bot message and in proper channel
        if (!event.getAuthor().isBot() && channel.getId().equals(ArtBot.CHANNEL_ID)){
            String messageRaw = event.getMessage().getContentRaw();

            //Matchers for Command Parsing
            Matcher addMatcher = ADD_COMMAND_PATTERN.matcher(messageRaw);
            Matcher showMatcher = SHOW_COLL_COMMAND_PATTERN.matcher(messageRaw);

            //-add [Twitter Link]
            if (addMatcher.find()) {
                //TODO: what if mode is displayArtists or displayArt, does that matter?
                channel.sendMessage("I'll try to add that for you!").queue();

                String artLink = trimLink(messageRaw.substring(5));
                int linkType = determineLinkType(artLink);

                if (linkType == -1){ //Invalid link
                    channel.sendMessage("Invalid Link. Are you sure you're using a Twitter *post* link? " +
                            "Profile links cannot be added directly.").queue();
                    return;
                }
                else if (linkType == 0) { //Twitter Link --> turn into FXTwitter Link
                    artLink = twitToFXLink(artLink);
                }

                String artistLink = "";

                //Build Twitter Profile Link
                if (linkType == 0 || linkType == 1){
                    artistLink = buildTwitProfileLink(artLink);
                }

                //TODO: delete user message

                //Try to add to map
                if (m.addValue(artistLink, artLink)) {
                    //Send Confirmation Message
                    channel.sendMessage("Added! \n" + artLink + "\n" + artistLink).queue() ; //TODO remove links from confir. message
                }
                else {
                    //Send Failure Message
                    channel.sendMessage("Sorry - I couldn't do that. That piece was already stored.").queue();
                }
            }
            //-showCollection
            else if (showMatcher.find()){
                channel.sendMessage(displayMode.toString()).queue(); //TODO: remove debug print
                switch (displayMode){
                    case DisplayOff: {
                        String[] keys = m.getKeys().toArray(new String[0]);

                        if (keys.length == 0){
                            channel.sendMessage("Nothing to show right now...").queue();
                        }
                        else {
                            displayMode = DisplayModeEnum.DisplayArtists;
                            displayedLinks.setArray(m.getKeys().toArray(new String[0]));
                            displayedArtist = displayedLinks.next();
                            channel.sendMessage(displayedArtist).queue();
                            //TODO: add action row
                        }
                        break;
                    }
                    case DisplayArtists: {
                        displayedArtist = displayedLinks.next();
                        channel.sendMessage(displayedArtist).queue();
                        break;
                    }
                    case DisplayArt: {
                        //TODO
                        break;
                    }
                }
                channel.sendMessage(displayMode.toString()).queue(); //TODO: remove debug print
            }
        }

    }


    /**
     * Determines the type of given Twitter link.
     *
     * A link can be:
     * a Twitter post link (0),
     * an FXTwitter post link (1),
     * or Invalid (-1).
     *
     * Note: Links to users' profiles are considered
     * invalid.
     *
     * @param link Link to determine type of.
     * @return 0 for Twitter link, 1 for FXTwitter link,
     * or -1 for an Invalid link.
     */
    private int determineLinkType(String link){
        Matcher twitMatcher = TWIT_PATTERN.matcher(link);
        Matcher fxMatcher = FX_PATTERN.matcher(link);

        if (twitMatcher.find()){
            return 0; //Twitter Link
        }
        else if (fxMatcher.find()){
            return 1; //FXTwitter Link
        }

        return -1;
    }

    /**
     * Clears the ArtListener's collection of art.
     */
    private void clearCollection(){
        m.clear();
    }

    /**
     * Trims a link of white space and any following
     * characters.
     *
     * @precond There are no characters preceding the given link besides
     * whitespace.
     * @precond The given link String is not empty (i.e. "").
     * @param link The desired link to trim.
     * @return The given link without any whitespace or following
     * characters.
     */
    private String trimLink(String link){
        link = link.trim(); //trim whitespace

        String[] linkParts = link.split(" "); //split based on whitespaces

        return linkParts[0];
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

    /**
     * Builds and returns a Twitter user profile link
     * for the author of a given Twitter or FXTwitter
     * post link.
     *
     * @param postLink A Twitter or FXTwitter post link.
     * @return The profile link of the given post's author.
     */
    private String buildTwitProfileLink(String postLink){
        String[] linkParts = postLink.split("/");
        String twitterHandle = linkParts[3];
        return "https://twitter.com/" + twitterHandle;
    }
}
