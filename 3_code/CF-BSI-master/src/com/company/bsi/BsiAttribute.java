package com.company.bsi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;


import com.company.hybridewah.HybridBitmap;

/**
 * 
 * @author gguzun
 * This defines the methods for a BsiAttribute. 
 * The least significant bit/slice is slice 0.
 */

public abstract class BsiAttribute implements Serializable{
	
	/**
	 * 
	 */
	int size;
	int offset = 0;
	int decimals=0;
	public HybridBitmap[] bsi;
	public HybridBitmap existenceBitmap = new HybridBitmap(2);
	long rows;
	long index; // if split horizontally
	boolean signed=false;
	boolean firstSlice=false; //contains first slice
	boolean lastSlice = false; //contains last slice
	HybridBitmap sign = new HybridBitmap(2);	
	boolean twosComplement=false;
	
	
	
	/**
	 * 
	 * @return if this attribute contains the last slice(most significant). For internal purposes (when splitting into sub attributes)
	 */
	public boolean isLastSlice(){
		return this.lastSlice;
	}
	/**
	 * 
	 * @param flag if the attribute contains the most significant slice then set it to true. Otherwise false.
	 */
	public void setLastSliceFlag(boolean flag){
		this.lastSlice=flag;
	}
	/**
	 * 
	 * @return if this attribute contains the first slice(least significant). For internal purposes (when splitting into sub attributes)
	 */
	public boolean isFirstSlice(){
		return this.firstSlice;
	}
	/**
	 * 
	 * @param flag if the attribute contains the least significant slice then set it to true. Otherwise false.
	 */
	public void setFirstSliceFlag(boolean flag){
		this.firstSlice=flag;
	}
	
	/**
	 * Returns false if contains only positive numbers 	  
	 */
	public boolean isSigned(){
		return this.signed;
	}
	
	
	/**
	 * Adds a slice to the attribute
	 * @param slice - the slice to be added
	 */
	public void addSlice(HybridBitmap slice){
//		if(size>0 && slice.sizeInBits()>this.bsi[size-1].sizeInBits())
//			System.out.println("here it is");
		this.bsi[size] = slice;
		this.size++;
	}
	
	/**
	 * Sets the number of slices in the attribute to s
	 * @param s - number of slices
	 */
	public void setNumberOfSlices(int s){
		this.size = s;
	}
	
	/**
	 * Returns the size of the bsi (how many slices are non zeros)	  
	 */
	public int getNumberOfSlices(){
		return this.size;
	}
	
	/**
	 * Returns the slice number i	  
	 */
	public HybridBitmap getSlice(int i){
		return this.bsi[i];
	}
	
	
	/**
	 * Returns the offset of the bsi (the first "offset" slices are zero, thus not encoding)
	 */
	public int getOffset(){
		return this.offset;
	}
	
	
	/**
	 * Sets the offset of the bsi (the first "offset" slices are zero, thus not encoding)
	 */
	public void setOffset(int offset){
		this.offset=offset;
	}
	
	/**
	 * Returns the number of rows for this attribute
	 */
	public long getNumberOfRows(){
		return this.rows;
	}
	
	
	/**
	 * Sets the number of rows for this attribute
	*/
	public void setNumberOfRows(long rows){
		this.rows=rows;
	}
	
	/**
	 * Returns the index(partition id if horizontally partitioned) for this attribute
	 */
	public long getPartitionID(){
		return this.index;
	}
	
	/**
	 * Sets the index(partition id if horizontally partitioned) for this attribute
	 */
	public void setPartitionID(long index){
		this.index=index;
	}
	
	/**
	 * Returns the Existence bitmap of the bsi attribute
	 */
	public HybridBitmap getExistenceBitmap(){
		return this.existenceBitmap;
	}
	
	/**
	 * Sets the existence bitmap of the bsi attribute 
	
	 */
	public void setExistenceBitmap(HybridBitmap exBitmap){
		this.existenceBitmap=exBitmap;
	}
	
	public void setTwosFlag(boolean flag){
		this.twosComplement=flag;
	}
	
	/**
	 * Computes the top-K tuples in a bsi-attribute. 
	 * @param k - the number in top-k
	 * @return a bitArray containing the top-k tuples
	 */
	public abstract HybridBitmap topKMax(int k);
	

	/**
	 * Computes the top-K tuples in a bsi-attribute. 
	 * @param k - the number in top-k
	 * @return a bitArray containing the top-k tuples
	 */
	public abstract HybridBitmap topKMin(int k);
	
