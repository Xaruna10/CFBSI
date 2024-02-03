package com.company.topK;

public class bitArray {
  public String name = "";
  // public Vector vec; // list of regular code words
  public long[] vec;
  // public activeWord active; // the active word
  public long active;
  public int pos = 0;
  public int maxPos = 0;
  public int count = 0;
  static int[] countOnes = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1,
      2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2,
      3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1,
      2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3,
      4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3,
      4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3, 4, 2,
      3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2,
      3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4,
      5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3,
      4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4,
      5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8 };

  public bitArray() {
    vec = new long[1]; // active = new activeWord();
  }

  public bitArray(int size) {
    vec = new long[size]; // active = new activeWord();
  }

  public bitArray(String name) {
    this.name = name;
    // vec = new Vector(); //active = new activeWord();
    vec = new long[1];
  }

  public bitArray(String name, int size) {
    this.name = name;
    // vec = new Vector(); //active = new activeWord();
    vec = new long[size];
  }

  public void appendWord() {
    // vec.addElement (new Long(active));
    vec[maxPos] = active;
    maxPos++;
  }

  public void appendWord(long word) {
    // vec.addElement (new Long(word));
    vec[maxPos] = word;
    maxPos++;
  }

  public int getNumberOfWords() {
    return maxPos;
  }

  public String toString() {
    Long l;
    StringBuffer bitmap = new StringBuffer();
    // for (Enumeration e = vec.elements(); e.hasMoreElements(); ) {
    for (int i = 0; i < maxPos; i++) {
      l = vec[i];
      String str =
          ("0000000000000000000000000000000" + Long.toBinaryString(l
              .longValue()));
      bitmap.append(str.substring(str.length() - 32) + " ");
      // bitmap.append(l.longValue()+",");
    }
    return bitmap.toString();
  }

  public int getCount(int x) {
    int temp;
    temp = 0x55555555;
    x = (x & temp) + (x >>> 1 & temp);
    temp = 0x33333333;
    x = (x & temp) + (x >>> 2 & temp);
    temp = 0x07070707;
    x = (x & temp) + (x >>> 4 & temp);
    temp = 0x000F000F;
    x = (x & temp) + (x >>> 8 & temp);
    return (x & 0x1F) + (x >>> 16);
  }

  public void setCount() {
    count = count();
  }

  public int count() {
    return getMatches();
    /*
     * int matches = 0; pos = 0; while (pos < vec.size()) { decode(); if
     * (active.isFill) { if (active.fill!=0) //The compressed words are all 1s {
     * matches+= 31*((int)active.nWords); } } else {//The word is a literal int
     * n=(int)active.value; matches+=getCount(n); } pos++; } return matches;
     */
  }

  public int getNextSetBit(int start) {
    int bit = -1;
    int W = 32, offset;
    pos = start / W;
    offset = start % W;
    int i, j;
    for (i = pos; i < maxPos; i++) {
      active = vec[i];
      if (active == 0) {
        continue;
      } else {
        for (j = offset; j < W; j++) {
          if ((vec[i] & BitmapIndex.power2[31 - j]) != 0) {
            bit = j;
            break;
          }
        }
        break;
      }
    }
    if (bit > 0) {
      bit += i * W;
    }
    return bit;
  }

  public bitArray first(int k) {
    bitArray F = new bitArray(vec.length);
    F.maxPos = this.maxPos;
    int needed = k;
    pos = 0;
    while (needed > 0 && pos < maxPos) {
      if (vec[pos] != 0) {
        int ones = getOnes(vec[pos]);
        if (needed > ones) {// Safely add all the ones
          F.vec[pos] = vec[pos];
          needed -= ones;
        } else { // We only need some of the ones in this word
          F.vec[pos] = getKOnes(vec[pos], needed);
          needed = 0;
        }
      }
      pos++;
    }
    return F;

  }

  public int getBit(int position) {
    int retValue = 0;
    pos = position / 32;
    int offset = position % 32;
    // System.out.println("vec["+pos+"]= "+vec[pos]);
    if ((vec[pos] & BitmapIndex.power2[31 - offset]) != 0) {
      retValue = 1;
    }
    return retValue;
  }

  public void decode() { // Decode the word of it on position pos
    // if (pos < vec.size()) {
    // active = ((Long) vec.get(pos)).longValue();
    active = vec[pos];
    /*
     * } else { //If the program reaches here it means that the bitmaps do not
     * have the same number of bits... active = -1; System.out.println
     * ("WANT TO ACCESS A POSITION BEYOND THE SIZE OF THE BITMAP ("+name+").");
     * }
     */
  }

  public void decode(int position) { // Decode the word of it on position pos
    // active = ((Long) vec.get(position)).longValue();
    active = vec[position];
  }

    public int getOnes(long n) {
    int matches = 0;
    byte b;
    b = (byte) (n);
    matches += countOnes[0xFF & (int) b];
    n >>>= 8;
    b = (byte) (n);
    matches += countOnes[0xFF & (int) b];
    n >>>= 8;
    b = (byte) (n);
    matches += countOnes[0xFF & (int) b];
    n >>>= 8;
    b = (byte) (n);
    matches += countOnes[0xFF & (int) b];
    return matches;
  }

  public long getKOnes(long n, int k) {
    int word = 0;
    int needed = k;
    int cur = 0;
    byte b;
    int b2;
    int i = 0;
    for (i = 0; i < 4; i++) {
      b = (byte) (n);
      b2 = 0xFF & (int) b;
      cur = countOnes[b2];
      if (needed >= cur) {
        b2 <<= i * 8;
        word |= b2;
        if (needed == cur) {
          break;
        }
        needed -= cur;
      } else {
        int c = 0;
        int j = 0;
        for (j = 0; j < 8; j++) {
          b2 = 0x01 & (int) b;
          if (b2 == 1) {
            c |= (b2 << j);
            needed--;
            if (needed == 0) {
              break;
            }
          }
          b >>>= 1;
        }
        word |= (c <<= i * 8);
        break;
      }
      n >>>= 8;
    }
    // while (i<4) {
    // word = word << 8;
    // i++;
    // }
    return word;
  }

  public int getMatches() {
    // Returns the number of 1s in the uncompressed format of the bitVector,
    // i.e. the number of records that
    // have the bit set
    int matches = 0;
    pos = 0;
    while (pos < maxPos) {
      decode();
      long n = active;
      byte b;
      b = (byte) (n);
      matches += countOnes[0xFF & (int) b];
      n >>>= 8;
      b = (byte) (n);
      matches += countOnes[0xFF & (int) b];
      n >>>= 8;
      b = (byte) (n);
      matches += countOnes[0xFF & (int) b];
      n >>>= 8;
      b = (byte) (n);
      // matches+=countOnes[0x7F & (int)b]; //For the compress version
      matches += countOnes[0xFF & (int) b];
      pos++;
    }
    return matches;
  }

  public String getIDs() {
    // Returns the number of 1s in the uncompressed format of the bitVector,
    // i.e. the number of records that
    // have the bit set
    int pos = 0, id = 1;
    String output = "";
    while (pos < maxPos) {
      // System.out.println("pos: "+pos);
      // result.pos = pos;
      decode(pos);
      String word =
          "00000000000000000000000000000000" + Long.toBinaryString(active);
      // System.out.println("Literal word "+word);
      int l = word.length();
      String s = word.substring(l - 32, l);
      int n = 0;
      int m = s.indexOf("1", n);
      while (m >= 0) {
        output = output + "\t " + (id + m);
        n = m;
        m = s.indexOf("1", n + 1);
      }
      id = id + 32;
      pos++;
    }
    return output;
  }

  public bitArray AND(bitArray y) {
    bitArray z = new bitArray(vec.length);
    int pos = 0;
    while (pos < vec.length) {
      decode(pos);
      y.decode(pos);
      z.active = active & y.active;
      z.appendWord();
      pos++;
    }
    return z;
  }

  public bitArray NOT() {
    bitArray z = new bitArray(vec.length);
    int pos = 0;
    while (pos < vec.length) {
      decode(pos);
      z.active = 0xFFFFFFFFL & ~active;
      z.appendWord();
      pos++;
    }
    return z;
  }
}
