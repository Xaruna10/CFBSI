package com.company.bsi;
/**
 * 
 * @author gguzun
 * This class defines a data attribute stored as BSI slices. 
 * The least significant bit/slice is slice 0
 */

import com.company.hybridewah.HybridBitmap;

import java.io.Serializable;

/**
 * 
 * @author Gheorghi Guzun
 */

public class BsiAttributeHybridExistence implements Serializable {


	/**
   * 
   */
	
	static int wordsNeeded = 0;
	int size;
	int offset = 0;
	public HybridBitmap[] bsi;
	HybridBitmap existenceBitmap = new HybridBitmap(2);
	long rows;
	int index;

	public BsiAttributeHybridExistence() {
		size = 0;
		bsi = new HybridBitmap[64];

	}
	
	
	public BsiAttributeHybridExistence(int maxSize) {
		size = 0;
		bsi = new HybridBitmap[maxSize];

	}
	/**
	 * 
	 * @param maxSize - maximum number of slices allowed for this attribute
	 * @param numOfRows - The number of rows (tuples) in the attribute
	 */
	public BsiAttributeHybridExistence(int maxSize, int numOfRows) {
		size = 0;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap.setSizeInBits(numOfRows, true);	
		if(existenceBitmap.sizeInBits()%64>0)
			existenceBitmap.setSizeInBits(existenceBitmap.sizeInBits()+64-existenceBitmap.sizeInBits()%64, false);
		existenceBitmap.density = (double)numOfRows/existenceBitmap.sizeInBits();
		this.rows = numOfRows;

	}
	
	/**
	 * 
	 * @param maxSize - maximum number of slices allowed for this attribute
	 * @param numOfRows - The number of rows (tuples) in the attribute
	 */
	public BsiAttributeHybridExistence(int maxSize, int numOfRows, int partitionID) {
		size = 0;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap.setSizeInBits(numOfRows, true);
		if(existenceBitmap.sizeInBits()%64>0)
			existenceBitmap.setSizeInBits(existenceBitmap.sizeInBits()+64-existenceBitmap.sizeInBits()%64, false);
		existenceBitmap.density = (double)numOfRows/existenceBitmap.sizeInBits();
		this.index=partitionID;
		this.rows = numOfRows;

	}
	