	/**
	 * Executes the SUM operation between two BsiAttributes
	 * @param a - the attribute to be added to this attribute
	 * @return - the summation result
	 */
	public abstract BsiAttribute SUM(BsiAttribute a);
	
	/**
	 * Executes the SUM operation between a BsiAttribute and and integer
	 * @param a - integer to be added to each tuple
	 * @return - the summation result
	 */
	public abstract BsiAttribute SUM(long a);
	
	
	/**
	 * Executes the SUM operation between two BsiAttributes
	 * @param a - the attribute to be added to this attribute
	 * @return - the summation result
	 */
//	public abstract BsiAttribute SUM(BsiSigned a);
//	
//	/**
//	 * Multiplies the BsiAttribute by a constant
//	 * @param number - the constant number
//	 * @return - the result of the multiplication
//	 */
//	public BsiAttribute multiplyByConstant(int number);
	
	/**
	 * Multiplies the BsiAttribute by another BsiAttribute
	 * @param a - the other BsiAttribute
	 * @return - the result of the multiplication
	 */
//	public BsiAttribute multiply(BsiAttribute a);
	
//	public BsiAttribute subtract(BsiUnsigned a);
	
//	public BsiAttribute subtract(BsiSigned a);	
	
	
//	public BsiAttribute divideByConstant(int number);
	
//	public BsiAttribute divide(BsiAttribute a);
	
	
	/**
	 * Converts a Unsigned or Sign-magnitude BSI into a Two's comoplement BSI
	 * 
	 * @param bits - the number of bits allocated for the Two's comoplement BSI. (including the sign)
	 * @return a two's complement of @this
	 */
	public abstract BsiAttribute convertToTwos(int bits);
	
	

	//public void setTwosFlag();
	
	

//	public void addSliceWithOffset(HybridBitmap sign, int i);
	
	/**
	 * Change the sign of a two's complement attribute (for subtractions)
	 */
//	public void changeSign();
/**
 * builds a BSI attribute with all rows identical given one number (row)
 * @param query
 * @param rows
 * @return the BSI attribute with all rows identical
 */
	public BsiAttribute buildQueryAttribute(long query, int rows, long partitionID){
		
		if(query<0){
			BsiSigned res = new BsiSigned(Long.toBinaryString(Math.abs(query)).length()+1);
			res.setPartitionID(partitionID);
			for (int i=0; i<=Long.toBinaryString(Math.abs(query)).length();i++){
				boolean currentBit = (query&(1<<i))!=0;
				HybridBitmap slice = new HybridBitmap();
				slice.setSizeInBits(rows, currentBit);
				if(currentBit)
					slice.density=1;
				res.addSlice(slice);				
			}		
			
			res.existenceBitmap.setSizeInBits(rows,true);
			res.existenceBitmap.density=1;
			res.lastSlice=true;
			res.firstSlice=true;
			res.twosComplement=true;
			res.sign = res.bsi[res.size-1];			
			return res;	
		}
			
		else{
			BsiUnsigned res=new BsiUnsigned(Long.toBinaryString(Math.abs(query)).length());
			res.setPartitionID(partitionID);
			for (int i=0; i<Long.toBinaryString(Math.abs(query)).length();i++){
				boolean currentBit = (query&(1<<i))!=0;
				HybridBitmap slice = new HybridBitmap();
				slice.setSizeInBits(rows, currentBit);
				if(currentBit)
					slice.density=1;
				res.addSlice(slice);
			}
			res.existenceBitmap.setSizeInBits(rows,true);
			res.existenceBitmap.density=1;
			res.lastSlice=true;
			res.firstSlice=true;
			return res;	
		}		
	}
	
