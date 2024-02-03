package com.company.topK;
import java.io.Serializable;
import java.util.ArrayList;



public class bitArrayWAH implements Serializable{
    public String name = "";
    //public Vector vec; // list of regular code words
    public long[] vec;
    public activeWord active=new activeWord(); // the active word 
//    public long active;
    public int pos=0;
    public int maxPos=0;
    public double density = 0;
    /** sizeinbits: number of bits in the (uncompressed) bitmap. */
    public int sizeinbits = 0;
    static int [] countOnes = {0,1,1,2,1,2,2,3,1,2,2,3,2,3,3,4,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,
                               1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
                               1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
                               2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
                               1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
                               2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
                               2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
                               3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,4,5,5,6,5,6,6,7,5,6,6,7,6,7,7,8};
    /*
    public bitArrayWAH() {
        vec = new long[1]; //active = new activeWord(); 
    }
     */
    
    public int sizeInBits() {
        return this.sizeinbits;
      }
    
    public bitArrayWAH(int size) {
        vec = new long[size]; //active = new activeWord(); 
    }                               
    public bitArrayWAH(String name){        
        this.name = name;
        //vec = new Vector(); //active = new activeWord(); 
        vec = new long[1];
    }
    public bitArrayWAH(String name, int size){        
        this.name = name;
        //vec = new Vector(); //active = new activeWord(); 
        vec = new long[size];
    }
    public void appendWord() {
	//vec.addElement (new Long(active));        
        vec[maxPos]=active.value;
        maxPos++;
    }
    
    public void setWord (long word) {
        vec[pos] = word;
    }
    
    public void appendLiteral() {
	//Input: 63 literal bits stored in active.value.
	//Output: vec extended by 63 bits.        
	if (maxPos == 0) {
	    vec[maxPos] = active.value; // cbi = 1 
            maxPos++;
        } else {        
		long lastElementValue = vec[maxPos-1];		
		if (active.value == 0) 
		    if (lastElementValue == 0) 
			vec[maxPos-1]=0x8000000000000002L; // cbi = 3 
                    else if ((lastElementValue >>>62) ==2) //fill of zero
			vec[maxPos-1] = lastElementValue+1; // cbi = 4
                    else {
			vec[maxPos] = active.value; // cbi = 4
                        maxPos++;
                    }        
                else if (active.value == 0x7FFFFFFFFFFFFFFFL)  //one fill of ones
		    if (lastElementValue == active.value)
			vec[maxPos-1] = 0xC000000000000002L; // cbi = 4
		    else if ((lastElementValue >>>62) ==3 )  // fill of ones
			vec[maxPos-1] = lastElementValue+1; // cbi = 5
		    else {
			vec[maxPos] = active.value; // cbi = 5
                        maxPos++;
                    }
                else {
			vec[maxPos] = active.value; // cbi = 3
                        maxPos++;
                }
         }
	this.sizeinbits+=63;
    }

    public void appendFill(long n, long fillBit) {
	//Input: n and fillBit, describing a fill with 63n bits of fillBit
	//Output: vec extended by 63n bits of value fillBit.
	//COMMENT: Assuming active.nbits = 0 and n > 0.
	if (maxPos>0) {
	    long lastElementValue = vec[maxPos-1];	    	    
	    if (fillBit == 0)
		if ((lastElementValue >>>62)==2 ) // fill of zeros
		    vec[maxPos-1] = lastElementValue+n; // cbi = 3
                else if (lastElementValue == 0)
                    vec[maxPos-1] = 0x8000000000000000L + (n+1); // cbi = 3
		else {
		    vec[maxPos] = 0x8000000000000000L + n; // cbi = 3
                    maxPos++;
                }
	    else if ((lastElementValue >>>62) ==3) // fill of ones
		vec[maxPos-1] = lastElementValue+n; // cbi = 3
	    else {
		vec [maxPos] = 0xC000000000000000L + n; // cbi = 3
                maxPos++;
            }
	}
	else {
          //if (maxPos == 0)        
	    if (fillBit == 0) {
		vec[maxPos] = 0x8000000000000000L + n; // cbi = 3
                maxPos++;
            } else {
		vec[maxPos] = 0xC000000000000000L + n; // cbi = 3
                maxPos++;
            }
          /*
          else {
            System.out.println("The number of words is 1...what a waste!!");
	    if (fillBit == 0)
		appendWord(0);
	    else
		appendWord(0x7FFFFFFFL);	    
	  }
           */
        }
	this.sizeinbits+=(n*63);
    }
    
    
    public void appendWord(long word) {
	//vec.addElement (new Long(word));
        if (maxPos >= vec.length) {
            System.out.println("BAD BAD");
            return;
        } 
        vec[maxPos]=word;
        maxPos++;       
        this.sizeinbits+=63;
    }
  
