package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

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
 * user text and button commands.
 *
 * Text Commands:
 * -add [Twitter Link]   -- add a Twitter link to the bot's collection
 * -showCollection       -- show the bot's current collection of Artists and their handles with nav buttons
 * -clearCollection      -- empty the collection of all entries
 * -help                 -- show commands
 *
 * Nav Button Commands (these are attached to embeds):
 * Next                  -- move to the next entry in the collection
 * Prev                  -- move to the previous entry in the collection
 * Enter                 -- Only Available in DisplayArtists Mode: show the displayed Artist's collection of art
 * Exit                  -- DisplayArtists Mode: stop displaying the collection, DisplayArt Mode: return to Artist view
 * Remove                -- Remove the displayed Artist or Art and update the display appropriately
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ArtListener extends ListenerAdapter {
    private final MultiValueMap<String, String> m; //collection of art -- Artist links are keys, Art links are values
    private final EmbedBuilder eb;
    private String displayedArtist; //String link to currently displayed Artist
    private ArrayIterator<String> displayedLinks; //Iterator for currently displayed links
    private DisplayModeEnum displayMode;
    private Message displayMessage; //message displaying collection

    //Regex Patterns for Input Analysis -- THREAD SAFE
    private static final Pattern ADD_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "add \\s*\\w++");
    private static final Pattern SHOW_COLL_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "showCollection$");
    private static final Pattern CLEAR_COLL_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "clearCollection$");
    private static final Pattern HELP_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "help$");

    //Nav Buttons
    private static final Button PREV_BUTTON = Button.primary("PrevButton", "Previous");
    private static final Button NEXT_BUTTON = Button.primary("NextButton", "Next");


    /**
     * Enum determining what the Bot is currently displaying.
     *
     * DisplayOff: nothing is displayed
     * DisplayArtists: Artist embeds are being displayed
     * DisplayArt: Art embeds for a particular Artist are being displayed
     */
    private enum DisplayModeEnum{
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
        displayMessage = null;
    }

    //--- Event Listeners --------------------------------------------------------------
    /**
     * Reacts to user clicks on a button according to the nav button
     * commands specified at the top of this class:
     *
     * Nav Button Commands (these are attached to embeds):
     * Next                  -- move to the next entry in the collection
     * Prev                  -- move to the previous entry in the collection
     * Enter                 -- Only Available in DisplayArtists Mode: show the displayed Artist's collection of art
     * Exit                  -- DisplayArtists Mode: stop displaying the collection, DisplayArt Mode: return to Artist view
     * Remove                -- Remove the displayed Artist or Art and update the display appropriately
     *
     * @param event A ButtonInteractionEvent.
     */
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        //TODO: check if button id matches
    }

    /**
     * Reacts to user messages in the bot's message channel according
     * to the text Bot commands specified at the top of this class:
     *
     * Text Commands:
     * -add [Twitter Link]   -- add a Twitter link to the bot's collection
     * -showCollection       -- show the bot's current collection of Artists and their handles with nav buttons
     * -clearCollection      -- empty the collection of all entries
     * -help                 -- show commands
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
                onReceiveAddCommand(channel, messageRaw);
            }
            //-showCollection
            else if (showCollMatcher.find()){
                onReceiveShowCommand(event, channel);
            }
            //-clearCollection
            else if (clearCollMatcher.find()){
                onReceiveClearCommand(channel);
            }
            //-help
            else if (helpMatcher.find()){
                onReceiveHelpCommand(event, channel);
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
    private void onReceiveAddCommand(@NotNull MessageChannel channel, @NotNull String messageRaw){
        channel.sendMessage("I'll try to add that for you!").queue();

        String artLink = LinkUtil.trimLink(messageRaw.substring(5)); //remove command portion of message; trim to link
        int linkType = LinkUtil.determineLinkType(artLink);

        if (linkType == -1){ //Invalid link
            channel.sendMessage("Invalid Link. Are you sure you're using a Twitter *post* link? " +
                    "Profile links cannot be added directly.").queue();
            return;
        }
        else if (linkType == 0) { //Twitter Link --> turn into FXTwitter Link
            artLink = LinkUtil.twitToFXLink(artLink);
        }

        String artistLink = "";

        //Build Twitter Profile Link
        artistLink = LinkUtil.buildTwitProfileLink(artLink);

        //Try to add to map
        if (m.addValue(artistLink, artLink)) {
            //Send Confirmation Message
            channel.sendMessage("Added!").queue();

            //Restart Display if Open Already
            if (!displayMode.equals(DisplayModeEnum.DisplayOff)){
                openDisplay(channel);
                channel.sendMessage("Reloaded the display for you!").queue();
            }
        }
        else {
            //Send Failure Message
            channel.sendMessage("Sorry - I couldn't do that. That piece was already stored.").queue();
        }
    }

    /**
     * Attempts to show the collection of Artists, which can then be
     * interacted with to explore their respective art link collections.
     *
     * Does nothing if the collection is already being shown (Artists or Art).
     *
     * @param channel The MessageChannel to display the collection of artists in.
     */
    private void onReceiveShowCommand(MessageReceivedEvent event, @NotNull MessageChannel channel){
        openDisplay(channel);
    }

    /**
     * Asks for confirmation to clear the ArtListener's collection of art and artists.
     *
     * @param channel MessageChannel to send response in.
     */
    private void onReceiveClearCommand(MessageChannel channel){
        //TODO: implement
        channel.sendMessage("Are you sure you'd like to clear the collection?").queue();
        //add checkmark and x buttons for confirmation
    }

    private void onReceiveHelpCommand(MessageReceivedEvent event, MessageChannel channel){
        //TODO: implement

        //show all commands and their descriptions
        //show all nav buttons and their descriptions
    }

    //--- Nav Button Commands --------------------------------------------------------------
    private void onClickNext(ButtonInteractionEvent event){
        //TODO: implement

        //Get next link in iterator
            //If DisplayArtists, set displayedArtist equal to it
        //Edit original message to use this link
            //Ensures nav buttons remain?
    }

    private void onClickPrev(ButtonInteractionEvent event){
        //TODO: implement

        //Get prev link in iterator
            //If DisplayArtists, set displayedArtist equal to it
        //Edit original message to use this link
            //Ensures nav buttons remain?
    }

    private void onClickEnter(ButtonInteractionEvent event){
        //TODO: implement

        //Precond: assume we are in DisplayArtists mode, currently displayed artist link is stored in field
        //Grab array of values from map for artist
        //set array in iterator to this array
        //call next with the same event param
        //set mode to displayArt
    }

    private void onClickExit(ButtonInteractionEvent event){
        //TODO: implement

        //Display Art: return to view of artists in collection
            //displayArtistCollection()
        //Display Artists: exit display
            //exitDisplay()
    }

    private void onClickRemove(ButtonInteractionEvent event){
        //TODO: implement
        //grab link, and mode from original message

        //Ask for confirmation
            //Display Artists
                //Send message asking for confirmation: "Delete Artist " + link + "?"
            //Display Art
                //Send message asking for confirmation: "Delete Art " + link + " by " + displayedArtist + "?"
            //add checkmark and x emoji buttons to accept response

        //set mode to displayOff null out displayedArtist
    }

    private void onClickCancel(ButtonInteractionEvent event){
        //TODO: implement
        //delete original message
        //send confirmation message: "Action canceled!"
    }

    private void onClickConfirm(ButtonInteractionEvent event){
        //TODO: implement
        //parse original message (if string.contains("delete"), else)
            //trying to clear collection
                //delete original message
                //m.clear();
                //send confirmation message: "Collection cleared!"
            //trying to delete an entry
                //grab original message string, delete original message
                    //split string about " "
                    //check length of split
                        //length == 5
                            //grab artist link
                            //grab art link
                            //attempt remove
                                //success: "Removed! \n" + artlink
                                //failure: "Sorry, I couldn't remove that. \n" + artLink
                        //else
                            //grab artist link
                            //attempt remove
                                //success: "Removed! \n" + link
                                //failure: "Sorry, I couldn't remove that. \n" + artistlink
    }

    /**
     * Displays the current collection of Artists, if there are any.
     * If the collection is already being displayed, closes and
     * reopens it.
     *
     * @param channel MessageChannel to display Artists in.
     */
    private void openDisplay(MessageChannel channel){
        if(!displayMode.equals(DisplayModeEnum.DisplayOff)){
            exitDisplay(channel);
        }

        String[] keys = m.getKeys().toArray(new String[0]);

        if (keys.length == 0) { //No Artists to show :(
            channel.sendMessage("Nothing to show right now...").queue();
        }
        else { //Show artists!
            displayMode = DisplayModeEnum.DisplayArtists;
            displayedLinks.setArray(m.getKeys().toArray(new String[0]));
            displayedArtist = displayedLinks.next();

            channel.sendMessage(displayedArtist)
                    .setActionRow(
                            PREV_BUTTON,
                            NEXT_BUTTON)
                    .queue(
                            message -> displayMessage = message);
            //TODO: add actionrow

        }
    }

    /**
     * Closes the collection display if it is open.
     *
     * @param channel The channel the display is open in.
     */
    private void exitDisplay(MessageChannel channel){
        if (!displayMode.equals(DisplayModeEnum.DisplayOff)){
            //Delete Embed
            displayMessage.delete().queue();
            displayMessage = null;

            //Set Display Mode to DisplayOff
            displayMode = DisplayModeEnum.DisplayOff;

            //Null Out Displayed Artist and Displayed Links Array
            displayedArtist = null;
            displayedLinks.setArray(null);
        }
    }
}
