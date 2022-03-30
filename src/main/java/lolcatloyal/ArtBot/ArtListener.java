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
    private static final Pattern CLEAR_COLL_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "clearCollection$");
    private static final Pattern HELP_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "help$");

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
     * Commands:
     * -add [Twitter Link]   -- add a Twitter link to the bot's collection
     * -showCollection       -- show the bot's current collection of Artists and their handles with nav buttons
     * -clearCollection      -- empty the collection of all entries
     * -help                 -- show commands
     *
     * @param event A buttoninteractionevent.
     */
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        //TODO: check if button id matches
    }

    /**
     * Reacts to user messages in the bot's message channel according
     * to the Bot commands specified at the top of this class:
     *
     * @param event A MessageReceivedEvent.
     */
    public void onMessageReceived(@Nonnull MessageReceivedEvent event){
        MessageChannel channel = event.getChannel();

        //check if non-bot message and in proper channel
        if (!event.getAuthor().isBot() && channel.getId().equals(ArtBot.CHANNEL_ID)){
            String messageRaw = event.getMessage().getContentRaw();

            //Matchers for Command Parsing
            Matcher addMatcher = ADD_COMMAND_PATTERN.matcher(messageRaw);
            Matcher showCollMatcher = SHOW_COLL_COMMAND_PATTERN.matcher(messageRaw);
            Matcher clearCollMatcher = CLEAR_COLL_COMMAND_PATTERN.matcher(messageRaw);
            Matcher helpMatcher = HELP_COMMAND_PATTERN.matcher(messageRaw);

            //-add [Twitter Link]
            if (addMatcher.find()) {
                addToCollection(channel, messageRaw);
            }
            //-showCollection
            else if (showCollMatcher.find()){
                showCollection(event, channel);
            }
            //-clearCollection
            else if (clearCollMatcher.find()){
                clearCollection(event, channel);
            }
            //-help
            else if (helpMatcher.find()){
                help(event, channel);
            }
        }
    }

    //--- Text Commands --------------------------------------------------------------

    /**
     * Attempts to add an art link given by a user's command and a matching artist link to the collection.
     * Sends an appropriate response to the user detailing whether the attempt was successful or not.
     *
     * @param channel The MessageChannel to send a response in.
     * @param messageRaw The raw String content of the MessageReceivedEvent that called this command.
     */
    private void addToCollection(MessageChannel channel, String messageRaw){
        //TODO: what if mode is displayArtists or displayArt, does that matter?
        channel.sendMessage("I'll try to add that for you!").queue();

        String artLink = trimLink(messageRaw.substring(5)); //remove command portion of command and trim to link
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
        artistLink = buildTwitProfileLink(artLink);

        //Try to add to map
        if (m.addValue(artistLink, artLink)) {
            //Send Confirmation Message
            channel.sendMessage("Added! \n" + artistLink + "\n" + artLink).queue();
        }
        else {
            //Send Failure Message
            channel.sendMessage("Sorry - I couldn't do that. That piece was already stored.").queue();
        }

    }

    /**
     * Attempts to show the collection of artist profile links, which can then be
     * interacted with to explore their respective art link collections.
     *
     * @param channel The MessageChannel to display the collection of artists in.
     */
    private void showCollection(MessageReceivedEvent event, MessageChannel channel){
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

    /**
     * Clears the ArtListener's collection of art and artists.
     *
     * @param event MessageReceivedEvent that called this command.
     * @param channel MessageChannel to send response in.
     */
    private void clearCollection(MessageReceivedEvent event, MessageChannel channel){
        //TODO: implement
        //m.clear();
    }

    private void help(MessageReceivedEvent event, MessageChannel channel){
        //TODO: implement
    }

    //--- Nav Button Commands --------------------------------------------------------------
    private void next(ButtonInteractionEvent event){
        //TODO: implement

        //show next link in iterator, add to currently displayed artist if needed
    }

    private void prev(ButtonInteractionEvent event){
        //TODO: implement

        //show prev link in iterator, add to currently displayed artist if needed
    }

    private void remove(ButtonInteractionEvent event){
        //TODO: implement

        //Ask for confirmation

        //DisplayArtists: remove artist, show next artist if there is one; else exit entirely
        //DisplayArt: remove art, show next art if there is one; else return to artist view if possible; else exit entirely
    }

    private void showArt(ButtonInteractionEvent event){
        //TODO: implement

        //show the currently displayed artist's collection
    }

    private void exit(ButtonInteractionEvent event){
        //TODO: implement

        //Display Art: return to view of artists in collection
        //Display Artists: delete embed
    }


    //TODO: add below methods to util static class ----------------------------------------------------------------
    /**
     * Determines the type of a given Twitter link.
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