    public int getNumberOfWords () {
	//return maxPos;        	
        long l=new Long(0);
	int nWords=0;
	int size = 0;
	
	for (int i=0; i<maxPos; i++ ) {            
	    l = vec[i];
	    if ((l>>>63) ==1 ) {
		nWords = (int)(l & 0x3FFFFFFFFFFFFFFFL);
	    } 
	    else {
		nWords = 1;
	    }
	    size+=nWords;
	}
	//aWords[0]--;
	//aWords[1]=l.intValue();
	return size;   
    }
        
    public String toString () {
	Long l;
	StringBuffer bitmap = new StringBuffer();
	//for (Enumeration e = vec.elements(); e.hasMoreElements(); ) {
        for (int i=0; i<maxPos; i++) {
	    l = vec[i];
            String str = ("00000000000000000000000000000000000000000000000000000000000000"+Long.toBinaryString(l.longValue()));            
	    bitmap.append(str.substring(str.length()-64)+" ");		            
            //bitmap.append(l.longValue()+",");
	}          
	return bitmap.toString();
    }     
    
    public int getCount(long x) {
//         
         return Long.bitCount(x);
      }

    public int count() {
        return getMatches();
       
    }
    
    public int getBit(int position) {
        int retValue = 0;
        pos = position/64;
        int offset = position%64;
        //System.out.println("vec["+pos+"]= "+vec[pos]);
        if ((vec[pos] & (1<<(63-offset)))!=0) {
            retValue = 1;
        }
        return retValue;
    }
    
    public void decode() { // Decode the word of it on position pos	
    if (pos < maxPos) {
		active.value = vec[pos];		
		if ((active.value >>>63) ==1) { // if fill
		    if ((active.value >>>62) ==3) // if fill of ones
			active.fill = 0x7FFFFFFFFFFFFFFFL;
		    else active.fill = 0; // cbi = 2
		    active.nWords = (int)(active.value & 0x3FFFFFFFFFFFFFFFL);
		    active.isFill = true; 
		} 
		else {
		    active.nWords = 1; // cbi = 1
		    active.isFill = false;
		}
    } else { // If the program reaches here it means that the bitmaps do not
      // have the same number of bits...
      active.nWords = 0;
      active.isFill = false;
      active.value = 0;
      System.out
          .println("WANT TO ACCESS A POSITION BEYOND THE SIZE OF THE BITMAP ("
              + name + ") Pos: " + pos + " MaxPos: " + maxPos + ".");
    }
    }    
    
    public void decode(int position) { // Decode the word of it on position pos	
        pos = position;
        decode();
    }    
    
    public int getMatches ( ) {
	//Returns the number of 1s in the uncompressed format of the bitVector, i.e. the number of records that 
	//have the bit set
	int matches = 0;
	pos = 0;
	while (pos < maxPos) {
	    decode();
	    if (active.isFill) {
		if (active.fill!=0) //The compressed words are all 1s
		    { matches = matches + 63*((int)active.nWords); }
	    } else {//The word is a literal
        
        matches += Long.bitCount(active.value);
	    }
	    pos++;
	}
	return matches;
    }
    
    
    
    public int countBits ( ) {
    	//Returns the number of 1s in the uncompressed format of the bitVector, i.e. the number of records that 
    	//have the bit set
    	int matches = 0;
    	pos = 0;
    	while (pos < maxPos) {
    	    decode();
    	    if (active.isFill) {
    		//if (active.fill!=0) //The compressed words are all 1s
    		     matches = matches + 63*((int)active.nWords); 
    	    } else {//The word is a literal
                    long n=active.value;
                    matches+=Long.bitCount(n);                  
    	    }
    	    pos++;
    	}
    	return matches;
        }
    
   
    
