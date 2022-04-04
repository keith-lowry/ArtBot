package lolcatloyal.ArtBot;

import java.util.*;

/**
 * Map that stores K type keys with multiple E type values.
 *
 * Keys must extend Comparable<K>. Keys are stored in ascending order according to
 * their compareTo() implementation.
 *
 * Values are stored in LIFO order.
 *
 * @param <K> The type of Keys to be stored
 * @param <V> The type of Values to be stored
 */
@SuppressWarnings("unused, Convert2Diamond")
public class MultiValueMap<K extends Comparable<K>, V> {
    private SortedMap<K, List<V>> map;

    /**
     * Creates a new empty MultiValueMap.
     */
    public MultiValueMap() {
        map = new TreeMap<K, List<V>>();
    }

    /**
     * Creates a new MultiValueMap with the given map.
     *
     * @param m A SortedMap of K Keys and associated List<V> holding values for each key.
     * @precond m is nonnull
     */
    public MultiValueMap(SortedMap<K, List<V>> m){
        map = m;
    }

    /**
     * Gets a Set of Keys stored in the map sorted
     * in alphabetical order.
     *
     * @return A Set containing the map's keys in alphabetical order.
     */
    public Set<K> getKeys(){
       return map.keySet();
    }

    /**
     * Gets a List of the Values associated with a given Key sorted
     * in LIFO order. Returns null if the given Key is not in the map.
     *
     * @param key Key for the desired Values in the map.
     * @precond key is nonnull
     * @return A List of Values mapped from the desired Key arranged in LIFO order. Null if the
     *             given key is not in the map.
     */
    public List<V> getValuesForKey(K key){
        //Case 1: key is in map
        if (map.containsKey(key)){
            return map.get(key);
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
     * @precond key, value are nonnull
     * @return True if Value was not already associated with the given
     *             Key.
     */
    public boolean addValue(K key, V value){
        //Case 1: key not in map
        if (!map.containsKey(key)){
            List<V> l = new ArrayList<V>();
            l.add(value);
            map.put(key, l); //Add key and value to map
            return true;
        }

        //Case 2: key in map
        List<V> values = map.get(key);

        //Check for duplicates
        if (values.contains(value)){
            return false;
        }
        values.add(0, value);
        return true;
    }

    /**
     * Removes the given Value associated with a given Key from
     * the map if the Key is in the Map and the Value is associated
     * with it.
     *
     * In addition, removes Key if the Value to remove was
     * its last associated Value.
     *
     * Fails if Key was not in the map, or was not mapped to Value.
     *
     * @param key Key associated with a given Value to remove.
     * @param value Desired Value to remove.
     * @precond key, value are nonnull
     * @return True if the Key was in the map and was mapped to the
     *              given Value.
     */
    public boolean removeValue(K key, V value){
        //Case 1: key not in map
        if (!map.containsKey(key)){
            return false;
        }

        //Case 2: key in map
        List<V> values = map.get(key);

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
     * @precond key is nonnull
     * @return True if the Key was in the map.
     */
    public boolean removeKey(K key){
        return map.remove(key) != null;
    }

    /**
     * Empties the map of all Key and Value mappings.
     */
    public void clear(){
        map = new TreeMap<K, List<V>>();
    }

    /**
     * Checks whether the map is empty.
     *
     * @return True if the map is empty.
     */
    public boolean isEmpty() { return map.isEmpty(); }

}
