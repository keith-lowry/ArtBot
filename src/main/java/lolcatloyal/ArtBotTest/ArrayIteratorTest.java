package lolcatloyal.ArtBotTest;

import lolcatloyal.ArtBot.ArrayIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayIteratorTest {
    private Integer[] a1;
    private Integer[] a2;
    private String[] b;
    private ArrayIterator<Integer> iA;
    private ArrayIterator<String> iB;

    @BeforeEach
    void setUp() {
        a1 = new Integer[]{1, 2, 3, 4};
        a2 = new Integer[]{5, 6, 7, 8, 9, 10, 0, -1, 5, 3};
        b = new String[]{"a", "b", "c", "d"};
        iA = new ArrayIterator<Integer>(a1);
        iB = new ArrayIterator<String>(b);
    }

    @Test
    void setArray() {
        //Call set Array for a2
        iA.setArray(a2);

        //Cycle back one space, then forward one space
        assertEquals(iA.prev(), a2[a2.length - 2]);
        assertEquals(iA.next(), a2[a2.length - 1]);

        //Test next() behavior
        for (int i = 0; i < a2.length; i++){
            assertEquals(iA.next(), a2[i]);
        }
    }

    @Test
    void next() {
        for (int i = 0; i < a1.length; i++){
            assertEquals(iA.next(), a1[i]);
        }

        for (int i = 0; i < b.length; i++){
            assertEquals(iB.next(), b[i]);
        }
    }

    @Test
    void prev() {
        for (int i = a1.length - 1; i > 0; i--){
            assertEquals(iA.prev(), a1[i - 1]);
        }
        assertEquals(iA.prev(), a1[a1.length - 1]); //Cycle back to last element

        for (int i = b.length - 1; i > 0; i--){
            assertEquals(iB.prev(), b[i - 1]);
        }

        assertEquals(iB.prev(), b[b.length - 1]); //Cycle back to last element
    }
}