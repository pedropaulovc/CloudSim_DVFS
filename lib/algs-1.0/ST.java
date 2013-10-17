/*************************************************************************
 *  Compilation:  javac ST.java
 *  Execution:    java ST
 *  
 *  Sorted symbol table implementation using a java.util.TreeMap.
 *  Does not allow duplicates.
 *
 *  % java ST
 *
 *************************************************************************/

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *  This class represents an ordered symbol table. It assumes that
 *  the elements are <tt>Comparable</tt>.
 *  It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 *  and <em>remove</em> methods.
 *  It also provides ordered methods for finding the <em>minimum</em>,
 *  <em>maximum</em>, <em>floor</em>, and <em>ceiling</em>.
 *  <p>
 *  The class uses the convention that values cannot be null. Setting the
 *  value associated with a key to null is equivalent to removing the key.
 *  <p>
 *  This class implements the Iterable interface for compatiblity with
 *  the version from <em>Introduction to Programming in Java: An Interdisciplinary
 *  Approach</em>.
 *  <p>
 *  This implementation uses a balanced binary search tree.
 *  The <em>put</em>, <em>contains</em>, <em>remove</em>, <em>minimum</em>,
 *  <em>maximum</em>, <em>ceiling</em>, and <em>floor</em> methods take
 *  logarithmic time.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/35applications">Section 4.5</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
public class ST<Key extends Comparable<Key>, Value> implements Iterable<Key> {

    private TreeMap<Key, Value> st;

    /**
     * Create an empty symbol table.
     */
    public ST() {
        st = new TreeMap<Key, Value>();
    }

    /**
     * Put key-value pair into the symbol table. Remove key from table if
     * value is null.
     */
    public void put(Key key, Value val) {
        if (val == null) st.remove(key);
        else             st.put(key, val);
    }

    /**
     * Return the value paired with given key; null if key is not in table.
     */
    public Value get(Key key) {
        return st.get(key);
    }

    /**
     * Delete the key (and paired value) from table.
     * Return the value paired with given key; null if key is not in table.
     */
    public Value delete(Key key) {
        return st.remove(key);
    }

    /**
     * Is the key in the table?
     */
    public boolean contains(Key key) {
        return st.containsKey(key);
    }

    /**
     * How many keys are in the table?
     */
    public int size() {
        return st.size();
    }

    /**
     * Return an <tt>Iterable</tt> for the keys in the table.
     * To iterate over all of the keys in the symbol table <tt>st</tt>, use the
     * foreach notation: <tt>for (Key key : st.keys())</tt>.
     */ 
    public Iterable<Key> keys() {
        return st.keySet();
    }

   /**
     * Return an <tt>Iterator</tt> for the keys in the table.
     * To iterate over all of the keys in the symbol table <tt>st</tt>, use the
     * foreach notation: <tt>for (Key key : st)</tt>.
     * This method is for backward compatibility with the version from <em>Introduction
     * to Programming in Java: An Interdisciplinary Approach.</em>
     */
    public Iterator<Key> iterator() {
        return st.keySet().iterator();
    }

    /**
     * Return the smallest key in the table.
     */ 
    public Key min() {
        return st.firstKey();
    }

    /**
     * Return the largest key in the table.
     */ 
    public Key max() {
        return st.lastKey();
    }


    /**
     * Return the smallest key in the table >= k.
     */ 
    public Key ceil(Key k) {
        SortedMap<Key, Value> tail = st.tailMap(k);
        if (tail.isEmpty()) return null;
        else return tail.firstKey();
    }

    /**
     * Return the largest key in the table <= k.
     */ 
    public Key floor(Key k) {
        if (st.containsKey(k)) return k;

        // does not include key if present (!)
        SortedMap<Key, Value> head = st.headMap(k);
        if (head.isEmpty()) return null;
        else return head.lastKey();
    }

   /***********************************************************************
    * Test routine.
    **********************************************************************/
    public static void main(String[] args) {
        ST<String, Integer> st = new ST<String, Integer>();
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String key = StdIn.readString();
            st.put(key, i);
        }
        for (String s : st.keys())
            StdOut.println(s + " " + st.get(s));
    }
}
