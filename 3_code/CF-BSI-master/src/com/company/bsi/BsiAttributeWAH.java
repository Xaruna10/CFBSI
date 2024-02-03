package com.company.bsi;
import com.company.hybridewah.HybridBitmap;

import java.io.Serializable;

import com.company.topK.BitmapIndex;
import com.company.topK.bitArrayWAH;

/**
 * 
 * @author Gheorghi Guzun
 */

public class BsiAttributeWAH implements Serializable {

	/**
   * 
   */
	//private static final long serialVersionUID = -6380203498543662511L;
	static int wordsNeeded = 0;
	int size;
	int offset = 0;
	public bitArrayWAH[] bsi;

	public BsiAttributeWAH() {
		size = 0;
		bsi = new bitArrayWAH[45];

	}

	/**
	 * 
	 * Adds new slice to the BSI array. Increases the array size if necessary.
	 * (gguzun)
	 * 
	 * @param slice
	 * 
	 */
	public void add(bitArrayWAH slice) {
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
	 * Computes the top-K tuples in a bsi-attribute.
	 * 
	 * @param k
	 *            - the number in top-k
	 * @return a bitArray containing the top-k tuples
	 */


	public bitArrayWAH topKMax(int k) {
		bitArrayWAH topK, SE, X;
//		EWAHCompressedBitmap G = topPrefEWAH.bitmapindex.getBitmap("B0");
//		EWAHCompressedBitmap E = topPrefEWAH.bitmapindex.getBitmap("B1");
		
		bitArrayWAH G = new bitArrayWAH(this.bsi[0].vec.length);
		G.appendFill(this.bsi[0].sizeinbits/63, 0L);
		bitArrayWAH E = new bitArrayWAH(this.bsi[0].vec.length);
		E.appendFill(this.bsi[0].sizeinbits/63, 0x7FFFFFFFFFFFFFFFL);	
		
		
		int n = 0;

		for (int i = this.size - 1; i >= 0; i--) {
			SE = E.and(this.bsi[i]);
			X = G.or(SE);
			n = X.getMatches();

			if (n > k) {
				E = SE;
			}
			if (n < k) {
				G = X;
				E = E.and(this.bsi[i].not());

			}
			if (n == k) {
				E = SE;
				break;
			}
		}
		n = G.getMatches();
		topK = G.or(E);
		// topK = OR(G, E.first(k - n+ 1));

		return topK;
	}
	
	

		
	public long SUM() {
		long sum = 0;
		bitArrayWAH B_i;
		for (int i = 0; i < size; i++) {
			B_i = bsi[i];
			sum += BitmapIndex.power2[offset + i] * B_i.getMatches();
		}
		return sum;
	}

	public BsiAttributeWAH SUM_BSI_inPlace(BsiAttributeWAH a) {
		BsiAttributeWAH res = new BsiAttributeWAH();
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

		bitArrayWAH A, B;
		bitArrayWAH C = new bitArrayWAH(this.bsi[0].vec.length), S;
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
		if (C.getMatches() > 0) {
			res.bsi[res.size + q] = C; // Carry bit
			q++;
		}
		res.size = res.size + q;
		return res;
	}
	
	

	public BsiAttributeWAH multiply_inPlace(int number) { // k is the offset
		// System.out.println("Multiply by "+number);
		BsiAttributeWAH res = null;
		
		bitArrayWAH C, S;
		int k = 0;
		while (number > 0) {
			if ((number & 1) == 1) {
				if (res == null) {
					res = new BsiAttributeWAH();
					res.offset = k;
					for (int i = 0; i < this.size; i++) {
						res.bsi[i] = this.bsi[i];
					}					
					res.size = this.size;
					k = 0;
				} else {
					/* Move the slices of res k positions */
					bitArrayWAH A, B;
					A = res.bsi[k];
					B = this.bsi[0];
					if(A==null){ 
						A= new bitArrayWAH(this.bsi[0].vec.length);getClass();
						A.appendFill(this.bsi[0].sizeinbits/63, 0L);						
						res.size=k+1;
					}
					// if (A==null || B==null) {
					// System.out.println("A or B is null");
					// }
					C = new bitArrayWAH(this.bsi[0].vec.length);
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
					if (C.getMatches() > 0) {
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
				res.bsi[i]= new bitArrayWAH(this.bsi[0].vec.length);
				res.bsi[i].appendFill(this.bsi[0].sizeinbits/63, 0L);				
			}
		}
		return res;
	}
	
	

}