    public ArrayList<Integer> getSetBitIDs(){
    	//returns an arrayList containng IDs for the set bits
    	ArrayList<Integer> setBitIDs = new ArrayList<Integer>();
    	pos=0;
    	int curPos=0;
    
    	
    	while(pos<maxPos){
    		
	    		decode();
	    	if (active.isFill)
	    	{
	    		if (active.fill!=0) //fill of ones
	    		{
	    			for (int i=curPos; i<(curPos+(active.nWords*63)); i++)
	    				setBitIDs.add(i);
	    		}
	    		
	    		curPos=curPos+(active.nWords*63);    		
	    	} 
	    	else
	    	{
	    		long n=active.value;
	    		
	    		 while (n != 0)
	    		 {
	    		     long c = n & (0 - n);
	    		     int index = Long.numberOfTrailingZeros(c);
	    		     setBitIDs.add(index+curPos);    		    
	    		     n = n ^ c;
	    		 }
	    		
	    		 curPos=curPos+63;
	    	} 
	    	
	    	 pos++;
    	}    	
    	return setBitIDs;
    }
    
         
    public bitArrayWAH decompress () {
        bitArrayWAH bA = new bitArrayWAH(vec.length);       
        pos=0;        
        long value = 0L;
        long nWords = 0L;
        while (pos < maxPos) {            
                value = vec[pos];                
		if ((value >>>63) ==1 ) {
                    nWords = value & 0x3FFFFFFFFFFFFFFFL;                    
		    if ((value >>> 62) ==3) {                                            
                        for (int i=0; i<nWords; i++)
                            bA.appendWord(0x7FFFFFFFFFFFFFFFL);                        
                    } else {
                        for (int i=0; i<nWords; i++)
                            bA.appendWord(0L);                        
                    }		    
		} else {
                    bA.appendWord(value);
		}
                pos++;
	}
        return bA;
    }

	public bitArrayWAH and(bitArrayWAH y) {
		bitArrayWAH x =this;
        //if ((x==null) || (y==null)) return cloneBitmap("B0");        
        int nWords;
    bitArrayWAH z = new bitArrayWAH((int) (Math.max(x.maxPos, y.maxPos)));
    //    bitArrayWAH z = new bitArrayWAH(numberofWords+1);      
        x.pos=0; y.pos=0; 
        x.active.nWords=0;
        y.active.nWords=0;                
        while (x.pos < x.maxPos || y.pos < y.maxPos) {
        	
            if (x.active.nWords == 0) 
                {
                 x.decode();
                x.pos++; }
            if (y.active.nWords == 0) 
                { y.decode();
                y.pos++; }            
            //DEBUG: if (x.pos==x.vec.size()) System.out.println("HereANDx "+x.pos);
            //DEBUG: if (y.pos==y.vec.size()) System.out.println("HereANDy "+y.pos);            
            if (x.active.isFill) {
                if (y.active.isFill) {
                	//System.out.println("appendFill");
                    nWords = Math.min(x.active.nWords, y.active.nWords);
                    z.appendFill(nWords, x.active.fill & y.active.fill); 
                    x.active.nWords = x.active.nWords - nWords; y.active.nWords = y.active.nWords - nWords;
                } else {
                z.active.value = x.active.fill & y.active.value;
                z.appendLiteral(); 
                x.active.nWords--;
                y.active.nWords--;
                }
            } else {
                if (y.active.isFill) {                
                    z.active.value = y.active.fill & x.active.value;
                    z.appendLiteral();
                    x.active.nWords--;
                    y.active.nWords--;
                } else {
                    z.active.value = x.active.value & y.active.value;
                    x.active.nWords--; y.active.nWords--;
                    z.appendLiteral();
                }   
            }
        }   
       
        return z;      
        
    }        
	
	
	public bitArrayWAH or(bitArrayWAH y) {
		bitArrayWAH x=this;
        //Input: Two bitArrayWAH x and y containing the same number of bits.
        //Output: The result of a bitwise OR operation as z.
        //System.out.println(col1+"   "+col2+"   "+resultKey);
        //if (x==null) return cloneBitmap(y);
        //if (y==null) return cloneBitmap(x);
        bitArrayWAH z = new bitArrayWAH(x.vec.length);
        int nWords;
        x.pos=0; y.pos=0; 
        x.active.nWords=0;
        y.active.nWords=0;                
        while (x.pos < x.maxPos || y.pos < y.maxPos) {
            if (x.active.nWords == 0) 
                {
                 x.decode();
                x.pos++; }
            if (y.active.nWords == 0) 
                { y.decode();
                y.pos++; }            //DEBUG: if (x.pos==x.vec.size()) System.out.println("HereORx: "+x.pos);
            //DEBUG: if (y.pos==y.vec.size()) System.out.println("HereORy: "+y.pos);
            if (x.active.isFill)
                if (y.active.isFill) {
                nWords = Math.min(x.active.nWords, y.active.nWords);
                z.appendFill(nWords, (x.active.fill | y.active.fill));
                x.active.nWords = x.active.nWords - nWords; y.active.nWords = y.active.nWords - nWords;
                } else {
                z.active.value = x.active.fill | y.active.value;
                //		    z.appendWord();
                z.appendLiteral();
                x.active.nWords--;
                y.active.nWords--;
                } else if (y.active.isFill) {
                z.active.value = y.active.fill | x.active.value;
                //		z.appendWord();
                z.appendLiteral();
                x.active.nWords--;
                y.active.nWords--;
                } else {
                z.active.value = x.active.value | y.active.value;
                //		z.appendWord();
                z.appendLiteral();
                x.active.nWords--; y.active.nWords--;
                }
            }       
/*
        System.out.println("OR");
        System.out.println("x.pos:" + x.pos + " size:" + x.vec.size()+" x.nWords:" + x.active.nWords);
        System.out.println("y.pos:" + y.pos + " size:" + y.vec.size()+" y.nWords:" + y.active.nWords);
*/
        return z;
    }   
	
