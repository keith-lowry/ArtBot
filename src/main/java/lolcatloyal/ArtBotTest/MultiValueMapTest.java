package lolcatloyal.ArtBotTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import lolcatloyal.ArtBot.MultiValueMap;

/**
 * Test class for MultiValueMap's methods.
 */
@SuppressWarnings("Convert2Diamond")
class MultiValueMapTest {
    private MultiValueMap<String, String> m;
    private final String key1 = "Fruit";
    private final String key2 = "Stores";
    private final String[] values1 = {"Apples", "Oranges", "Bananas"};
    private final String[] values2 = {"Harris Teeter", "Safeway", "Walmart", "Target"};
    private final String[] keys = {key1, key2};


    @BeforeEach
    void setUp() {
        m = new MultiValueMap<String, String>();
    }

    @Test
    void getKeys() {
        //Empty Map
        assertArrayEquals(new String[]{}, m.getKeys().toArray(new String[0]));

        //Map with 2 Keys
        m.addValue(key1, values1[0]);
        m.addValue(key2, values2[0]);
        String[] keysReceived = m.getKeys().toArray(new String[0]);
        assertEquals(keys.length, keysReceived.length);
        assertArrayEquals(keys, keysReceived);
    }

    @Test
    void getValuesForKey() {
        //Empty Map
        assertNull(m.getValuesForKey(key1));

        //Add values in reverse order
        for (int i = values1.length - 1; i > -1; i--){
            m.addValue(key1, values1[i]);
        }

        //Map with 1 Key, Multiple Values
        String[] valuesReceived = m.getValuesForKey(key1).toArray(new String[0]);
        assertArrayEquals(values1, valuesReceived);

    }

    @Test
    void addValue() {
        //Empty Map
        assertEquals(0, m.getKeys().size());

        assertTrue(m.addValue(key1, values1[0]));
        assertArrayEquals(new String[]{values1[0]}, m.getValuesForKey(key1).toArray(new String[0]));
        assertEquals(1, m.getKeys().size());

        //Add duplicate
        assertFalse(m.addValue(key1, values1[0]));
        assertArrayEquals(new String[]{values1[0]}, m.getValuesForKey(key1).toArray(new String[0]));

        //Add New Key
        assertTrue(m.addValue(key2, values2[0]));
        assertArrayEquals(new String[]{values2[0]}, m.getValuesForKey(key2).toArray(new String[0]));

        //Add duplicate value for New Key
        assertTrue(m.addValue(key2, values1[0]));
        assertArrayEquals(new String[]{values1[0], values2[0]}, m.getValuesForKey(key2).toArray(new String[0]));
    }

    @Test
    void removeValue() {
        //Empty Map
        assertFalse(m.removeValue(key1, values1[0]));

        m.addValue(key1, values1[0]);

        //Remove value not in map for key
        assertFalse(m.removeValue(key1, values1[1]));

        //Remove value for key not in map
        assertFalse(m.removeValue(key2, values1[0]));

        //Remove value for key
        assertTrue(m.removeValue(key1, values1[0]));
        assertEquals(m.getKeys().size(), 0); //last value for key --> remove key
        assertNull(m.getValuesForKey(key1));
    }

    @Test
    void removeKey() {
        //Empty Map
        assertFalse(m.removeKey(key1));

        m.addValue(key1, values1[0]);
        assertEquals(1, m.getValuesForKey(key1).size());

        //Remove Key not in Map
        assertFalse(m.removeKey(key2));

        //Remove Key in Map
        assertTrue(m.removeKey(key1));
        assertNull(m.getValuesForKey(key1));
    }

    @Test
    void clear() {
        //Fill up Map
        for(int i = values1.length - 1; i > -1; i--){
            m.addValue(key1, values1[i]);
        }
        for(int i = values2.length - 1; i > -1; i--){
            m.addValue(key2, values2[i]);
        }

        assertEquals(values1.length, m.getValuesForKey(key1).size());
        assertEquals(values2.length, m.getValuesForKey(key2).size());
        assertEquals(2, m.getKeys().size());

        //Clear map
        m.clear();

        assertNull(m.getValuesForKey(key1));
        assertNull(m.getValuesForKey(key2));
        assertEquals(0, m.getKeys().size());
    }
}