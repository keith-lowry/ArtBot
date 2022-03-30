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

    //Regex Patterns for Input Analysis -- THREAD SAFE
    private static final Pattern ADD_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "add \\s*\\w++");
    private static final Pattern SHOW_COLL_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "showCollection$");
    private static final Pattern CLEAR_COLL_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "clearCollection$");
    private static final Pattern HELP_COMMAND_PATTERN = Pattern.compile("^" + ArtBot.PREFIX + "help$");

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

        String artLink = LinkUtil.trimLink(messageRaw.substring(5)); //remove command portion of command and trim to link
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
    private void onClickNext(ButtonInteractionEvent event){
        //TODO: implement

        //show next link in iterator, add to currently displayed artist if needed
    }

    private void onClickPrev(ButtonInteractionEvent event){
        //TODO: implement

        //show prev link in iterator, add to currently displayed artist if needed
    }

    private void onClickEnter(ButtonInteractionEvent event){
        //TODO: implement

        //show the currently displayed artist's collection
    }

    private void onClickExit(ButtonInteractionEvent event){
        //TODO: implement

        //Display Art: return to view of artists in collection
        //Display Artists: delete embed
    }

    private void onClickRemove(ButtonInteractionEvent event){
        //TODO: implement

        //Ask for confirmation

        //DisplayArtists: remove artist, show next artist if there is one; else exit entirely
        //DisplayArt: remove art, show next art if there is one; else return to artist view if possible; else exit entirely
    }
}
