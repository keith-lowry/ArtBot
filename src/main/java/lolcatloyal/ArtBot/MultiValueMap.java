package lolcatloyal.ArtBot;

import java.util.*;

/**
 * Map that stores String keys with multiple E type values.
 * Keys are stored in sorted alphabetical order.
 * Values are stored in LIFO order.
 */
public class MultiValueMap<E> {
    private SortedMap<String, List<E>> map;

    /**
     * Creates a new empty MultiValueMap.
     */
    public MultiValueMap() {
        map = new TreeMap<String, List<E>>();
    }

    /**
     * Creates a new MultiValueMap with the given map.
     * @param m A SortedMap of String Keys and associated E values.
     */
    public MultiValueMap(SortedMap<String, List<E>> m){
        map = m;
    }

    /**
     * Gets an array of Keys stored in the map sorted
     * in alphabetical order.
     *
     * @return A sorted String array containing the Keys stored in the map.
     */
    public String[] getKeys(){
        return map.keySet().toArray(new String[0]);
    }

    /**
     * Gets an array of the Values associated with a given Key sorted
     * in LIFO order. Returns null if the given Key is not in the map.
     *
     * @param key Key for the desired Values in the map.
     * @return A sorted array of Values for the desired Key. Null if the
     *             given key is not in the map.
     */
    public E[] getValuesForKey(String key){
        //Case 1: key is in map
        if (map.containsKey(key)){
            return (E[]) map.get(key).toArray(new Object[0]);
        }

        //Case 2: key not in map
        return null;
    }

    /**
     * Adds a Value to the map with a given Key. If the Key is not in
     * the map already, adds the Key as well. Fails if Value is already
     * associated with the given Key.
     *
     * @param key Key for desired Value to add to the map.
     * @param value Desired Value to add to the map.
     * @return True if Value was not already associated with the given
     *             Key.
     */
    public boolean addValue(String key, E value){
        //Case 1: key not in map
        if (!map.containsKey(key)){
            List<E> l = (List<E>) new ArrayList<Object>();
            l.add(value);
            map.put(key, l); //Add key and value to map
            return true;
        }

        //Case 2: key in map
        List<E> values = map.get(key);

        //Check for duplicates
        if (values.contains(value)){
            return false;
        }

        values.add(0, value); //Add value to front of list
        return true;
    }

    /**
     * Removes the given Value associated with a given Key from
     * the map if the Key is in the Map and the Value is associated
     * with it.
     *
     * @param key Key associated with a given Value to remove.
     * @param value Desired Value to remove.
     * @return True if the Key was in the map and was mapped to the
     *              given Value.
     */
    public boolean removeValue(String key, E value){
        //Case 1: key not in map
        if (!map.containsKey(key)){
            return false;
        }

        //Case 2: key in map
        List<E> values = map.get(key);

        //Value is in list and has been removed
        if (values.remove(value)){
            if (values.size() == 0){
                map.remove(key); //No associated values --> remove key
            }
            return true;
        }

        return false; //Value is not in List
    }

    /**
     * Removes the given Key from the map if it is
     * in the map.
     *
     * @param key Desired Key to remove.
     * @return True if the Key was in the map.
     */
    public boolean removeKey(Object key){
        return map.remove(key) != null;
    }

    /**
     * Empties the map of all Key and Value mappings.
     */
    public void clear(){
        map = new TreeMap<String, List<E>>();
    }

}
