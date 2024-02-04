package com.company.bsi;

import com.company.hybridewah.HybridBitmap;

import java.io.Serializable;

import com.company.org.roaringbitmap.RoaringBitmap;

/**
 * 
 * @author Guzun
 */

public class BsiAttributeRoaring implements Serializable {

	/**
   * 
   */
	//private static final long serialVersionUID = -6380203498543662511L;
	static int wordsNeeded = 0;
	int size;
	int offset = 0;
	public RoaringBitmap[] bsi;

	public BsiAttributeRoaring() {
		size = 0;
		bsi = new RoaringBitmap[45];

	}

	/**
	 * 
	 * Adds new slice to the BSI array. Increases the array size if necessary.
	 * (gguzun)
	 * 
	 * @param slice
	 * 
	 */
	public void add(RoaringBitmap slice) {
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
	 * Adds otherBSI to this BsiAttribute. Saves the result in this BsiAttribute
	 * 
	 * @param otherBSI
	 * @param offset
	 */
	
	public BsiAttributeRoaring SUM_BSI(BsiAttributeRoaring a) {
		BsiAttributeRoaring res = new BsiAttributeRoaring();
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

		RoaringBitmap A, B;
		RoaringBitmap C = new RoaringBitmap(), S;
		A = a.bsi[aStart];
		B = this.bsi[thisStart];
//		if (A == null || B == null) {
//			System.out.println("A or B is null");
//		}
		//S = A.xor(B);
		S=RoaringBitmap.xor(A, B);
		C=RoaringBitmap.and(A, B);
		//C = A.and(B);
		

		// S = XOR_AND(A, B, C);
		res.bsi[res.size] = S;
		for (i = 1; i < r; i++) {
			A = a.bsi[aStart + i];
			B = this.bsi[thisStart + i];
			//S = A.xor(B).xor(C);
			S=RoaringBitmap.xor(A, B);
			S.xor(C);
			// S = XOR(A, B, C);
			res.bsi[res.size + i] = S;
			// C = maj(A, B, C);
			RoaringBitmap temp = RoaringBitmap.and(A, B);
			temp.or(RoaringBitmap.and(B, C));
			temp.or(RoaringBitmap.and(A, C));
			C=temp;
			//C = A.and(B).or(B.and(C)).or(A.and(C));
			
		}
		if (s > p) {
			for (i = p; i < s; i++) {
				A = a.bsi[aStart + i];
				//S = A.xor(C);
				S=RoaringBitmap.xor(A, C);
				
				
				//C = A.and(C);
				//C=RoaringBitmap.and(A, C);
				C.and(A);
				// S = XOR_AND(A, C, C);
				res.bsi[res.size + i] = S;

			}
		} else {
			for (i = s; i < p; i++) {
				B = this.bsi[thisStart + i];
				//S = B.xor(C);
				S=RoaringBitmap.xor(B, C);
				
				//C = B.and(C);
				C.and(B);
				//C=RoaringBitmap.and(B, C);
				// S = XOR_AND(B, C, C);
				res.bsi[res.size + i] = S;
			}
		}
		if (C.getCardinality() > 0) {
			res.bsi[res.size + q] = C; // Carry bit
			q++;
		}
		res.size = res.size + q;
		return res;
	}
	
	
	public RoaringBitmap topKMax(int k, int total) {
		RoaringBitmap topK, SE, X;
//		EWAHCompressedBitmap G = topPrefEWAH.bitmapindex.getBitmap("B0");
//		EWAHCompressedBitmap E = topPrefEWAH.bitmapindex.getBitmap("B1");
		
		RoaringBitmap G = new RoaringBitmap();
		RoaringBitmap E = new RoaringBitmap();
		for(int i=0; i<total; i++){
			E.add(i);
		}
		//G.addStreamOfEmptyWords(false, this.bsi[0].sizeInBits()/this.bsi[0].wordinbits);
		//EWAHCompressedBitmap E = new EWAHCompressedBitmap();		
//		E.addStreamOfEmptyWords(true, this.bsi[0].sizeInBits()/this.bsi[0].wordinbits);
//		E.density=1;
		
		int n = 0;

		for (int i = this.size - 1; i >= 0; i--) {
			//SE = E.and(this.bsi[i]);
			SE = RoaringBitmap.and(E, this.bsi[i]);
			//X = G.or(SE);
			X=RoaringBitmap.or(G, SE);
			n = X.getCardinality();

			if (n > k) {
				E = SE;
			}
			if (n < k) {
				G = X;
				E.andNot(this.bsi[i]);
				//E = E.andNot(this.bsi[i]);

			}
			if (n == k) {
				E = SE;
				break;
			}
		}
		n = G.getCardinality();
		topK=RoaringBitmap.or(G, E);
		//topK = G.or(E);
		// topK = OR(G, E.first(k - n+ 1));

		return topK;
	}
	
	
	
}