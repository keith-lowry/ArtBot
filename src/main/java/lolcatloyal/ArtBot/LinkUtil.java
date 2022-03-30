package lolcatloyal.ArtBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class holding static helper methods for handling links.
 */
public class LinkUtil {
    //Regex patterns for identifying valid links
    private static final Pattern TWIT_PATTERN = Pattern.compile("^https://twitter\\.com/.+/status/.+");
    private static final Pattern FX_PATTERN = Pattern.compile("^https://fxtwitter\\.com/.+/status/.+");

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
    public static int determineLinkType(String link){
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
    public static String trimLink(String link){
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
    public static String twitToFXLink(String twitLink){
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
    public static String buildTwitProfileLink(String postLink){
        String[] linkParts = postLink.split("/");
        String twitterHandle = linkParts[3];
        return "https://twitter.com/" + twitterHandle;
    }
}