	/**
	 * 
	 * @param array
	 * @param compressThreshold
	 * @return
	 */
	public BsiAttribute buildBsiAttributeFromArray(Iterator<Long> iter, int attRows, double compressThreshold){
		long max=Long.MIN_VALUE;
		long min=Long.MAX_VALUE;
		long temp=0;
		int count=0;
		long[] array = new long[attRows];
		while(iter.hasNext()){
			temp=iter.next();
			array[count]=temp;
			count++;
			if(max<temp)
				max = temp;
			if(min>temp)
				min=temp;  //*** will the max and min not always be 1 and 0 respectively.
		}
		//can be negative
		int slices = Long.toBinaryString(Math.max(Math.abs(min), Math.abs(max))).length();
		
		if(min<0){
			BsiSigned res = new BsiSigned(slices+1);
                        long[][] bitSlices = bringTheBits(array, slices+1);
			
			for(int i=0; i<=slices; i++){
				double bitDensity = bitSlices[i][0]/(double)attRows; // the bit density for this slice
				double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
				if(compressRatio<compressThreshold){
					//build compressed bitmap
					HybridBitmap bitmap = new HybridBitmap();
					for(int j=1; j<bitSlices[i].length; j++){
						bitmap.add(bitSlices[i][j]);
					}
					//bitmap.setSizeInBits(attRows);					
					bitmap.density=bitDensity;

					res.addSlice(bitmap);					
										
				}else{
					//build verbatim Bitmap
					HybridBitmap bitmap = new HybridBitmap(true);
					bitmap.buffer=Arrays.copyOfRange(bitSlices[i], 1, bitSlices[i].length);
					bitmap.actualsizeinwords=bitSlices[i].length-1;
					//bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
					bitmap.setSizeInBits(attRows);
					bitmap.density=bitDensity;
					res.addSlice(bitmap);					
					
				}				
			}



			HybridBitmap ebBitmap = res.getSlice(0);
			for (int i=0; i< res.getNumberOfSlices(); i++){
				ebBitmap.or(res.getSlice(i));
			}
			res.existenceBitmap = ebBitmap;
			res.existenceBitmap.density=(ebBitmap.cardinality()/attRows);
			res.lastSlice=true;
			res.firstSlice=true;
			res.twosComplement=true;
			res.sign = res.bsi[res.size-1];
			return res;
			
		}else{
			BsiUnsigned res = new BsiUnsigned(slices);
			
			
			long[][] bitSlices = bringTheBits(array, slices);

			//here compression of result takes place if it is feasible(check by calculating compression ratio
			for(int i=0; i<slices; i++){
				double bitDensity = bitSlices[i][0]/(double)attRows; // the bit density for this slice
				double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
				if(compressRatio<compressThreshold){
					//build compressed bitmap
					HybridBitmap bitmap = new HybridBitmap();
					for(int j=1; j<bitSlices[i].length; j++){
						bitmap.add(bitSlices[i][j]);
					}
					//bitmap.setSizeInBits(attRows);					
					bitmap.density=bitDensity;
					res.addSlice(bitmap);
					
				}else{
					//build verbatim Bitmap
					HybridBitmap bitmap = new HybridBitmap(true);
					bitmap.buffer=Arrays.copyOfRange(bitSlices[i], 1, bitSlices[i].length);
					bitmap.actualsizeinwords=bitSlices[i].length-1;
					//bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
					bitmap.setSizeInBits(attRows);
					bitmap.density=bitDensity;
					res.addSlice(bitmap);
				}				
			}
			//what is this for?
			HybridBitmap ebBitmap = res.getSlice(0);
			for (int i=1; i< res.getNumberOfSlices(); i++){
				ebBitmap = ebBitmap.or(res.getSlice(i));
			}
			res.existenceBitmap = ebBitmap;
			res.existenceBitmap.density=((double) ebBitmap.cardinality()/attRows);
			res.lastSlice=true;
			res.firstSlice=true;
			return res;
		}

		
	}
	
	
	/**
	 * 
	 * @param array
	 * @param compressThreshold
	 * @return
	 */
	public BsiAttribute buildBsiAttributeFromArray(long[] array, long max, long min, long firstRowID, double compressThreshold){
		int attRows = array.length;
		int slices = Long.toBinaryString(Math.max(Math.abs(min), Math.abs(max))).length();
		
		if(min<0){
			BsiSigned res = new BsiSigned(slices+1,attRows, firstRowID);
                        long[][] bitSlices = bringTheBits(array, slices+1);
			
			for(int i=0; i<=slices; i++){
				double bitDensity = bitSlices[i][0]/(double)attRows; // the bit density for this slice
				double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
				if(compressRatio<compressThreshold){
					//build compressed bitmap
					HybridBitmap bitmap = new HybridBitmap();
					for(int j=1; j<bitSlices[i].length; j++){
						bitmap.add(bitSlices[i][j]);
					}
					//bitmap.setSizeInBits(attRows);					
					bitmap.density=bitDensity;
					res.addSlice(bitmap);					
										
				}else{
					//build verbatim Bitmap
					HybridBitmap bitmap = new HybridBitmap(true);
					bitmap.buffer=Arrays.copyOfRange(bitSlices[i], 1, bitSlices[i].length);
					bitmap.actualsizeinwords=bitSlices[i].length-1;
					//bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
					bitmap.setSizeInBits(attRows);
					bitmap.density=bitDensity;
					res.addSlice(bitmap);					
					
				}				
			}
			res.existenceBitmap.setSizeInBits(attRows,true);
			res.existenceBitmap.density=1;
			res.lastSlice=true;
			res.firstSlice=true;
			res.twosComplement=true;
			res.sign = res.bsi[res.size-1];
			return res;
			
		}else{
			BsiUnsigned res = new BsiUnsigned(slices,attRows, firstRowID);
			
			
			long[][] bitSlices = bringTheBits(array, slices);
			
			for(int i=0; i<slices; i++){
				double bitDensity = bitSlices[i][0]/(double)attRows; // the bit density for this slice
				double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
				if(compressRatio<compressThreshold){
					//build compressed bitmap
					HybridBitmap bitmap = new HybridBitmap();
					for(int j=1; j<bitSlices[i].length; j++){
						bitmap.add(bitSlices[i][j]);
					}
					//bitmap.setSizeInBits(attRows);					
					bitmap.density=bitDensity;
					res.addSlice(bitmap);
					
				}else{
					//build verbatim Bitmap
					HybridBitmap bitmap = new HybridBitmap(true);
					bitmap.buffer=Arrays.copyOfRange(bitSlices[i], 1, bitSlices[i].length);
					bitmap.actualsizeinwords=bitSlices[i].length-1;
					//bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
					bitmap.setSizeInBits(attRows);
					bitmap.density=bitDensity;
					res.addSlice(bitmap);
				}				
			}
			res.existenceBitmap.setSizeInBits(attRows,true);
			res.existenceBitmap.density=1;
			res.lastSlice=true;
			res.firstSlice=true;
			return res;
		}
		
		
	}
	
	
/**
 * 	
 * @param array
 * @param slices
 * @return res - An array of slices. The first word in each slice is the bit density of that slice.
 */
	
private long[][] bringTheBits(long[] array, int slices) {
	int attRows = array.length;
	int wordsNeeded = (int) Math.ceil((double) attRows / 64);
	long[][] bitmapDataRaw = new long[slices][wordsNeeded+1]; // one for the bit density (the first word in each slice)
	long thisBin = 0;
	for (int seq = 0; seq < attRows; seq++) {
		int w = (seq / 64)+1;
		int offset = seq % 64;				
		thisBin = array[seq];

		int slice = 0;
		while (thisBin != 0 && slice<slices) {
			if ((thisBin & 1) == 1) {
				bitmapDataRaw[slice][w] |= (1L << offset);
				bitmapDataRaw[slice][0]++; //update bit density
			}
				thisBin >>= 1;
				slice++;
			}
	}	
		
		return bitmapDataRaw;
	}

public HybridBitmap maj(HybridBitmap a, HybridBitmap b, HybridBitmap c) {
	// AB + BC + AC
	if(a.verbatim && b.verbatim && c.verbatim){
		return a.maj(b, c);
	}else{
		
		return a.and(b).or(b.and(c)).or(a.and(c));
	}	
}


public HybridBitmap XOR(HybridBitmap a, HybridBitmap b, HybridBitmap c) {
	// Input: Three bitArray w, x and y containing the same number of bits.
	// Output: The result of a bitwise OR operation as z.
	if(a.verbatim && b.verbatim && c.verbatim){
		return a.xor(b, c);
	}else{
		return a.xor(b).xor(c);
	}	
}

public HybridBitmap orAndNot(HybridBitmap a, HybridBitmap b, HybridBitmap c){
	if(a.verbatim && b.verbatim && c.verbatim){
		return a.orAndNotV(b,c);
	}else{
		return a.or(b.andNot(c));
	}
}

public HybridBitmap orAnd(HybridBitmap a, HybridBitmap b, HybridBitmap c){
	if(a.verbatim && b.verbatim && c.verbatim){
		return a.orAndV(b,c);
	}else{
		return a.or(b.and(c));
	}
}

public HybridBitmap and(HybridBitmap a, HybridBitmap b, HybridBitmap c){
	if(a.verbatim && b.verbatim && c.verbatim){
		return a.andV(b,c);
	}else{
		return a.and(b.and(c));
	}
}


public void signMagnitudeToTwos(int bits){			
	int i=0;
	for(i=0; i<this.getNumberOfSlices(); i++){
		this.bsi[i]=this.bsi[i].xor(this.sign);			
		}
	while(i<bits){ // sign extension
		this.addSlice(this.sign);
		i++;
	}
	if(this.firstSlice){			
		this.addOneSliceSameOffset(this.sign);
	}
	
	this.setTwosFlag(true);	
}

/**
 * 
 * @param bits number of bits for the twos complement encoding (In general unsignedCardinality+1 is sufficient)
 * @return
 */
public BsiAttribute signMagnToTwos(int bits){
	BsiAttribute res = new BsiSigned();
	res.twosComplement=true;
	int i=0,count=0;
	for(i=0; i<this.getNumberOfSlices(); i++){
		res.bsi[i]=this.bsi[i].xor(this.sign);			
		}
	while(i<bits){
		res.addSlice(this.sign);
		i++;
	}
	if(this.firstSlice){			
			res.addOneSliceSameOffset(this.sign);
		}
	res.size = i;
	return res;
}

public BsiAttribute TwosToSignMagnitue(){
	BsiAttribute res = new BsiSigned();
	for (int i=0; i<this.size; i++){
		res.bsi[i]=this.bsi[i].xor(this.bsi[this.size-1]);
	}if(this.firstSlice){			
		res.addOneSliceSameOffset(this.bsi[this.size-1]);
	}	
	return res;
	}

/**
 * This adds(summation) a slice to a BSI attribute
 */
public void addOneSliceSameOffset(HybridBitmap slice) {
	HybridBitmap zeroBitmap = new HybridBitmap();
	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);		
	
