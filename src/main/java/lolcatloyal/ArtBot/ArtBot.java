package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

/**
 * Discord bot for storing the creator's favorite Twitter art links
 * for easy retrieval. The user can add an art link to the
 * bot which will be stored under the artist's handle.
 *
 * Commands:
 * -add [Twitter Link]   -- add a Twitter link to the bot's collection
 * -showArtists          -- show the bot's current collection of Artists and their handles
 * -showCollection       -- show the currently displayed Artist's collection of art (if one is displayed)
 * -help                 -- show command help
 *
 */
public class ArtBot {

    public static JDABuilder builder;

    public static final String PREFIX = "-"; //Command Prefix
    public static final String CHANNEL_ID = "875121326921248791"; //ID for TextChannel
    //TODO: add constants for navigation reaction emotes



    public static void main(String[] args) throws LoginException {

        builder = JDABuilder.createDefault(args[0]);

        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);

        builder.setBulkDeleteSplittingEnabled(false);

        builder.setCompression(Compression.NONE);

        builder.setActivity(Activity.playing("-help"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);

        registerListeners();

        builder.build();
    }

    /**
     * Adds any desired listeners to the bot.
     */
    private static void registerListeners() {
        builder.addEventListeners(new ArtListener());
    }

}