	 public bitArrayWAH xor(bitArrayWAH y) {
		 bitArrayWAH x =this;
	        //Input: Two bitArrayWAH x and y containing the same number of bits.
	        //Output: The result of a bitwise OR operation as z.
	        //System.out.println(col1+"   "+col2+"   "+resultKey);
	        //if (x==null) return NOT(y);
	        //if (y==null) return NOT(x);
	        bitArrayWAH z = new bitArrayWAH(x.vec.length);
	        int nWords;
	        x.pos=0; y.pos=0; 
	        x.active.nWords=0;
	        y.active.nWords=0;                
	        while (x.pos < x.maxPos || y.pos < y.maxPos) {
	            if (x.active.nWords == 0) 
	                {
	                 x.decode();
	                x.pos++; }
	            if (y.active.nWords == 0) 
	                { y.decode();
	                y.pos++; }            //DEBUG: if (x.pos==x.vec.size()) System.out.println("HereORx: "+x.pos);
	            //DEBUG: if (y.pos==y.vec.size()) System.out.println("HereORy: "+y.pos);
	            if (x.active.isFill)
	                if (y.active.isFill) {
	                nWords = Math.min(x.active.nWords, y.active.nWords);
	                if (x.active.fill == y.active.fill) {
	                    z.appendFill(nWords, 0L);
	                } else {
	                    z.appendFill(nWords, 0x7FFFFFFFFFFFFFFFL);                
	                }
	                x.active.nWords = x.active.nWords - nWords; y.active.nWords = y.active.nWords - nWords;
	                } else {
	                z.active.value = 0x7FFFFFFFFFFFFFFFL & (x.active.fill ^ y.active.value);
	                //		    z.appendWord();
	                z.appendLiteral();
	                x.active.nWords--;
	                y.active.nWords--;
	                } else if (y.active.isFill) {
	                z.active.value = 0x7FFFFFFFFFFFFFFFL & (x.active.value ^ y.active.fill) ;
	                //		z.appendWord();
	                z.appendLiteral();
	                x.active.nWords--;
	                y.active.nWords--;
	                } else {
	                z.active.value = 0x7FFFFFFFFFFFFFFFL & (x.active.value ^ y.active.value);
	                //		z.appendWord();
	                z.appendLiteral();
	                x.active.nWords--; y.active.nWords--;
	                }
	            }       
	        return z;
	    }   
	 
	 public bitArrayWAH not() {
		 bitArrayWAH x=this;
	        int nWords;
	        bitArrayWAH z = new bitArrayWAH(x.vec.length);      
	        x.pos=0; 
	        x.active.nWords=0;        
	        while (x.pos < x.maxPos) {
	            x.decode();
	            x.pos++;
	            if (x.active.isFill) {
	                nWords = x.active.nWords;
	                if (x.active.fill == 0) {//All 0s, put all 1s
	                    z.appendWord(0xC000000000000000L | x.active.value); 
	                } else {//All 1s
	                    z.appendWord(0xBFFFFFFFFFFFFFFFL & x.active.value);
	                }
	            } else {
	                z.active.value = 0x7FFFFFFFFFFFFFFFL & ~x.active.value;
	                z.appendWord(); 
	            }
	        }
	        return z;
	    } 
	
    
    
}    
