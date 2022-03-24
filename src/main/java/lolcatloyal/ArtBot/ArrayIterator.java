package lolcatloyal.ArtBot;

/**
 * Wrapper class to allow easy iteration over an array.
 *
 * @param <E> The type of Object stored within the array
 */
public class ArrayIterator<E> {
    private E[] a;
    private int index;

    /**
     * Creates a new ArrayIterator for the given array.
     *
     * If the given array is null or has size 0, initializes
     * iterator with a size 1 empty array of E.
     *
     * @param array Array to iterate over.
     */
    public ArrayIterator(E[] array){
        setArray(array);
    }

    /**
     * Sets the underlying array to iterate over.
     *
     * If the given array is null or has size 0, sets array to
     * a size 1 empty array of E.
     * @param array New array to iterate over.
     */
    public void setArray(E[] array){
        if (array == null || array.length == 0){
            a = (E[]) new Object[1];
        }
        else {
            a = array;
        }
        resetIndex();
    }

    /**
     * Cycles through the aray, getting
     * the next element in the array.
     *
     * @return Next element in the array.
     */
    public E next(){
        incrementIndex();
        return a[index];
    }

    /**
     * Cycles through the array, getting
     * the previous element in the array.
     *
     * @return Previous element in the array.
     */
    public E prev(){
        decrementIndex();
        return a[index];
    }

    /**
     * Resets the index to point to the last element
     * in the array.
     */
    private void resetIndex(){
        index = a.length - 1;
    }

    /**
     * Increments the index to point to the next
     * element in the array.
     *
     * If the index reaches the end of the array, points
     * to the first element in the array.
     */
    private void incrementIndex(){
        index = (index + 1) % a.length;
    }

    /**
     * Decrements the index to point to the previous
     * element in the array.
     *
     * If the index reaches -1, resets to point to the last
     * element in the array.
     */
    private void decrementIndex(){
        index--;

        //Below 0 --> Reset to last element
        if (index < 0){
            resetIndex();
        }
    }


}
