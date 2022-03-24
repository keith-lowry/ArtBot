package lolcatloyal.ArtBotTest;

import lolcatloyal.ArtBot.ArrayIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayIteratorTest {
    private Integer[] a;
    private String[] b;
    private ArrayIterator<Integer> iA;
    private ArrayIterator<String> iB;

    //TODO: make ArrayIteratorTest
    @BeforeEach
    void setUp() {
        a = new Integer[]{1, 2, 3, 4};
        b = new String[]{"a", "b", "c", "d"};
        iA = new ArrayIterator<Integer>(a);
        iB = new ArrayIterator<String>(b);
    }

    @Test
    void setArray() {

    }

    @Test
    void next() {
        for (int i = 0; i < a.length; i++){
            assertEquals(iA.next(), a[i]);
        }

        for (int i = 0; i < b.length; i++){
            assertEquals(iB.next(), b[i]);
        }
    }

    @Test
    void prev() {
        for (int i = a.length - 1; i > 0; i--){
            assertEquals(iA.prev(), a[i - 1]);
        }
        assertEquals(iA.prev(), a[a.length - 1]); //Cycle back to last element

        for (int i = b.length - 1; i > 0; i--){
            assertEquals(iB.prev(), b[i - 1]);
        }

        assertEquals(iB.prev(), b[b.length - 1]); //Cycle back to last element
    }
}