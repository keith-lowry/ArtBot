package lolcatloyal.ArtBot;

import java.util.*;

/**
 * A Collection of art links arranged with
 * artist profile links stored as keys and arrays of art links
 * sorted by most recently added link stored as values.
 *
 * The collection does not check whether a key or value being
 * added is a link.
 *
 * Artist profile links cannot be added separately to the collection. Instead,
 * they are added when necessary when a new art link is added.
 *
 * An array of sorted artist profile links can be obtained from
 * the Collection.
 *
 * An array of art links for a certain artist profile link can
 * be obtained from the Collection.
 */
public class ArtLinkCollection {
    private final SortedMap<String, List<String>> collection;

    /**
     * Initializes a new, empty ArtCollection.
     */
    public ArtLinkCollection(){
        collection = new TreeMap<String, List<String>>();
    }

    /**
     * Initializes a new ArtCollection with the given
     * map of artist handles and  Links.
     *
     * @param map SortedMap with artist handles and lists of art links
     *            as Keys and Values.
     * @precon map is a SortedMap of artist links and their associated list of art links.
     */
    public ArtLinkCollection(SortedMap<String, List<String>> map){
        collection = map;
    }

    /**
     * Gets an array of the currently stored
     * Artist links in alphabetical order.
     *
     * @return A sorted array of Artist links.
     */
    public String[] getArtistLinks(){
        return collection.keySet().toArray(new String[0]);
    }

    /**
     * Gets an array of the given artist link's
     * currently stored art links.
     *
     * @param artistLink The profile link of the desired artist.
     * @return The desired artist's collection of links
     *         if the artist is in the collection. Returns
     *         null otherwise.
     */
    public String[] getArtLinks(String artistLink){
        if (collection.containsKey(artistLink)){
            return collection.get(artistLink).toArray(new String[0]);
        }

        return null;
    }

    /**
     * Attempts to add an art link associated with
     * a specific artist link to the
     * collection. If the link is already in the
     * collection, it is not added.
     *
     * artLinks are added to the beginning of an
     * artist's collection.
     *
     * Adds the given artist link as well
     * if it is not already in the collection.
     *
     * @param artistLink Link to artist's profile.
     * @param artLink Art post link to add to the Collection.
     * @precond artistLink and artLink are nonnull links to an artist's social
     * page and one of the artist's pieces.
     * @return True if the link was added to the
     *         collection, false otherwise.
     */
    public boolean addArtLink(String artistLink, String artLink){

        //add artistLink to collection as key if needed
        addArtistLink(artistLink);

        List<String> links = collection.get(artistLink);

        //check for duplicates
        if (links.contains(artLink)){
            return false;
        }

        //add link to start
        links.add(0, artLink);
        return true;
    }

    /**
     * Attempts to remove the given art link from
     * the collection if it is in the collection.
     *
     * If this is the last link stored for a given
     * artist link, removes the artist link from the collection
     * as well.
     *
     * @param artLink Art link stored in the collection.
     * @param artistLink The artistLink associated with the art link.
     * @return True if removal was successful, false
     *         otherwise.
     */
    public boolean removeArtLink(String artistLink, String artLink){
        if (!collection.containsKey(artistLink)){
            return false;
        }

        List<String> links = collection.get(artistLink);

        if(links.remove(artLink)){
            if (links.size() == 0){
                collection.remove(artistLink);
            }
            return true;
        }

        //Not in list
        return false;
    }

    /**
     * Attempts to remove the given artist
     * link from the collection, along
     * with their art links.
     *
     * If the given artist link is not in the
     * collection already, no change
     * will occur.
     *
     * @param artistLink Desired Artist link to remove.
     * @return True if removal was successful, false
     *         otherwise.
     */
    public boolean removeArtistLink(String artistLink){
        //TODO
        return false;
    }

    /**
     * Adds a new artistLink to the collection if the
     * collection does not already contain it.
     *
     * @param artistLink Profile link of the artist to
     *               add to the collection.
     * @precond artistLink is nonnull.
     */
    private void addArtistLink(String artistLink) {
        if (!collection.containsKey(artistLink)){
            collection.put(artistLink, new ArrayList<String>());
        }
    }
}