	HybridBitmap C = new HybridBitmap(),S;		
	S=this.bsi[0].xor(slice);
	C=this.bsi[0].and(slice);		
	this.bsi[0]=S;		
	int curPos =1;		
	while(C.cardinality()>0){
		if(curPos<this.size){
		S=C.xor(this.bsi[curPos]);
		C=C.and(this.bsi[curPos]);
		this.bsi[curPos]=S;
		curPos++;
		}else{
			this.addSlice(C);
			return;
		}			
	}		
}

/**
 * This adds(summation) a slice to a BSI attribute. Discards the carry slice
 */
public void addOneSliceDiscardCarry(HybridBitmap slice) {
	HybridBitmap zeroBitmap = new HybridBitmap();
	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);		
	
	HybridBitmap C = new HybridBitmap(),S;		
	S=this.bsi[0].xor(slice);
	C=this.bsi[0].and(slice);		
	this.bsi[0]=S;		
	int curPos =1;		
	while(C.cardinality()>0){
		if(curPos<this.size){
		S=C.xor(this.bsi[curPos]);
		C=C.and(this.bsi[curPos]);
		this.bsi[curPos]=S;
		curPos++;
		}			
	}		
}


/**
 * This adds(summation) a slice to a BSI attribute
 */
public void addOneSliceNoSignExt(HybridBitmap slice) {
	HybridBitmap zeroBitmap = new HybridBitmap();
	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);		
	
	HybridBitmap C = new HybridBitmap(),S;		
	S=this.bsi[0].xor(slice);
	C=this.bsi[0].and(slice);		
	this.bsi[0]=S;		
	int curPos =1;		
	while(C.cardinality()>0){
		if(curPos<this.size){
		S=C.xor(this.bsi[curPos]);
		C=C.and(this.bsi[curPos]);
		this.bsi[curPos]=S;
		curPos++;
		}else return;		
	}		
}

public void applyExsistenceBitmap(HybridBitmap ex){
	this.existenceBitmap = ex;
	for(int i=0; i< this.size; i++){
		this.bsi[i] = this.bsi[i].and(ex);
	}
	this.addSlice(ex.NOT());
}


public abstract BsiUnsigned abs();
/**
 * Extracts the absolute value given an existence bitmap and the number of slices that is enough to encode the absolute value.
 * @param resultSlices
 * @param EB
 * @return
 */
public abstract BsiUnsigned abs(int resultSlices, HybridBitmap EB);

public abstract long getValue(int pos);

public abstract HybridBitmap rangeBetween(long lowerBound, long upperBound);

public abstract BsiUnsigned absScale(double range);
	



}
