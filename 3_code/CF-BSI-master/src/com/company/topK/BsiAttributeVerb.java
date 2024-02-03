package com.company.topK;
import java.io.Serializable;

/**
 * 
 * @author Gheorghi Guzun
 */

public class BsiAttributeVerb implements Serializable {
  
  /**
   * 
   */
  
  static int wordsNeeded=0;
  int size;
  int offset=0;
 bitArray64[] bsi;
 
 public BsiAttributeVerb() {
    size = 0;
    bsi = new bitArray64[32];

  }
 
  /**
   * 
   * Adds new slice to the BSI array. Increases the array size if necessary.
   * (gguzun)
   * 
   * @param slice
   * 
   */
  public void add(bitArray64 slice) {
    /*
     * if(this.size==bsi.length){ bitArray64[] temp = bsi; bsi = new
     * bitArray64[size * 3 / 2 + 1]; for (int i = 0; i < temp.length; i++) {
     * bsi[i] = temp[i]; } bsi[size] = slice; size++;
     * 
     * }
     */
    this.bsi[size] = slice;
    this.size++;
 }

  /**
   * Set the size of the bsi (how many slices are non zeros)
   * 
   * @param s
   */
  public void setSize(int s) {
    this.size = s;
  }
  
  
  public int getSizeInBytes(){
	  int totSize=0;
	  for(int i=0; i<this.size; i++){
		  totSize+=this.bsi[i].getNumberOfWords()*8;
	  }
	  
	  return totSize;
  }
  
  
  
  public void buildBSI(long[] array){
	  
	  
	  long max = 0;	
		for(int i=0; i< array.length; i++){
			if(max<array[i])
				max=array[i];
		}					
			
			int slices = Long.toBinaryString(max).length(); // local maximum
			int words = (int) Math.ceil((double) array.length / 64);
	  this.bsi = new bitArray64[slices];	 
	  for (int c = 0; c < slices; c++) {
		  this.bsi[c] = new bitArray64(words);
		  this.bsi[c].maxPos = words;
	      }
	  
	  long thisBin=0;
	  for (int seq = 0; seq < array.length; seq++) {
	      int w = seq / 64;
	      int offset = seq % 64;	     
	        thisBin = array[seq];	        
	        int slice = 0;
	        while (thisBin > 0) {
	          if ((thisBin & 1) == 1) {
	        	  this.bsi[slice].vec[w] |= (1L << offset);
	          }
	          thisBin >>= 1;
	          slice++;
	        }
	    }
	  this.size = slices;
  }
  
  /**
   * Adds otherBSI to this BsiAttribute. Saves the result in this BsiAttribute
   * 
   * @param otherBSI
   * @param offset
   */

  public void BSI_SUM(BsiAttributeVerb otherBSI, int offset) {
    if (this.bsi[0] == null) {

      for (int i = 0; i < offset; i++) {
        this.bsi[i] = topPref.bitmapindex.getBitmap("B0");
      }
      for (int i = offset; i < otherBSI.size + offset; i++) {
        this.bsi[i] = otherBSI.bsi[i - offset];
      }
      for (int i = otherBSI.size + offset; i < 32; i++) {
        this.bsi[i] = topPref.bitmapindex.getBitmap("B0");
      }
      this.setSize(otherBSI.size + offset);

    } else {
      // int r = Math.min(this.size, otherBSI.size);
      bitArray64 A = this.bsi[offset];
      bitArray64 B = otherBSI.bsi[0];

      bitArray64 sum[] = XOR_AND(A, B);
      // bitArray64 S = XOR(A, B);
      // bitArray64 C = AND(A, B);
      bitArray64 S = sum[0];
      bitArray64 C = sum[1];
      this.bsi[offset] = S;
      int i;
      // if(this.size>(otherBSI.size+offset)){

      for (i = 1; i < otherBSI.size; i++) {
        A = this.bsi[i + offset];
        B = otherBSI.bsi[i];
        // S = XOR(XOR(A, B), C);
        S = XOR(A, B, C);
        // C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
        C = maj(A, B, C);
        this.bsi[i + offset] = S;
      }

      this.size = Math.max(i + offset, this.size);
      while (C.getMatches() > 0) {
        A = this.bsi[i + offset];
        sum = XOR_AND(A, C);
        // S = XOR(A, C);
        S = sum[0];
        this.bsi[i + offset] = S;
        // C = AND(A, C);
        C = sum[1];
        i++;
        this.size = Math.max(i + offset, this.size);
      }

    }
  }