	/**
	 * 
	 * @param maxSize - maximum number of slices allowed for this attribute
	 * @param numOfRows - The number of rows (tuples) in the attribute
	 * @param partitionID - the id of the partition
	 * @param ex - existence bitmap
	 */
	public BsiAttributeHybridExistence(int maxSize, long numOfRows, int partitionID, HybridBitmap ex) {
		size = 0;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap = ex;
		this.index=partitionID;
		this.rows = numOfRows;

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
	public void setNumberOfSlices(int s) {
		this.size = s;
	}

	/**
	 * Returns the size of the bsi (how many slices are non zeros)
	 * 
	 * 
	 */
	public int getNumberOfSlices() {
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
	 * Returns the number of rows for this attribute
	 * 
	 * 
	 */
	public long getNumberOfRows() {
		return this.rows;
	}
	
	
	/**
	 * Sets the number of rows for this attribute
	 * 
	 * 
	 */
	public void setNumberOfRows(long rows) {
		this.rows=rows;
	}
	
	/**
	 * Returns the index(partition id if horizontally partitioned) for this attribute
	 * 
	 * 
	 */
	public int getPartitionID() {
		return this.index;
	}
	
	
	/**
	 * Sets the index(partition id if horizontally partitioned) for this attribute
	 * 
	 * 
	 */
	public void setPartitionID(int index) {
		this.index=index;
	}


	/**
	 * Returns the Existence bitmap of the bsi attribute
	 * 
	 * 
	 */
	public HybridBitmap getExistenceBitmap() {
		return this.existenceBitmap;
	}
	
	/**
	 * Sets the existence bitmap of the bsi attribute 
	 * 
	 * 
	 */
	public void setExistenceBitmap(HybridBitmap exBitmap) {
		this.existenceBitmap=exBitmap;
	}



	

	

	/**
	 * Computes the top-K tuples in a bsi-attribute.
	 * 
	 * @param k
	 *            - the number in top-k
	 * @return a bitArray containing the top-k tuples
	 */
//initiate E to the existence bitmap

	public HybridBitmap topKMax(int k) {
		HybridBitmap topK, SE, X;
		HybridBitmap G = new HybridBitmap();	
		G.addStreamOfEmptyWords(false, this.existenceBitmap.sizeInBits()/64);		
		HybridBitmap E = this.existenceBitmap;
		System.out.println("Existence Bitmap bits: "+ this.existenceBitmap.sizeInBits());
		System.out.println("First Slice bits: "+ this.bsi[0].sizeInBits());

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
	
	

	
	public BsiAttributeHybridExistence addSliceWithOffset(BsiAttributeHybridExistence a) {
		int wordsInSlice=this.bsi[0].actualsizeinwords;
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.addStreamOfEmptyWords(false,wordsInSlice);
		BsiAttributeHybridExistence base, slice;
		if (a.size==1){
			base=this;
			slice=a;
		}else{
			base=a;
			slice=this;
		}
		BsiAttributeHybridExistence res = new BsiAttributeHybridExistence();
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
	
	public BsiAttributeHybridExistence SUM_BSI_withOffset(BsiAttributeHybridExistence a) {
		int wordsInSlice=this.bsi[0].sizeInBits()/64;
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.addStreamOfEmptyWords(false,wordsInSlice);
	
		BsiAttributeHybridExistence res = new BsiAttributeHybridExistence();
		
		
		int i = 0, s = a.size, p = this.size;
		int aStart = 0, thisStart = 0;
		
	
		if (a.offset == this.offset) {
			// do the sum as normal
			res.offset = a.offset;
		} else if (a.offset > this.offset) {
			// start the sum at a[0] + res[a.offset], res_< a.offset remain unchanged
			int unchanged = a.offset - this.offset; // justAdd. slices can be just added from the result with the minimum offset
			for (i = 0; i < unchanged ; i++) {
				// str = Integer.toString(i);
				res.bsi[i] = this.bsi[i];
			}
			thisStart = i;
			p = (p - i)>0?(p-i):0;
			res.offset = this.offset;
			res.size = i;
		} else {// a.offset < this.offset
			// copy the slices into
			int appendFromA = this.offset - a.offset; // justAdd slices can be just added from the result with the minimum offset
			for (i = 0; i < appendFromA ; i++) {
				// str = Integer.toString(i);
				res.bsi[i] = a.bsi[i];
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
			S = A.xor(B).xor(C);
			
			// S = XOR(A, B, C);
			res.bsi[res.size + i] = S;
			// C = maj(A, B, C);
			
			C = A.and(B).or(B.and(C)).or(A.and(C));
			
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
		res.existenceBitmap = this.existenceBitmap;
		res.rows = this.rows;
		res.index = this.index;
		return res;
	}

	
	
	

	public BsiAttributeHybridExistence SUM_BSI(BsiAttributeHybridExistence a) {
		BsiAttributeHybridExistence res = new BsiAttributeHybridExistence();
		
		
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
		res.existenceBitmap = this.existenceBitmap.and(a.existenceBitmap);
		res.rows = this.rows;
		res.index = this.index;
		return res;
	}
	
	public BsiAttributeHybridExistence SUM_BSI_verbatim(BsiAttributeHybridExistence a) {
		BsiAttributeHybridExistence res = new BsiAttributeHybridExistence();
		
		
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
			S = A.xorV(B).xorV(C);
			
			// S = XOR(A, B, C);
			res.bsi[res.size + i] = S;
			// C = maj(A, B, C);
			
			C = A.andV(B).orV(B.andV(C)).orV(A.andV(C));
			
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
		res.existenceBitmap = this.existenceBitmap.and(a.existenceBitmap);
		res.rows = this.rows;
		res.index = this.index;
		return res;
	}

	public BsiAttributeHybridExistence multiply(int number) { // k is the offset
		// System.out.println("Multiply by "+number);
		BsiAttributeHybridExistence res = null;
		
		
		HybridBitmap C, S;
		int k = 0;
		while (number > 0) {
			if ((number & 1) == 1) {
				if (res == null) {
					res = new BsiAttributeHybridExistence();
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
						A= new HybridBitmap();
						A.addStreamOfEmptyWords(false, this.bsi[0].sizeInBits()/this.bsi[0].wordinbits);
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
				res.bsi[i]= new HybridBitmap();
				//res.bsi[i].setSizeInBits(this.bsi[0].sizeInBits(), false);
				res.bsi[i].addStreamOfEmptyWords(false, this.existenceBitmap.sizeInBits()/64);				
			}
		}
		res.existenceBitmap = this.existenceBitmap;
		res.rows = this.rows;
		res.index = this.index;
		return res;
	}
	
	public BsiAttributeHybridExistence multiply_verbatim(int number) { // k is the offset
		// System.out.println("Multiply by "+number);
		BsiAttributeHybridExistence res = null;
		HybridBitmap C, S;
		int k = 0;
		while (number > 0) {
			if ((number & 1) == 1) {
				if (res == null) {
					res = new BsiAttributeHybridExistence();
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
		res.existenceBitmap = this.existenceBitmap;
		res.rows = this.rows;
		res.index = this.index;
		return res;
	}


}

