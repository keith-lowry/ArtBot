package lolcatloyal.ArtBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Utility class for handling miscellaneous tasks
 * like building an embed for an artist's twitter,
 * or determining the type of a link.
 */
public class Util {
    private EmbedBuilder eb;

    /**
     * Initializes a new Util object with its own
     * EmbedBuilder.
     */
    public Util(){
        eb = new EmbedBuilder();
    }

    /**
     * Gets a MessageEmbed for the artist with the given handle.
     *
     * @param handle The social handle for the given artist.
     * @return A MessageEmbed leading to the artist's social account.
     */
    public MessageEmbed getArtistEmbed(String handle){
        //TODO
        return null;
    }

    /**
     * Gets a MessageEmbed for the given art link.
     *
     * @param link Online link to a piece of art.
     *             NOTE: currently only works for twitter links
     * @return A MessageEmbed leading to the piece of art.
     */
    public MessageEmbed getArtEmbed(String link){
        //TODO
        return null;
    }

    /**
     * Determines the type of website an art link leads to.
     * NOTE: links must be to a piece of art, NOT a user's profile.
     *
     * @param link Online link to a piece of art.
     * @return 0 for Twitter links,
     *         -1 for invalid links
     */
    public static int parseLinkType(String link){
        //TODO
        return 0;
    }

    /**
     * Gets the twitter handle from a given Twitter
     * Art Link.
     *
     * @param link A twitter art link.
     * @return The handle of the art poster.
     */
    public static String parseTwitterHandleFromLink(String link){
        //TODO
        return null;
    }

    /**
     * Gets a link to a user's profile using their handle and the type
     * of website they are on.
     *
     * @param handle A nonnull String representing a User's handle.
     * @param websiteType An integer representing the type of website to link to.
     *                    0 for Twitter,
     * @return A link to the profile of a user with the given handle and
     *         website type.
     */
    private String getProfileLinkFromHandle(String handle, int websiteType){
        //TODO
        if (handle == null || websiteType == -1){
            return null;
        }
        return "";
    }

}