  public bitArray64 XOR_AND(bitArray64 x, bitArray64 y, bitArray64 c) {    
    bitArray64 s = new bitArray64(wordsNeeded);
    c.maxPos = x.maxPos;
    s.maxPos = x.maxPos;
    int pos = 0;
    long xactive, yactive;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // xactive = x.active;
      xactive = x.vec[pos];
      // y.decode(pos);
      // yactive = y.active;
      yactive = y.vec[pos];
      // s.active = xactive ^ yactive;
      // s.appendWord();
      s.vec[pos] = xactive ^ yactive;
      // c.active = xactive & yactive;
      // c.appendWord();
      c.vec[pos] = xactive & yactive;
      pos++;
    }    
    return s;
  }
  
  public bitArray64[] XOR_AND(bitArray64 x, bitArray64 y) {
    bitArray64 z[] = new bitArray64[2];
    z[0] = new bitArray64(x.vec.length);
    z[1] = new bitArray64(x.vec.length);
    z[0].maxPos = x.maxPos;
    z[1].maxPos = x.maxPos;
    int pos = 0;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // y.decode(pos);
      // z[0].active = x.active ^ y.active;
      z[0].vec[pos] = x.vec[pos] ^ y.vec[pos];
      // z[0].appendWord();
      // z[1].active = x.active & y.active;
      z[1].vec[pos] = x.vec[pos] & y.vec[pos];
      // z[1].appendWord();
      pos++;
    }
    return z;
  }

  /**
   * XOR with three inputs
   * 
   * @param w - bitArray
   * @param x
   * @param y
   * @return
   */
  public bitArray64 XOR(bitArray64 w, bitArray64 x, bitArray64 y) {
    // Input: Two bitArray x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray64 z = new bitArray64(x.vec.length);
    z.maxPos = x.maxPos;
    int pos = 0;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // y.decode(pos);
      // w.decode(pos);
      // z.active = x.active ^ y.active ^ w.active;
      // z.appendWord();
      z.vec[pos] = x.vec[pos] ^ y.vec[pos] ^ w.vec[pos];
      pos++;
    }
    return z;
  }

  public void BSI_SUM2(BsiAttributeVerb otherBSI, int offset) {
    if (this.bsi[0] == null) {

      for (int i = 0; i < offset; i++) {
        this.bsi[i] = topPref.bitmapindex.getBitmap("B0");
      }
      for (int i = offset; i < otherBSI.size + offset; i++) {
        this.bsi[i] = otherBSI.bsi[i - offset];
      }
      for (int i = otherBSI.size + offset; i < 32; i++) {
        this.bsi[i] = topPref.bitmapindex.getBitmap("B0");
      }
      this.setSize(otherBSI.size + offset);

    } else {
      int r = Math.min(this.size, otherBSI.size);
      bitArray64 A = this.bsi[offset];
      bitArray64 B = otherBSI.bsi[0];
      bitArray64 S = XOR(A, B);
      this.bsi[offset] = S;
      bitArray64 C = AND(A, B);
      int i;
      // if(this.size>(otherBSI.size+offset)){

      for (i = 1; i < r; i++) {
        A = this.bsi[i + offset];
        B = otherBSI.bsi[i];
        S = XOR(XOR(A, B), C);
        this.bsi[i + offset] = S;
        // C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
        C = maj(A, B, C);
      }

      if (this.size > otherBSI.size) {
        for (i = otherBSI.size; i < this.size; i++) {
          A = this.bsi[i + offset];
          S = XOR(A, C);
          this.bsi[i + offset] = S;
          C = AND(A, C);
        }
      } else {
        for (i = this.size; i < otherBSI.size; i++) {
          B = otherBSI.bsi[i];
          S = XOR(B, C);
          this.bsi[i + offset] = S;
          C = AND(B, C);
        }
      }
      this.size = Math.max(otherBSI.size + offset, this.size);
      if (C.count() > 0) {
        this.bsi[otherBSI.size + offset] = C;
        this.size = Math.max(otherBSI.size + offset + 1, this.size);
      }

    }
  }

  public bitArray64 maj(bitArray64 w, bitArray64 x, bitArray64 y) {
    // AB + BC + AC
    bitArray64 z = new bitArray64(x.vec.length);
    int pos = 0;
    z.maxPos = x.maxPos;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // y.decode(pos);
      // w.decode(pos);
      // z.active = (x.active & y.active) | (y.active & w.active) | (x.active &
      // w.active);
      // z.appendWord();

      z.vec[pos] =
          (x.vec[pos] & y.vec[pos]) | (y.vec[pos] & w.vec[pos])
              | (x.vec[pos] & w.vec[pos]);
      pos++;
    }
    return z;
  }
  /**
   * Computes the top-K tuples in a bsi-attribute.
   * 
   * @param k - the number in top-k
   * @return a bitArray containing the top-k tuples
   */
  public bitArray64 topKMax(int k) {
    bitArray64 topK, SE, X;
    bitArray64 G = topPref.bitmapindex.getBitmap("B0");
    bitArray64 E = topPref.bitmapindex.getBitmap("B1");
    int n = 0;

    for (int i = this.size - 1; i >= 0; i--) {
      SE = AND(E, this.bsi[i]);
      X = OR(G, SE);
      n = X.count();
      if (n > k) {
        E = SE;
      }
      if (n < k) {
        G = X;
        E = ANDNOT(E, this.bsi[i]);
      }
      if (n == k) {
        E = SE;
        break;
      }
    }
    n = G.getMatches();
    topK = OR(G, E.first(k - n+ 1));

    return topK;
  }

  /**
   * Performs XOR operation between x and y. Returns a bitarry containing the
   * result
   * 
   * @param x
   * @param y
   * @return
   */
  public bitArray64 XOR(bitArray64 x, bitArray64 y) {
    // Input: Two bitArray64 x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray64 z = new bitArray64(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      //x.decode(pos);
      //y.decode(pos);
      z.active = x.vec[pos] ^ y.vec[pos];
      z.appendWord();
      pos++;
    }
    return z;
  }

  public bitArray64 NOT(bitArray64 x) {
    int nWords;
    bitArray64 z = new bitArray64(x.vec.length);
    x.pos = 0;
    while (x.pos < x.vec.length) {
      x.decode();
      z.active = 0xFFFFFFFFFFFFFFFFL & ~x.active;
      z.appendWord();
      x.pos++;
    }
    return z;
  }

  /*
   * public BsiAttribute SUM_BSI_inplace_withBug (BsiAttribute a) { BsiAttribute
   * res = new BsiAttribute(); int i=0, s=a.size, p=this.size; int aStart = 0,
   * thisStart = 0; if (a.offset==this.offset) { //do the sum as normal
   * res.offset = a.offset; } else if (a.offset > this.offset) { //start the sum
   * at a[0] + res[a.offset], res_< a.offset remain unchanged int unchanged =
   * a.offset - this.offset; //justAdd slices can be just added from the result
   * with the minimum offset for (i=0; i<unchanged && i<this.size; i++) { //str
   * = Integer.toString(i); res.bsi[i]=this.bsi[i]; } thisStart = i; p=p-i;
   * res.offset = this.offset; res.size = i; } else {//a.offset < this.offset
   * //copy the slices into int appendFromA = this.offset - a.offset; //justAdd
   * slices can be just added from the result with the minimum offset for (i=0;
   * i<appendFromA && i<a.size; i++) { //str = Integer.toString(i); res.bsi[i]=
   * a.bsi[i]; } aStart = i; s=s-i; res.offset = a.offset; res.size = i; }
   * 
   * 
   * int q = Math.max(s, p); int r = Math.min(s, p);
   * 
   * bitArray64 A, B; bitArray64 C=new bitArray64(wordsNeeded),S=new
   * bitArray64(wordsNeeded); A = a.bsi[aStart]; B = this.bsi[thisStart]; if
   * (A==null || B==null) { System.out.println("A or B is null"); }
   * XOR_AND(A,B,S,C); res.bsi[res.size]= S; for (i = 1; i < r; i++) { A =
   * a.bsi[aStart+i]; B = this.bsi[thisStart+i]; S = XOR(A, B, C);
   * res.bsi[res.size+i] = S; C = maj (A,B,C); } if (s > p) { for (i = p; i < s;
   * i++) { A = a.bsi[aStart+i]; S = new bitArray64(wordsNeeded); XOR_AND(A, C,
   * S, C); res.bsi[res.size + i] = S;
   * 
   * } } else { for (i = s; i < p; i++) { B = this.bsi[thisStart + i]; S = new
   * bitArray64(wordsNeeded); XOR_AND(B,C,S,C); res.bsi[res.size + i] = S; } }
   * if (C.count() > 0) { res.bsi[res.size+q] = C; // Carry bit q++; } res.size=
   * res.size+q; return res; }
   */
    public BsiAttributeVerb SUM_BSI (BsiAttributeVerb a) {
        BsiAttributeVerb res = new BsiAttributeVerb();                  
        int i=0, s=a.size, p=this.size;
        int aStart = 0, thisStart = 0;
        if (a.offset==this.offset) {            
            //do the sum as normal
            res.offset = a.offset;
        } else if (a.offset > this.offset) {
            //start the sum at a[0] + res[a.offset], res_< a.offset remain unchanged
            int unchanged = a.offset - this.offset; //justAdd slices can be just added from the result with the minimum offset            
            for (i=0; i<unchanged && i<this.size; i++) {
                //str = Integer.toString(i);
                res.bsi[i]=this.bsi[i];                
            }
            thisStart = i;            
            p=p-i;
            res.offset = this.offset;
            res.size = i;
        } else {//a.offset < this.offset
            //copy the slices into 
            int appendFromA = this.offset - a.offset; //justAdd slices can be just added from the result with the minimum offset
            for (i=0; i<appendFromA && i<a.size; i++) {
                //str = Integer.toString(i);
                res.bsi[i]= a.bsi[i];                
            }
            aStart = i;            
            s=s-i;
            res.offset = a.offset;            
            res.size = i;
        }
        
         
        int q = Math.max(s, p);
        int r = Math.min(s, p);

        bitArray64 A, B;
        bitArray64[] sum = new bitArray64[2];        
        A = a.bsi[aStart];
        B = this.bsi[thisStart];
        if (A==null || B==null) {
            System.out.println("A or B is null");
        }                
        sum = XOR_AND(A,B);
        res.bsi[res.size]= sum[0];                
        for (i = 1; i < r; i++) {
            A = a.bsi[aStart+i];
            B = this.bsi[thisStart+i];
            sum[0] = XOR(A, B, sum[1]);
            res.bsi[res.size+i] = sum[0];
            sum[1] = maj (A,B,sum[1]);
        }
        if (s > p) {
            for (i = p; i < s; i++) {
                A = a.bsi[aStart+i];
                sum = XOR_AND(A, sum[1]);                
                res.bsi[res.size + i] = sum[0];                
            }
        } else {
            for (i = s; i < p; i++) {
                B = this.bsi[thisStart + i];
                sum = XOR_AND(B,sum[1]);                
                res.bsi[res.size + i] = sum[0];                
            }
        }
        if (sum[1].count() > 0) {
            res.bsi[res.size+q] = sum[1]; // Carry bit
            q++;
        }
        res.size= res.size+q;
        return res;
    }

  public BsiAttributeVerb multiply_inplace_withBug (int number) { //k is the offset
        //System.out.println("Multiply by "+number);        
        BsiAttributeVerb res = null;
        bitArray64 C=new bitArray64(wordsNeeded),S;
        int k=0;
        while (number > 0) {
            if ((number & 1) == 1) {                
                if (res==null) {                    
                    res = new BsiAttributeVerb();
                    res.offset = k;
                    for (int i=0; i<this.size; i++) {
                        res.add(this.bsi[i]);
                    }
                    k=0;
                } else {
                    /*Move the slices of res k positions*/                                       
                    bitArray64 A, B;
                    A = res.bsi[k];
                    B = this.bsi[0];
//                    if (A==null || B==null) {
//                        System.out.println("A or B is null");
//                    }                    
                    S = XOR_AND(A,B,C);                  
                    res.bsi[k]=S;        
                    //C = Sum[1];
                    for (int i = 1; i < this.size; i++) {//Add the slices of this to the current res
                        A = res.bsi[i+k]; B = this.bsi[i];                        
                        if (A==null) {
                            S = XOR_AND (B,C,C);
                            res.size++;
                        } else {                            
                            S = XOR(A, B, C);                            
                            C = maj(A, B, C); //OR(OR(AND(A, B), AND(A, C)), AND(C, B));
                        }
                        res.bsi[i+k]=S;                        
                    }                    
                    for (int i = this.size+k; i < res.size; i++) {//Add the remaining slices of res with the Carry C
                        A = res.bsi[i];
                        S= XOR_AND(A, C, C);
                        res.bsi[i]=S;                        
                    }
                    if (C.count() > 0) {
                        res.bsi[res.size]=C; // Carry bit
                        res.size++;
                    }                            
                    /**/                    
                }
//                System.out.println("number="+number+" k="+k+" res="+res.SUM());
            }            
            number >>= 1;
            k++;
        }
//        System.out.println(this.SUM()+"x"+orig+"="+res.SUM());
        return res;
    }
     
  public BsiAttributeVerb multiply (int number) { //k is the offset
        //System.out.println("Multiply by "+number);        
        BsiAttributeVerb res = null;
        bitArray64[] sum = new bitArray64[2];     
        int k=0;
        while (number > 0) {
            if ((number & 1) == 1) {                
                if (res==null) {                    
                    res = new BsiAttributeVerb();
                    res.offset = k;
                    for (int i=0; i<this.size; i++) {
                        res.bsi[i]=this.bsi[i];
                    }
                    res.size = this.size;
                    k=0;
                } else {
                    /*Move the slices of res k positions*/                                       
                    bitArray64 A, B;
                    A = res.bsi[k];
                    B = this.bsi[0];
//                    if (A==null || B==null) {
//                        System.out.println("A or B is null");
//                    }                    
                    sum = XOR_AND(A,B);
                    
                    res.bsi[k]=sum[0];                            
                    for (int i = 1; i < this.size; i++) {//Add the slices of this to the current res
                        A = res.bsi[i+k]; B = this.bsi[i];                        
                        if (A==null) {
                            sum = XOR_AND (B,sum[1]);                            
                            res.size++;
                        } else {                            
                            sum[0] = XOR(A, B, sum[1]);                                                        
                            sum[1] = maj(A, B, sum[1]); //OR(OR(AND(A, B), AND(A, C)), AND(C, B));
                        }
                        res.bsi[i+k]=sum[0];                        
                    }                    
                    for (int i = this.size+k; i < res.size; i++) {//Add the remaining slices of res with the Carry C
                        A = res.bsi[i];
                        sum = XOR_AND(A, sum[1]);
                        res.bsi[i]=sum[0];                                                
                    }
                    if (sum[1].count() > 0) {
                        res.bsi[res.size]=sum[1]; // Carry bit
                        res.size++;
                    }                            
                    /**/
                    //k=0;
                }
//                System.out.println("number="+number+" k="+k+" res="+res.SUM());
            }            
            number >>= 1;
            k++;
        }
//        System.out.println(this.SUM()+"x"+orig+"="+res.SUM());
        return res;
    }
 
  /**
   * Performs AND operation between x and y. Returns a bitarry containing the
   * result
   * 
   * @param x
   * @param y
   * @return
   */
  public bitArray64 AND(bitArray64 x, bitArray64 y) {
    bitArray64 z = new bitArray64(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // y.decode(pos);
      z.active = x.vec[pos] & y.vec[pos];
      z.appendWord();
      pos++;
    }
    return z;
  }

  /**
   * Performs ANDNOT operation between x and y. Returns a bitarry containing the
   * result
   * 
   * @param x
   * @param y
   * @return
   */
  public bitArray64 ANDNOT(bitArray64 x, bitArray64 y) {
    bitArray64 z = new bitArray64(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // y.decode(pos);
      z.active = x.vec[pos] & (~y.vec[pos]);
      z.appendWord();
      pos++;
    }
    return z;
  }

  /**
   * Performs OR operation between x and y. Returns a bitarry containing the
   * result
   * 
   * @param x
   * @param y
   * @return
   */
  public bitArray64 OR(bitArray64 x, bitArray64 y) {
    // Input: Two bitArray64 x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray64 z = new bitArray64(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      // x.decode(pos);
      // y.decode(pos);
      z.active = x.vec[pos] | y.vec[pos];
      z.appendWord();
      pos++;
    }
    return z;
  }

      
  public long SUM( ) {
        long sum = 0;
        bitArray64 B_i; 
        for (int i = 0; i < size; i++) {
            B_i = bsi[i];
            sum += BitmapIndex.power2[offset+i] * B_i.count();
        }
        return sum;
  }
  
  public BsiAttributeVerb SUM_BSI_inPlace(BsiAttributeVerb a) {
    BsiAttributeVerb res = new BsiAttributeVerb();
    int i = 0, s = a.size, p = this.size;
    int aStart = 0, thisStart = 0;
    if (a.offset == this.offset) {
      // do the sum as normal
      res.offset = a.offset;
    } else if (a.offset > this.offset) {
      // start the sum at a[0] + res[a.offset], res_< a.offset remain unchanged
      int unchanged = a.offset - this.offset; // justAdd slices can be just
                                              // added from the result with the
                                              // minimum offset
      for (i = 0; i < unchanged && i < this.size; i++) {
        // str = Integer.toString(i);
        res.bsi[i] = this.bsi[i];
      }
      thisStart = i;
      p = p - i;
      res.offset = this.offset;
      res.size = i;
    } else {// a.offset < this.offset
      // copy the slices into
      int appendFromA = this.offset - a.offset; // justAdd slices can be just
                                                // added from the result with
                                                // the minimum offset
      for (i = 0; i < appendFromA && i < a.size; i++) {
        // str = Integer.toString(i);
        res.bsi[i] = a.bsi[i];
      }
      aStart = i;
      s = s - i;
      res.offset = a.offset;
      res.size = i;
    }

    int q = Math.max(s, p);
    int r = Math.min(s, p);

    bitArray64 A, B;
    bitArray64 C = new bitArray64(wordsNeeded), S;
    A = a.bsi[aStart];
    B = this.bsi[thisStart];
    if (A == null || B == null) {
      System.out.println("A or B is null");
    }
    S = XOR_AND(A, B, C);
    res.bsi[res.size] = S;
    for (i = 1; i < r; i++) {
      A = a.bsi[aStart + i];
      B = this.bsi[thisStart + i];
      S = XOR(A, B, C);
      res.bsi[res.size + i] = S;
      C = maj(A, B, C);
    }
    if (s > p) {
      for (i = p; i < s; i++) {
        A = a.bsi[aStart + i];
        S = XOR_AND(A, C, C);
        res.bsi[res.size + i] = S;

      }
    } else {
      for (i = s; i < p; i++) {
        B = this.bsi[thisStart + i];
        S = XOR_AND(B, C, C);
        res.bsi[res.size + i] = S;
      }
    }
    if (C.count() > 0) {
      res.bsi[res.size + q] = C; // Carry bit
      q++;
    }
    res.size = res.size + q;
    return res;
  }

  public BsiAttributeVerb multiply_inPlace(int number) { // k is the offset
    // System.out.println("Multiply by "+number);
    BsiAttributeVerb res = null;
    bitArray64 C, S;
    int k = 0;
    while (number > 0) {
      if ((number & 1) == 1) {
        if (res == null) {
          res = new BsiAttributeVerb();
          res.offset = k;
          for (int i = 0; i < this.size; i++) {
            res.bsi[i] = this.bsi[i];
          }
          res.size = this.size;
          k = 0;
        } else {
          /* Move the slices of res k positions */
          bitArray64 A, B;
          A = res.bsi[k];
          B = this.bsi[0];
          // if (A==null || B==null) {
          // System.out.println("A or B is null");
          // }
          C = new bitArray64(wordsNeeded);
          S = XOR_AND(A, B, C);
          res.bsi[k] = S;
          // C = Sum[1];
          for (int i = 1; i < this.size; i++) {// Add the slices of this to the
                                               // current res
            A = res.bsi[i + k];
            B = this.bsi[i];
            if (A == null) {
              S = XOR_AND(B, C, C);
              res.size++;
            } else {
              S = XOR(A, B, C);
              C = maj(A, B, C); // OR(OR(AND(A, B), AND(A, C)), AND(C, B));
            }
            res.bsi[i + k] = S;
          }
          for (int i = this.size + k; i < res.size; i++) {// Add the remaining
                                                          // slices of res with
                                                          // the Carry C
            A = res.bsi[i];
            S = XOR_AND(A, C, C);
            res.bsi[i] = S;
          }
          if (C.count() > 0) {
            res.bsi[res.size] = C; // Carry bit
            res.size++;
          }
          /**/
        }
        // System.out.println("number="+number+" k="+k+" res="+res.SUM());
      }
      number >>= 1;
      k++;
    }
    // System.out.println(this.SUM()+"x"+orig+"="+res.SUM());
    return res;
  }
  
}
