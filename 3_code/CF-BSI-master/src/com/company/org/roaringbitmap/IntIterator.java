package com.company.org.roaringbitmap;

/**
 * A simple iterator over integer values
 */
public interface IntIterator extends Cloneable {
    /**
     * @return whether there is another value
     */
    boolean hasNext();

    /**
     * @return next integer value
     */
    int next();
    
    /**
     * Creates a copy of the iterator.
     * 
     * @return a clone of the current iterator
     */
    IntIterator clone();

}
