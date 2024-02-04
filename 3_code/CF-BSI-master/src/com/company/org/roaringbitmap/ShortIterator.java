package com.company.org.roaringbitmap;

/**
 * Iterator over short values
 */
public interface ShortIterator  extends Cloneable {
    /**
     * @return whether there is another value
     */
    boolean hasNext();

    /**
     * @return next short value
     */
    short next();
    

    /**
     * Creates a copy of the iterator.
     * 
     * @return a clone of the current iterator
     */
    ShortIterator clone();
    
    /**
     * If possible, remove the current value
     */
    void remove();
}
