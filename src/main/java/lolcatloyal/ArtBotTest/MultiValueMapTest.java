package lolcatloyal.ArtBotTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import lolcatloyal.ArtBot.MultiValueMap;
import java.util.Arrays;

class MultiValueMapTest {
    private MultiValueMap<String> m;
    private final String key1 = "Fruit";
    private final String key2 = "Stores";
    private final String[] values1 = {"Apples", "Oranges", "Bananas"};
    private final String[] values2 = {"Harris Teeter", "Safeway", "Walmart", "Target"};
    private final String[] keys = {key1, key2};


    @BeforeEach
    void setUp() {
        m = new MultiValueMap<String>();
    }

    @Test
    void getKeys() {
        m.addValue(key1, values1[0]);
        m.addValue(key2, values2[0]);
        String[] keysReceived = m.getKeys();
        assertEquals(keys.length, keysReceived.length);
        assertTrue(Arrays.equals(keys, keysReceived));
    }

    @Test
    void getValuesForKey() {
    }

    @Test
    void addValue() {
    }

    @Test
    void removeValue() {
    }

    @Test
    void removeKey() {
    }

    @Test
    void clear() {
    }
}