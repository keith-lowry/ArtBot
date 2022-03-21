package lolcatloyal.ArtBot;

import java.util.*;

public class StringListMap {
    private SortedMap<String, List<String>> map;

    public StringListMap() {
        map = new TreeMap<String, List<String>>();
    }

    public StringListMap(SortedMap<String, List<String>> m){
        map = m;
    }

    public String[] getKeys(){
        return map.keySet().toArray(new String[0]);
    }

    public String[] getValuesForKey(String key){
        //Case 1: key is in map
        if (map.containsKey(key)){
            return map.get(key).toArray(new String[0]);
        }

        //Case 2: key not in map
        return null;
    }

    public boolean addValue(String key, String value){
        //Case 1: key not in map
        if (!map.containsKey(key)){
            List<String> l = new ArrayList<String>();
            l.add(value);
            map.put(key, l); //Add key and value to map
            return true;
        }

        //Case 2: key in map
        List<String> values = map.get(key);

        //Check for duplicates
        if (values.contains(value)){
            return false;
        }

        values.add(0, value); //Add value to front of list
        return true;
    }

    public boolean removeValue(String key, String value){
        //Case 1: key not in map
        if (!map.containsKey(key)){
            return false;
        }

        //Case 2: key in map
        List<String> values = map.get(key);

        //Value is in list and has been removed
        if (values.remove(value)){
            if (values.size() == 0){
                map.remove(key); //No associated values --> remove key
            }
            return true;
        }

        return false; //Value is not in List
    }

    public boolean removeKey(String key){
        return map.remove(key) != null;
    }

    public void clear(){
        map = new TreeMap<String, List<String>>();
    }

}
