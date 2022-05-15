package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
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
@SuppressWarnings("ALL")
public class ArtListener extends ListenerAdapter {
    private final MultiValueMap<String, String> m; //collection of art -- Artist links are keys, Art links are values
    //private final EmbedBuilder eb;
    private String displayedArtist; //String link to currently displayed Artist
    private String displayedArt; //String link to currently displayed Art
    private static final String removeArtPrompt = "Remove Art? \n";
    private static final String removeArtistPrompt = "Remove Artist? \n";
    private static final String removeFailureResponse ="Sorry, I couldn't remove that.";
    private static final String removeSuccessResponse = "Removed!";
    private final ArrayIterator<String> links; //Iterator for currently displayed links
    private DisplayModeEnum displayMode;
    private Message displayMessage; //message displaying collection

    //Regex Patterns for Input Analysis -- THREAD SAFE
    private static final Pattern ADD_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "add \\s*\\w++");
    private static final Pattern SHOW_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "show$");
    private static final Pattern CLEAR_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "clear$");
    private static final Pattern HELP_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "help$");
    private static final Pattern QUIT_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "quit$");
    //TODO: remove patterns for simple commands (EVERYTHING OTHER THAN ADD COMMAND), can just use String.equals()

    //Nav Buttons
    private static final String PREV_BUTTON_ID = "Previous";
    private static final String NEXT_BUTTON_ID = "Next";
    private static final String EXIT_BUTTON_ID = "Exit";
    private static final String ENTER_BUTTON_ID = "Enter";
    private static final String REMOVE_BUTTON_ID = "Remove";
    private static final List<Button> navActionRow = Arrays.asList(     //Action row for navigating collection
                    Button.primary(PREV_BUTTON_ID, PREV_BUTTON_ID),
                    Button.primary(NEXT_BUTTON_ID, NEXT_BUTTON_ID),
                    Button.primary(EXIT_BUTTON_ID, EXIT_BUTTON_ID),
                    Button.primary(ENTER_BUTTON_ID, ENTER_BUTTON_ID),
                    Button.primary(REMOVE_BUTTON_ID, REMOVE_BUTTON_ID));
    private static final List<Button> disabledNavActionRow = Arrays.asList(
                    Button.primary(PREV_BUTTON_ID, PREV_BUTTON_ID).asDisabled(),
                    Button.primary(NEXT_BUTTON_ID, NEXT_BUTTON_ID).asDisabled(),
                    Button.primary(EXIT_BUTTON_ID, EXIT_BUTTON_ID).asDisabled(),
                    Button.primary(ENTER_BUTTON_ID, ENTER_BUTTON_ID).asDisabled(),
                    Button.primary(REMOVE_BUTTON_ID, REMOVE_BUTTON_ID).asDisabled());
    
    //Confirmation Buttons
    private static final String CONFIRM_BUTTON_ID = "Confirm";
    private static final String CANCEL_BUTTON_ID = "Cancel";
    private static final List<Button> promptActionRow = Arrays.asList(   //Action row for confirmation prompt
                    Button.primary(CONFIRM_BUTTON_ID, CONFIRM_BUTTON_ID),
                    Button.primary(CANCEL_BUTTON_ID, CANCEL_BUTTON_ID));


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
        DisplayArt,
        DisplayPrompt
    }

    /**
     * Creates a new ArtListener with
     * an empty collection.
     */
    public ArtListener(){
        m = new MultiValueMap<>();
        //eb = new EmbedBuilder();
        links = new ArrayIterator<>(new String[0]);
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
        if (displayMessage != null) { //Display is on
            if (displayMessage.getId().equals(event.getMessageId())) { //Click on our DisplayMessage
                MessageChannel channel = event.getChannel();
                switch (event.getComponentId()) {
                    case PREV_BUTTON_ID:
                        onClickPrev(event);
                        break;
                    case NEXT_BUTTON_ID:
                        onClickNext(event);
                        break;
                    case EXIT_BUTTON_ID:
                        onClickExit(event);
                        break;
                    case ENTER_BUTTON_ID:
                        onClickEnter(event);
                        break;
                    case REMOVE_BUTTON_ID:
                        onClickRemove(event);
                        break;
                    case CANCEL_BUTTON_ID:
                        onClickCancelRemove(event);
                        break;
                    case CONFIRM_BUTTON_ID:
                        onClickConfirmRemove(event);
                        break;
                }
            }
        }
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
            Matcher showMatcher = SHOW_COMMAND_PATTERN.matcher(messageRaw);
            Matcher clearMatcher = CLEAR_COMMAND_PATTERN.matcher(messageRaw);
            Matcher helpMatcher = HELP_COMMAND_PATTERN.matcher(messageRaw);
            Matcher quitMatcher = QUIT_COMMAND_PATTERN.matcher(messageRaw);

            //-add [Twitter Link]
            if (addMatcher.find()) {
                onReceiveAddCommand(channel, messageRaw);
            }
            //-showCollection
            else if (showMatcher.find()){
                onReceiveShowCommand(channel);
            }
            //-clearCollection
            else if (clearMatcher.find()){
                onReceiveClearCommand(channel);
            }
            //-help
            else if (helpMatcher.find()){
                onReceiveHelpCommand(channel);
            }
            //-quit
            else if (quitMatcher.find()){
                channel.sendMessage("Shutting down...").queue();
                System.exit(0);
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

        //Build Twitter Profile Link
        String artistLink = LinkUtil.buildTwitProfileLink(artLink);

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
    private void onReceiveShowCommand(@NotNull MessageChannel channel){
        openDisplay(channel);
    }

    /**
     * Asks for confirmation to clear the ArtListener's collection of art and artists
     * if the collection is not already empty.
     *
     * @param channel MessageChannel to send response in.
     */
    private void onReceiveClearCommand(MessageChannel channel){
        //TODO: implement
        if (m.isEmpty()){
            channel.sendMessage("The collection is already empty.").queue();
        }
        else {
            channel.sendMessage("Are you sure you'd like to clear the collection?")
                    .setActionRow(
                            Button.primary("Yes", "Yes"),
                            Button.primary("No", "No"))
                    .queue();
        }
    }

    /**
     * Displays a message describing and explaining user commands.
     *
     * @param event MessageReceivedEvent that triggered this command.
     */
    private void onReceiveHelpCommand(MessageChannel channel){
        channel.sendMessage("**Text Commands**\n " +
                    "(these are preceeded by '" + ArtBot.PREFIX + "' )" +
                    "\n\nadd [Twitter Link]   -- add a Twitter link to the bot's collection" +
                    "\nshow        -- show the bot's current collection of Artists and their handles with nav buttons" +
                    "\nclear         -- empty the collection of all entries" +
                    "\nhelp                             -- show commands help" +
                    "\nquit                             -- shut down the bot" +
                    "\n\n**Nav Button Commands**" +
                    "\n(these are attached to embeds)" +
                    "\n\nNext                          -- move to the next entry in the collection" +
                    "\nPrevious                   -- move to the previous entry in the collection" +
                    "\nEnter                         -- Only Available in DisplayArtists Mode: show the displayed Artist's collection of art" +
                    "\nExit                            -- DisplayArtists Mode: stop displaying the collection, DisplayArt Mode: return to Artist view" +
                    "\nRemove                    -- Remove the displayed Artist or Art and update the display appropriately")
                .queue();
    }

    //--- Nav Button Commands --------------------------------------------------------------

    /**
     * Displays the next link in the iterator.
     *
     * @param event Button click that triggered this command.
     * @precond The display is on.
     */
    private void onClickNext(ButtonInteractionEvent event){
        //Get next link in iterator
        String linkToDisplay = links.next();

        //Set displayedArtist if needed
        if (displayMode.equals(DisplayModeEnum.DisplayArtists)){
            displayedArtist = linkToDisplay;
        }
        //Set displayedArt if needed
        else {
            displayedArt = linkToDisplay;
        }

        //Edit message to display next link
        event.editMessage(linkToDisplay).queue();
    }

    /**
     * Displays the previous link in the iterator.
     *
     * @param event Button click that triggered this command.
     * @orecond The display is on.
     */
    private void onClickPrev(ButtonInteractionEvent event){
        String linkToDisplay = links.prev();

        if (displayMode.equals(DisplayModeEnum.DisplayArtists)){
            displayedArtist = linkToDisplay;
        }
        else {
            displayedArt = linkToDisplay;
        }

        event.editMessage(linkToDisplay).queue();
    }

    /**
     * Displays the displayed artist's colleection of art links.
     *
     * @param event Button click that triggered this command.
     * @precond displayMode is DisplayArtists.
     */
    private void onClickEnter(ButtonInteractionEvent event){
        links.setArray(m.getValuesForKey(displayedArtist).toArray(new String[0]));
        displayedArt = links.next();
        event.editMessage(displayedArt).queue();
        event.editButton(event.getButton().asDisabled()).queue(); //disable enter button
        displayMode = DisplayModeEnum.DisplayArt;
    }

    /**
     * Exits the display depending on the display's current mode.
     * If artists are being displayed, closes the display entirely.
     * If art is being displayed, returns to display of artists.
     *
     * @param event The Button click that triggered this command.
     * @precond The display is on.
     */
    private void onClickExit(ButtonInteractionEvent event){
        //Displaying Art --> return to DisplayArtists Mode
        if(displayMode.equals(DisplayModeEnum.DisplayArt)){
            displayMode = DisplayModeEnum.DisplayArtists;
            links.setArray(m.getKeys().toArray(new String[0]));
            displayedArtist = links.next();
            displayedArt = null;

            //Edit Message
            event.editMessage(displayedArtist)
                    .setActionRow(navActionRow)
                    .queue();
        }
        else { //Displaying Artists --> close display
            exitDisplay();
        }
    }

    /**
     *
     * @param event The Button click that triggered this command.
     * @precond The display is on.
     */
    private void onClickRemove(ButtonInteractionEvent event){
        //Delete Artist - Send prompt message
        if(displayMode.equals(DisplayModeEnum.DisplayArtists)){
            event.editMessage(removeArtistPrompt + displayedArtist).setActionRow(promptActionRow).queue();
        }
        //Delete Art - Send prompt message
        else {
            event.editMessage(removeArtPrompt + displayedArt).setActionRow(promptActionRow).queue();
        }

        //Now Displaying a Prompt
        displayMode = DisplayModeEnum.DisplayPrompt;
    }

    private void onClickCancelRemove(ButtonInteractionEvent event){
        //Send Cancellation Message
        event.editMessage("Cancelled!").setActionRows().queue();

        //Display Off
        displayMode = DisplayModeEnum.DisplayOff;
    }

    /**
     * Attempts to remove the desired entry (Artist or Art).
     *
     * @param event Button click that triggered this action.
     * @precond Display is on.
     */
    private void onClickConfirmRemove(ButtonInteractionEvent event){
        boolean success = false;

        //Remove Artist
        if(event.getMessage().getContentRaw().contains(removeArtistPrompt)){
            success = m.removeKey(displayedArtist);
        }
        //Remove Art
        else {
            success = m.removeValue(displayedArtist, displayedArt);
        }

        //Close Display
        exitDisplay();

        //Send Confirmation Message
        if (success) {
            event.reply(removeSuccessResponse).queue();
        }
        else {
            event.reply(removeFailureResponse).queue();
        }


        //TODO: implement
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
        //Close display if it's already open
        if(!displayMode.equals(DisplayModeEnum.DisplayOff)){
            exitDisplay();
        }

        //Get Artists
        String[] keys = m.getKeys().toArray(new String[0]);

        if (keys.length == 0) { //No Artists to show :(
            channel.sendMessage("Nothing to show right now...").queue();
        }
        else { //Show artists!
            displayMode = DisplayModeEnum.DisplayArtists;
            links.setArray(m.getKeys().toArray(new String[0]));
            displayedArtist = links.next();

            channel.sendMessage(displayedArtist)
                    .setActionRow(navActionRow)
                    .queue(message -> displayMessage = message);
        }
    }

    /**
     * Closes the collection display if it is open.
     */
    private void exitDisplay(){
        if (!displayMode.equals(DisplayModeEnum.DisplayOff)){
            //Delete Embed
            displayMessage.delete().queue();
            displayMessage = null;

            //Set Display Mode to DisplayOff
            displayMode = DisplayModeEnum.DisplayOff;

            //Null Out Displayed Artist and Displayed Links Array
            displayedArtist = null;
            links.setArray(null);
        }
    }
}
