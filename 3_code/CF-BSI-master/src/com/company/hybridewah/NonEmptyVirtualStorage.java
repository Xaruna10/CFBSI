package com.company.hybridewah;

/*
 * Copyright 2009-2013, Daniel Lemire, Cliff Moon, David McIntosh, Robert Becho, Google Inc. and Veronika Zenz
 * Modified by Gheorghi Guzun to support operation of compressed and verbatim bitmaps.
 * Licensed under APL 2.0.
 */
/**
 * This is a BitmapStorage that can be used to determine quickly if the result
 * of an operation is non-trivial... that is, whether there will be at least on
 * set bit.
 * 
 * @since 0.4.2
 * @author Daniel Lemire and Veronika Zenz
 * 
 */
public class NonEmptyVirtualStorage implements BitmapStorage {
  static class NonEmptyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Do not fill in the stack trace for this exception
     * for performance reasons.
     *
     * @return this instance
     * @see Throwable#fillInStackTrace()
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
  }
  
  private static final NonEmptyException nonEmptyException = new NonEmptyException();

  /**
   * If the word to be added is non-zero, a NonEmptyException exception is
   * thrown.
   * 
   * @see BitmapStorage#add(long)
   */
  public void add(long newdata) {
    if (newdata != 0)
      throw nonEmptyException;
    return;
  }

  /**
   * throws a NonEmptyException exception when number > 0
   * 
   * @see BitmapStorage#addStreamOfLiteralWords(long[], long, long)
   */
  public void addStreamOfLiteralWords(long[] data, int start, int number) {
      if(number>0){
          throw nonEmptyException;
      }
  }

  /**
   * If the boolean value is true and number>0, then it throws a NonEmptyException exception,
   * otherwise, nothing happens.
   * 
   * @see BitmapStorage#addStreamOfEmptyWords(boolean, long)
   */
  public void addStreamOfEmptyWords(boolean v, long number) {
    if (v && (number>0))
      throw nonEmptyException;
    return;
  }

  /**
   * throws a NonEmptyException exception when number > 0
   * 
   * @see BitmapStorage#addStreamOfNegatedLiteralWords(long[], long,
   *      long)
   */
  public void addStreamOfNegatedLiteralWords(long[] data, int start, int number) {
      if(number>0){
          throw nonEmptyException;
      }
  }

  /**
   * Does nothing.
   * 
   * @see BitmapStorage#setSizeInBits(int)
   */
  public void setSizeInBits(int bits) {
  }

@Override
public void setColName(String colname) {
	// TODO Auto-generated method stub
	
}

	@Override
	public void setVerbatim(boolean verbatimFlag) {
		// TODO Auto-generated method stub

	}

}
