package com.company.topK;

import java.io.Serializable;

public class activeWord implements Serializable{
  long value; // the literal value of the active word
  int nbits; // number of bits in the active word
  long fill;
  int nWords;
  boolean isFill;

  activeWord() {
    nbits = 0;
  }
}
