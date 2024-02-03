package com.company.bsi;
import com.company.hybridewah.HybridBitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Gheorghi Guzun
 */

public class BsiAttributeEWAH implements Serializable {

	/**
   * 
   */
	
	static int wordsNeeded = 0;
	int size;
	int offset = 0;
	public HybridBitmap[] bsi;

	public BsiAttributeEWAH() {
		size = 0;
		bsi = new HybridBitmap[64];

	}
	
	
	public BsiAttributeEWAH(int maxSize) {
		size = 0;
		bsi = new HybridBitmap[maxSize];

	}
	

	/**
	 * 
	 * Adds new slice to the BSI array. Increases the array size if necessary.
	 * (gguzun)
	 * 
	 * @param slice
	 * 
	 */
	public void add(HybridBitmap slice) {
		
//		  if(this.size==bsi.length){ 
//			  EWAHCompressedBitmap[] temp = bsi; 
//			  bsi = new  EWAHCompressedBitmap[size * 3 / 2 + 1]; 
//			  for (int i = 0; i < temp.length; i++) {
//		  bsi[i] = temp[i]; } bsi[size] = slice; size++;
//		  
//		  }
		 
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

	/**
	 * Returns the size of the bsi (how many slices are non zeros)
	 * 
	 * 
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * Returns the offset of the bsi (the first "offset" slices are zero, thus not encoding)
	 * 
	 * 
	 */
	public int getOffset() {
		return this.offset;
	}
	
	/**
	 * Sets the offset of the bsi (the first "offset" slices are zero, thus not encoding)
	 * 
	 * 
	 */
	public void setOffset(int offset) {
		this.offset=offset;
	}
	
	
	/**
	 * Returns the size of this BSI attribute
	 * 
	 * 
	 */
	public int getSizeInBytes() {
		int totSize = 0;
		for(int i=0; i< this.size; i++){
			totSize+=this.bsi[i].sizeInBytes();			
		}
		return totSize;
	}

	/**
	 * Adds otherBSI to this BsiAttribute. Saves the result in this BsiAttribute
	 * 
	 * @param otherBSI
	 * @param offset
	 */
	
	
	

//	public void BSI_SUM(BsiAttributeEWAH otherBSI, int offset) {
//		if (this.bsi[0] == null) {
//
//			for (int i = 0; i < offset; i++) {
//				this.bsi[i] = topPrefEWAH.bitmapindex.getBitmap("B0");
//			}
//			for (int i = offset; i < otherBSI.size + offset; i++) {
//				this.bsi[i] = otherBSI.bsi[i - offset];
//			}
//			for (int i = otherBSI.size + offset; i < 32; i++) {
//				this.bsi[i] = topPrefEWAH.bitmapindex.getBitmap("B0");
//			}
//			this.setSize(otherBSI.size + offset);
//
//		} else {
//			// int r = Math.min(this.size, otherBSI.size);
//			EWAHCompressedBitmap A = this.bsi[offset];
//			EWAHCompressedBitmap B = otherBSI.bsi[0];
//
//
//			// bitArray64 S = XOR(A, B);
//			// bitArray64 C = AND(A, B);
//			EWAHCompressedBitmap S = A.xor(B);
//			EWAHCompressedBitmap C = A.and(B);
//			// >>>>>>> 22849d63628ecc6b7f8a13c42e15dff479c4e528
//			this.bsi[offset] = S;
//			int i;
//			// if(this.size>(otherBSI.size+offset)){
//
//			for (i = 1; i < otherBSI.size; i++) {
//				A = this.bsi[i + offset];
//				B = otherBSI.bsi[i];
//				// S = XOR(XOR(A, B), C);
//				S = A.xor(B).xor(C);
//				// C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
//				// C = maj(A, B, C);
//				C = A.and(B).or(B.and(C)).or(A.and(C));
//				this.bsi[i + offset] = S;
//			}
//
//			this.size = Math.max(i + offset, this.size);
//			while (C.cardinality() > 0) {
//				A = this.bsi[i + offset];
//				// sum = XOR_AND(A, C);
//				// S = XOR(A, C);
//				S = A.xor(C);
//				this.bsi[i + offset] = S;
//				// C = AND(A, C);
//				C = A.and(C);
//				i++;
//				this.size = Math.max(i + offset, this.size);
//			}
//
//		}
//	}

	

	public HybridBitmap XOR_AND(HybridBitmap x, HybridBitmap y, HybridBitmap c) {
		HybridBitmap s = new HybridBitmap();

		s = x.xor(y);
		c = x.and(y);

		return s;
	}
	
/**
 * Builds a BSI attribute from an array of longs withoud knowing the number of slices	
 * @param array
 * @param compressThresh - the threshold for compressing (compresses only if the compressed size is smaller than the verbatim size *@param compressThresh)
 */
public void buildBSI (long[] array, double compressThresh){
		
		
		//int attributeID=0;
		//List<Long> att = new ArrayList<Long>();
		long max = 0;	
		for(int i=0; i< array.length; i++){
			if(max<array[i])
				max=array[i];
		}		
			int attRows = array.length;
			
			int slices = Long.toBinaryString(max).length(); // local maximum
			this.bsi = new HybridBitmap[slices];
			long[] setBitsCounter = new long[slices];
			int wordsNeeded = (int) Math.ceil((double) attRows / 64);
			long[][] bitmapDataRaw = new long[slices][wordsNeeded];
			long thisBin = 0;
			for (int seq = 0; seq < attRows; seq++) {
				int w = seq / 64;
				int offset = seq % 64;				
				thisBin = array[seq];
				// System.out.print(seq+" "+a+" "+w+" "+offset+" "+thisBin+"          ");
				// bitmapData[a][thisBin].vec[w] |= power2[offset];
				int slice = 0;
				while (thisBin > 0) {
					if ((thisBin & 1) == 1) {
						bitmapDataRaw[slice][w] |= (1L << offset);
						setBitsCounter[slice]++;
					}
						thisBin >>= 1;
						slice++;
					}
			}			
			for(int i=0; i<slices; i++){
				double bitDensity = setBitsCounter[i]/(double)attRows; // the bit density for this slice
				double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
				if(compressRatio<compressThresh){
					//build compressed bitmap
					HybridBitmap bitmap = new HybridBitmap();
					for(int j=0; j<bitmapDataRaw[i].length; j++){
						bitmap.add(bitmapDataRaw[i][j]);
					}
					//bitmap.setSizeInBits(attRows);					
					bitmap.density=bitDensity;
					this.add(bitmap);
					
				}else{
					//build verbatim Bitmap
					HybridBitmap bitmap = new HybridBitmap(true);
					bitmap.buffer=bitmapDataRaw[i];
					bitmap.actualsizeinwords=bitmapDataRaw[i].length;
					//bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
					bitmap.setSizeInBits(attRows);
					bitmap.density=bitDensity;
					this.add(bitmap);
				}			
			}							
	}


/**
 * Builds a BSI attribute from an array of longs withoud knowing the number of slices	
 * @param array - the array to be built into the BSI attribute
 * @param compressThresh - the threshold for compressing (compresses only if the compressed size is smaller than the verbatim size *@param compressThresh)
 * @param slices - the number of slices this attribute needs
 */
public void buildBSI (long[] array, double compressThresh, int slices){
		
		
		//int attributeID=0;
		//List<Long> att = new ArrayList<Long>();
//		long max = 0;	
//		for(int i=0; i< array.length; i++){
//			if(max<array[i])
//				max=array[i];
//		}		
//			int attRows = array.length;
//			
//			int slices = Long.toBinaryString(max).length(); // local maximum
	        int attRows = array.length;
			this.bsi = new HybridBitmap[slices];
			long[] setBitsCounter = new long[slices];
			int wordsNeeded = (int) Math.ceil((double) attRows / 64);
			long[][] bitmapDataRaw = new long[slices][wordsNeeded];
			long thisBin = 0;
			for (int seq = 0; seq < attRows; seq++) {
				int w = seq / 64;
				int offset = seq % 64;				
				thisBin = array[seq];
				// System.out.print(seq+" "+a+" "+w+" "+offset+" "+thisBin+"          ");
				// bitmapData[a][thisBin].vec[w] |= power2[offset];
				int slice = 0;
				while (thisBin > 0) {
					if ((thisBin & 1) == 1) {
						bitmapDataRaw[slice][w] |= (1L << offset);
						setBitsCounter[slice]++;
					}
						thisBin >>= 1;
						slice++;
					}
			}			
			for(int i=0; i<slices; i++){
				double bitDensity = setBitsCounter[i]/(double)attRows; // the bit density for this slice
				double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
				if(compressRatio<compressThresh){
					//build compressed bitmap
					HybridBitmap bitmap = new HybridBitmap();
					for(int j=0; j<bitmapDataRaw[i].length; j++){
						bitmap.add(bitmapDataRaw[i][j]);
					}
					//bitmap.setSizeInBits(attRows);					
					bitmap.density=bitDensity;
					this.add(bitmap);
					
				}else{
					//build verbatim Bitmap
					HybridBitmap bitmap = new HybridBitmap(true);
					bitmap.buffer=bitmapDataRaw[i];
					bitmap.actualsizeinwords=bitmapDataRaw[i].length;
					//bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
					bitmap.setSizeInBits(attRows);
					bitmap.density=bitDensity;
					this.add(bitmap);
				}				
			}			
			//attributeID++;
				
				
	}


	

	/**
	 * XOR with three inputs
	 * 
	 * @param a            
	 * @param b
	 * @param c
	 * @return a^b^c
	 */
	public HybridBitmap XOR(HybridBitmap a, HybridBitmap b, HybridBitmap c) {
		// Input: Three bitArray w, x and y containing the same number of bits.
		// Output: The result of a bitwise OR operation as z.
		if(a.verbatim && b.verbatim && c.verbatim){
			return a.xor(b, c);
		}else{
			return a.xor(b).xor(c);
		}	
		
		
	}

	

	public HybridBitmap maj(HybridBitmap a, HybridBitmap b, HybridBitmap c) {
		// AB + BC + AC
		if(a.verbatim && b.verbatim && c.verbatim){
			return a.maj(b, c);
		}else{
			
			return a.and(b).or(b.and(c)).or(a.and(c));
		}	
	}

	/**
	 * Computes the top-K tuples in a bsi-attribute.
	 * 
	 * @param k
	 *            - the number in top-k
	 * @return a bitArray containing the top-k tuples
	 */


	public HybridBitmap topKMax(int k) {
		HybridBitmap topK, SE, X;
//		EWAHCompressedBitmap G = topPrefEWAH.bitmapindex.getBitmap("B0");
//		EWAHCompressedBitmap E = topPrefEWAH.bitmapindex.getBitmap("B1");
		
		HybridBitmap G = new HybridBitmap();
		G.setSizeInBits(this.bsi[0].sizeInBits(),false);
		HybridBitmap E = new HybridBitmap();	
		E.setSizeInBits(this.bsi[0].sizeInBits(),true);
		E.density=1;
		
		int n = 0;

		for (int i = this.size - 1; i >= 0; i--) {
			SE = E.and(this.bsi[i]);
			X = G.or(SE);
			n = X.cardinality();

			if (n > k) {
				E = SE;
			}
			if (n < k) {
				G = X;
				E = E.andNot(this.bsi[i]);

			}
			if (n == k) {
				E = SE;
				break;
			}
		}
		n = G.cardinality();
		topK = G.or(E);
		// topK = OR(G, E.first(k - n+ 1));

		return topK;
	}
	
	public HybridBitmap topKMax_verbatim(int k) {
		HybridBitmap topK, SE, X;
		//EWAHCompressedBitmap G = topPrefEWAH.bitmapindex.getBitmap("B0");
		//EWAHCompressedBitmap E = topPrefEWAH.bitmapindex.getBitmap("B1");
		
		int bitmapSize = this.bsi[0].sizeInBits()/this.bsi[0].wordinbits;
		HybridBitmap G = new HybridBitmap(true, bitmapSize);
		HybridBitmap E = new HybridBitmap(true, bitmapSize);
		for(int i=0; i<bitmapSize; i++){
			G.addVerbatim(0);
			E.addVerbatim(0xFFFFFFFFFFFFFFFFL);
		}
		E.density = 1;
		
		
		int n = 0;

		for (int i = this.size - 1; i >= 0; i--) {
			SE = E.andV(this.bsi[i]);
			X = G.orV(SE);
			n = X.cardinality();

			if (n > k) {
				E = SE;
			}
			if (n < k) {
				G = X;
				E = E.andNotV(this.bsi[i]);

			}
			if (n == k) {
				E = SE;
				break;
			}
		}
		n = G.cardinality();
		topK = G.orV(E);
		// topK = OR(G, E.first(k - n+ 1));

		return topK;
	}

		
	public long SUM() {
		long sum = 0;
		HybridBitmap B_i;
		for (int i = 0; i < size; i++) {
			B_i = bsi[i];
			sum += 1<<(offset + i) * B_i.cardinality();
		}
		return sum;
	}
	
	public long SUM(int pos) {
		long sum = 0;
		HybridBitmap B_i;
		
		
		for (int i = 0; i < size; i++) {
			B_i = bsi[i];
			if(B_i.getBit(pos)) 
			sum += 1<<(offset + i);
		}
		return sum;
	}
	
	

	
	public BsiAttributeEWAH addSliceWithOffset(BsiAttributeEWAH a) {
		int wordsInSlice=this.bsi[0].actualsizeinwords;
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.addStreamOfEmptyWords(false,wordsInSlice);
		BsiAttributeEWAH base, slice;
		if (a.size==1){
			base=this;
			slice=a;
		}else{
			base=a;
			slice=this;
		}
		BsiAttributeEWAH res = new BsiAttributeEWAH();
					for(int i=0;i<base.size;i++){
				res.bsi[i]=base.bsi[i];				
			}
			res.size=base.size;
			res.offset=base.offset;
		
		
		HybridBitmap A = base.bsi[slice.offset-base.offset];
		HybridBitmap C = new HybridBitmap(),S;
		if(A==null){
			A=zeroBitmap;}
			
		S=A.xor(slice.bsi[0]);
		C=A.and(slice.bsi[0]);
		
		res.bsi[slice.offset-base.offset]=S;
		int curPos = slice.offset-base.offset+1;
		
		while(C.cardinality()>0){
			A=res.bsi[curPos];
			if(A==null){
				A=zeroBitmap;}
			S=C.xor(A);
			C=C.and(A);
			res.bsi[curPos]=S;
			curPos++;
		}
		res.size=Math.max(curPos, res.size);
		
		return res;
	}
	
	public BsiAttributeEWAH SUM_BSI_withOffset(BsiAttributeEWAH a) {
		//int verbatimSizeInWords = (int)Math.ceil((double)this.bsi[0].sizeInBits()/64);
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		//zeroBitmap.addStreamOfEmptyWords(false,verbatimSizeInWords);
	
		BsiAttributeEWAH res = new BsiAttributeEWAH();
		int i = 0, s = a.size, p = this.size;
		int aStart = 0, thisStart = 0;
		
	
		if (a.offset == this.offset) {
			// do the sum as normal
			res.offset = a.offset;
		} else if (a.offset > this.offset) {
			// start the sum at a[0] + res[a.offset], res_< a.offset remain unchanged
			int unchanged = a.offset - this.offset; // justAdd. slices can be just added from the result with the minimum offset
			for (i = 0; i < unchanged ; i++) { //Math.min(unchanged, p)
				// str = Integer.toString(i);
				if(i<p)
				res.bsi[i] = this.bsi[i];
				else
					res.bsi[i]=zeroBitmap;
			}
			thisStart = i;
			p = (p - i)>0?(p-i):0; //
			res.offset = this.offset;
			res.size = i;
		} else {// a.offset < this.offset
			// copy the slices into
			int appendFromA = this.offset - a.offset; // justAdd slices can be just added from the result with the minimum offset
			for (i = 0; i < appendFromA ; i++) { //Math.min(appendFromA, s)
				// str = Integer.toString(i);
				if(i<s)
				res.bsi[i] = a.bsi[i];
				else
					res.bsi[i]=zeroBitmap;
			}
			aStart = i;
			s = (s - i)>0?(s-i):0;
			res.offset = a.offset;
			res.size = i;
		}

		int q = Math.max(s, p);
		int r = Math.min(s, p);

		HybridBitmap A, B;
		HybridBitmap C = new HybridBitmap(), S;
		A = a.bsi[aStart];
		B = this.bsi[thisStart];
		if (A == null ) {
			A= zeroBitmap;			
		}
		
		if (B == null ) {
			B=zeroBitmap;			
		}
		
		S = A.xor(B);
		
		
		C = A.and(B);
		

		// S = XOR_AND(A, B, C);
		res.bsi[res.size] = S;
		for (i = 1; i < r; i++) {
			A = a.bsi[aStart + i];
			B = this.bsi[thisStart + i];
			if (A == null ) {
				A= zeroBitmap;				
			}
			
			if (B == null ) {
				B= zeroBitmap;				
			}
			//S = A.xor(B).xor(C);
			
			 S = XOR(A, B, C);
			res.bsi[res.size + i] = S;
		    C = maj(A, B, C);
			
			//C = A.and(B).or(B.and(C)).or(A.and(C));
			
		}
		if (s > p) {
			for (i = p; i < s; i++) {
				A = a.bsi[aStart + i];
				if (A == null ) {
					A= zeroBitmap;					
				}
				
				
				S = A.xor(C);
				
				
				C = A.and(C);
				
				// S = XOR_AND(A, C, C);
				res.bsi[res.size + i] = S;

			}
		} else {
			for (i = s; i < p; i++) {
				B = this.bsi[thisStart + i];
								
				if (B == null ) {
					B= zeroBitmap;
				}
				
				
				S = B.xor(C);
				
				
				C = B.and(C);
				
				// S = XOR_AND(B, C, C);
				res.bsi[res.size + i] = S;
			}
		}
		if (C.cardinality() > 0) {
			res.bsi[res.size + q] = C; // Carry bit
			q++;
		}
		res.size = res.size + q;	
		
		return res;
	}
	
	
	public BsiAttributeEWAH SUM_BSI_noOffset(BsiAttributeEWAH a) {		
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttributeEWAH res = new BsiAttributeEWAH();
		int i = 0, s = a.size, p = this.size;
		int minSP = Math.min(s, p);
		
		res.bsi[0] = this.bsi[0].xor(a.bsi[0]);
		HybridBitmap C = this.bsi[0].and(a.bsi[0]);
		res.size=1;
		
		for(i=1; i<minSP; i++){
			//res.bsi[i] = this.bsi[i].xor(a.bsi[i].xor(C));
			res.bsi[i] = XOR(this.bsi[i], a.bsi[i], C);
			//res.bsi[i] = this.bsi[i].xor(this.bsi[i], a.bsi[i], C);
			C= maj(this.bsi[i], a.bsi[i], C);			
			res.size++;
		}
		
		if(s>p){
			for(i=p+1; i<=s;i++){
				res.bsi[i] = a.bsi[i].xor(C);
				C=a.bsi[i].and(C);
				res.size++;
			}
		}else{
			for(i=s+1; i<=p;i++){
				res.bsi[i] = this.bsi[i].xor(C);
				C = this.bsi[i].and(C);
				res.size++;
			}
		}
		if(C.cardinality()>0){
			res.bsi[size]= C;
			res.size++;
		}
		
		
		return res;
	}
	
	
	public BsiAttributeEWAH SUM_BSI_offset(BsiAttributeEWAH a) {		
		HybridBitmap zeroBitmap = new HybridBitmap();
		if(this.bsi[0]==null){
			System.out.println("Check this out");
		}
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttributeEWAH res = new BsiAttributeEWAH();
		int i = 0, s = a.size, p = this.size;
		
		
		
		int minOffset = Math.min(a.offset, this.offset);
		res.offset = minOffset;
		
		int aIndex = 0;
		int thisIndex =0;
		
		if(this.offset>a.offset){
			for(int j=0;j<this.offset-minOffset; j++){
				if(j<a.size)
				res.bsi[res.size]=a.bsi[aIndex];
				else
					res.bsi[res.size]=zeroBitmap;
				aIndex++;
				res.size++;
			}
		}else if(a.offset>this.offset){
			for(int j=0;j<a.offset-minOffset;j++){
				if(j<this.size)
				res.bsi[res.size]=this.bsi[thisIndex];
				else
					res.bsi[res.size]=zeroBitmap;
				res.size++;
				thisIndex++;
			}
		}
		//adjust the remaining sizes for s and p
		s=s-aIndex;
		p=p-thisIndex;
		int minSP = Math.min(s, p);
		
		if(minSP<0){ // one of the BSI attributes is exausted
			for(int j=aIndex; j<a.size;j++){
				res.bsi[res.size]=a.bsi[j];
				res.size++;
			}
			for(int j=thisIndex; j<this.size;j++){
				res.bsi[res.size]=this.bsi[j];
				res.size++;
			}
			return res;
		}else {
			
			res.bsi[res.size] = this.bsi[thisIndex].xor(a.bsi[aIndex]);
			HybridBitmap C = this.bsi[thisIndex].and(a.bsi[aIndex]);
			res.size++;
			thisIndex++;
			aIndex++;
			
			for(i=1; i<minSP; i++){
				//res.bsi[i] = this.bsi[i].xor(a.bsi[i].xor(C));
				res.bsi[res.size] = XOR(this.bsi[thisIndex], a.bsi[aIndex], C);
				//res.bsi[i] = this.bsi[i].xor(this.bsi[i], a.bsi[i], C);
				C= maj(this.bsi[thisIndex], a.bsi[aIndex], C);			
				res.size++;
				thisIndex++;
				aIndex++;
			}
			
			if(s>p){
				for(i=p; i<s;i++){
					res.bsi[res.size] = a.bsi[aIndex].xor(C);
					C=a.bsi[aIndex].and(C);
					res.size++;
					aIndex++;
				}
			}else{
				for(i=s; i<p;i++){
					res.bsi[res.size] = this.bsi[thisIndex].xor(C);
					C = this.bsi[thisIndex].and(C);
					res.size++;
					thisIndex++;
				}
			}
			if(C.cardinality()>0){
				res.bsi[res.size]= C;
				res.size++;
			}		
			
			for(int l=0;l<res.size; l++){
			  if(res.bsi[l]==null)
				  System.out.println("Null on slice "+ l);
			}
			return res;			
		}	
	}

	
	
	

	public BsiAttributeEWAH SUM_BSI_inPlace(BsiAttributeEWAH a) {
		BsiAttributeEWAH res = new BsiAttributeEWAH();
		int i = 0, s = a.size, p = this.size;
		int aStart = 0, thisStart = 0;
		
	
		if (a.offset == this.offset) {
			// do the sum as normal
			res.offset = a.offset;
		} else if (a.offset > this.offset) {
			// start the sum at a[0] + res[a.offset], res_< a.offset remain
			// unchanged
			int unchanged = a.offset - this.offset; // justAdd slices can be
													// just
													// added from the result
													// with the
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
			int appendFromA = this.offset - a.offset; // justAdd slices can be
														// just
														// added from the result
														// with
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

		HybridBitmap A, B;
		HybridBitmap C = new HybridBitmap(), S;
		A = a.bsi[aStart];
		B = this.bsi[thisStart];
//		if (A == null || B == null) {
//			System.out.println("A or B is null");
//		}
		S = A.xor(B);
		
		
		C = A.and(B);
		

		// S = XOR_AND(A, B, C);
		res.bsi[res.size] = S;
		for (i = 1; i < r; i++) {
			A = a.bsi[aStart + i];
			B = this.bsi[thisStart + i];
			S = A.xor(B).xor(C);
			
			// S = XOR(A, B, C);
			res.bsi[res.size + i] = S;
			// C = maj(A, B, C);
			
			C = A.and(B).or(B.and(C)).or(A.and(C));
			
		}
		if (s > p) {
			for (i = p; i < s; i++) {
				A = a.bsi[aStart + i];
				S = A.xor(C);
				
				
				C = A.and(C);
				
				// S = XOR_AND(A, C, C);
				res.bsi[res.size + i] = S;

			}
		} else {
			for (i = s; i < p; i++) {
				B = this.bsi[thisStart + i];
				S = B.xor(C);
				
				
				C = B.and(C);
				
				// S = XOR_AND(B, C, C);
				res.bsi[res.size + i] = S;
			}
		}
		if (C.cardinality() > 0) {
			res.bsi[res.size + q] = C; // Carry bit
			q++;
		}
		res.size = res.size + q;
		return res;
	}
	
	public BsiAttributeEWAH SUM_BSI_verbatim(BsiAttributeEWAH a) {
		BsiAttributeEWAH res = new BsiAttributeEWAH();
		int i = 0, s = a.size, p = this.size;
		int aStart = 0, thisStart = 0;
		
	
		if (a.offset == this.offset) {
			// do the sum as normal
			res.offset = a.offset;
		} else if (a.offset > this.offset) {
			// start the sum at a[0] + res[a.offset], res_< a.offset remain
			// unchanged
			int unchanged = a.offset - this.offset; // justAdd slices can be
													// just
													// added from the result
													// with the
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
			int appendFromA = this.offset - a.offset; // justAdd slices can be
														// just
														// added from the result
														// with
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

		HybridBitmap A, B;
		HybridBitmap C = new HybridBitmap(), S;
		A = a.bsi[aStart];
		B = this.bsi[thisStart];
//		if (A == null || B == null) {
//			System.out.println("A or B is null");
//		}
		S = A.xorV(B);
		
		
		C = A.andV(B);
		

		// S = XOR_AND(A, B, C);
		res.bsi[res.size] = S;
		for (i = 1; i < r; i++) {
			A = a.bsi[aStart + i];
			B = this.bsi[thisStart + i];
			//S = A.xorV(B).xorV(C);
			
			 S = XOR(A, B, C);
			res.bsi[res.size + i] = S;
			 C = maj(A, B, C);
			
			//C = A.andV(B).orV(B.andV(C)).orV(A.andV(C));
			
		}
		if (s > p) {
			for (i = p; i < s; i++) {
				A = a.bsi[aStart + i];
				S = A.xorV(C);
				
				
				C = A.andV(C);
				
				// S = XOR_AND(A, C, C);
				res.bsi[res.size + i] = S;

			}
		} else {
			for (i = s; i < p; i++) {
				B = this.bsi[thisStart + i];
				S = B.xorV(C);
				
				
				C = B.andV(C);
				
				// S = XOR_AND(B, C, C);
				res.bsi[res.size + i] = S;
			}
		}
		if (C.cardinality() > 0) {
			res.bsi[res.size + q] = C; // Carry bit
			q++;
		}
		res.size = res.size + q;
		return res;
	}

	public BsiAttributeEWAH multiply_inPlace(int number) { // k is the offset
		// System.out.println("Multiply by "+number);
		
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttributeEWAH res = null;
		
		HybridBitmap C, S;
		int k = 0;
		while (number > 0) {
			if ((number & 1) == 1) {
				if (res == null) {
					res = new BsiAttributeEWAH();
					res.offset = k;
					for (int i = 0; i < this.size; i++) {
						res.bsi[i] = this.bsi[i];
					}					
					res.size = this.size;
					k = 0;
				} else {
					/* Move the slices of res k positions */
					HybridBitmap A, B;
					A = res.bsi[k];
					B = this.bsi[0];
					if(A==null){ 
						A= zeroBitmap;						
						res.size=k+1;
					}
					// if (A==null || B==null) {
					// System.out.println("A or B is null");
					// }
					C = new HybridBitmap();
					S = A.xor(B);
					C = A.and(B);
					// S = XOR_AND(A, B, C);
					res.bsi[k] = S;
					
					// C = Sum[1];
					for (int i = 1; i < this.size; i++) {// Add the slices of
															// this to the
															// current res
						A = res.bsi[i + k];
						B = this.bsi[i];
						if (A == null) {
							S = B.xor(C);
							C = B.and(C);
							res.size++;
							// S = XOR_AND(B, C, C);							
						} else {
							S = A.xor(B).xor(C);
							// S = XOR(A, B, C);
							C = A.and(B).or(B.and(C)).or(A.and(C));
							// C = maj(A, B, C); // OR(OR(AND(A, B), AND(A, C)),
												// AND(C, B));
						}
						res.bsi[i + k] = S;
					}
					for (int i = this.size + k; i < res.size; i++) {// Add the
																	// remaining
																	// slices of
																	// res with
																	// the Carry
																	// C
						A = res.bsi[i];
						S = A.xor(C);
						C = A.and(C);
						// S = XOR_AND(A, C, C);
						res.bsi[i] = S;
					}
					if (C.cardinality() > 0) {
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
		
		//The next two for loops check for null slices within the result range and populates them with zeros
		int maxNotNull = 0;
		for(int i=0; i<res.bsi.length; i++){
			if(res.bsi[i]!=null)
				maxNotNull=i;
		}
		for(int i=0; i<maxNotNull; i++){
			if(res.bsi[i]==null){
				res.bsi[i]= zeroBitmap;
				//res.bsi[i].addStreamOfEmptyWords(false, this.bsi[0].sizeInBits()/this.bsi[0].wordinbits);				
			}
		}
		return res;
	}
	
	public BsiAttributeEWAH multiply_verbatim(int number) { // k is the offset
		// System.out.println("Multiply by "+number);
		BsiAttributeEWAH res = null;
		HybridBitmap C, S;
		int k = 0;
		while (number > 0) {
			if ((number & 1) == 1) {
				if (res == null) {
					res = new BsiAttributeEWAH();
					res.offset = k;
					for (int i = 0; i < this.size; i++) {
						res.bsi[i] = this.bsi[i];
					}
					res.size = this.size;
					k = 0;
				} else {
					/* Move the slices of res k positions */
					HybridBitmap A, B;
					A = res.bsi[k];
					B = this.bsi[0];
					// if (A==null || B==null) {
					// System.out.println("A or B is null");
					// }
					C = new HybridBitmap();
					S = A.xorV(B);
					C = A.andV(B);
					// S = XOR_AND(A, B, C);
					res.bsi[k] = S;
					// C = Sum[1];
					for (int i = 1; i < this.size; i++) {// Add the slices of
															// this to the
															// current res
						A = res.bsi[i + k];
						B = this.bsi[i];
						if (A == null) {
							S = B.xorV(C);
							C = B.andV(C);
							// S = XOR_AND(B, C, C);
							res.size++;
						} else {
							S = A.xorV(B).xorV(C);
							// S = XOR(A, B, C);
							C = A.andV(B).orV(B.andV(C)).orV(A.andV(C));
							// C = maj(A, B, C); // OR(OR(AND(A, B), AND(A, C)),
												// AND(C, B));
						}
						res.bsi[i + k] = S;
					}
					for (int i = this.size + k; i < res.size; i++) {// Add the
																	// remaining
																	// slices of
																	// res with
																	// the Carry
																	// C
						A = res.bsi[i];
						S = A.xorV(C);
						C = A.andV(C);
						// S = XOR_AND(A, C, C);
						res.bsi[i] = S;
					}
					if (C.cardinality() > 0) {
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
