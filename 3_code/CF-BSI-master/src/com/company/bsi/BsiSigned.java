package com.company.bsi;

import java.io.Serializable;

/**
 * 
 * @author gguzun
 * This class defines a signed data attribute stored as BSI slices. 
 * The least significant bit/slice is slice 0
 * 
 *  if two's complement, the last slice is the sign slice
 */
import com.company.hybridewah.HybridBitmap;;

public class BsiSigned extends BsiAttribute implements Serializable {
	
//	int size;
//	int offset = 0;
//	public HybridBitmap[] bsi;
//	public HybridBitmap existenceBitmap = new HybridBitmap(2);
//	long rows;
//	int index;

	
	
	
	/**
	 * 
	 */
	public BsiSigned() {
		size = 0;
		bsi = new HybridBitmap[32];
		signed=true;

	}
	/**
	 * 
	 * @param maxSize - including the sign bit when twos complement
	 */
	public BsiSigned(int maxSize) {
		size = 0;
		bsi = new HybridBitmap[maxSize];
		signed=true;

	}
	
	/**
	 * 
	 * @param maxSize - maximum number of slices allowed for this attribute (including the sign bit if twos complement)
	 * @param numOfRows - The number of rows (tuples) in the attribute
	 */
	public BsiSigned(int maxSize, int numOfRows) {
		size = 0;
		signed=true;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap.setSizeInBits(numOfRows, true);	
//		if(existenceBitmap.sizeInBits()%64>0)
//			existenceBitmap.setSizeInBits(existenceBitmap.sizeInBits()+64-existenceBitmap.sizeInBits()%64, false);
//		existenceBitmap.density = (double)numOfRows/(existenceBitmap.sizeInBits()+64-existenceBitmap.sizeInBits()%64);
		existenceBitmap.density=1;
		this.rows = numOfRows;

	}
	
	/**
	 * 
	 * @param maxSize - maximum number of slices allowed for this attribute
	 * @param numOfRows - The number of rows (tuples) in the attribute
	 */
	public BsiSigned(int maxSize, int numOfRows, long partitionID) {
		size = 0;
		signed=true;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap.setSizeInBits(numOfRows, true);	
//		if(existenceBitmap.sizeInBits()%64>0)
//			existenceBitmap.setSizeInBits(existenceBitmap.sizeInBits()+64-existenceBitmap.sizeInBits()%64, false);
//		existenceBitmap.density = (double)numOfRows/(existenceBitmap.sizeInBits()+64-existenceBitmap.sizeInBits()%64);
		existenceBitmap.density=1;
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
	
	public BsiSigned(int maxSize, long numOfRows, long partitionID, HybridBitmap ex) {
		size = 0;
		signed=true;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap = ex;
		this.index=partitionID;
		this.rows = numOfRows;

	}
	

	

		


	
	
	// Need to handle the case when all values are negative, or when some negative values should be in the topK
	public HybridBitmap topKMax(int k) {

		
		if(this.twosComplement){
			this.twosToSignMagnitude();
		} 
		HybridBitmap topK, SE, X;
		HybridBitmap G = new HybridBitmap();	
		G.addStreamOfEmptyWords(false, this.existenceBitmap.sizeInBits()/64);		
		HybridBitmap E = this.existenceBitmap.andNot(this.sign); //considers only positive values
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
		if(n<k){
			//todo add negative numbers here (topKMin abs)
		}
		n = G.cardinality();
		topK = G.or(E);
		// topK = OR(G, E.first(k - n+ 1));

		return topK;
	}
	
	
	public void twosToSignMagnitude(){		
		
		
		
	}
	
	
	
	
	public BsiSigned convertToTwos(int bits){
		BsiSigned res = new BsiSigned(bits);
		
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.addStreamOfEmptyWords(false,this.existenceBitmap.actualsizeinwords);
		int i=0;
		for(i=0; i<this.getNumberOfSlices(); i++){
			res.addSlice(this.bsi[i].xor(this.sign));
		}
		while(i<bits){
			res.addSlice(this.sign);
			i++;
		}
		res.addSliceWithOffset(this.sign,0);
		res.setTwosFlag(true);
		
			
		return res;
	}

	
	
	/**
	 * This adds(summation) a slice to a BSI attribute
	 */
	public void addSliceWithOffset(HybridBitmap slice, int sliceOffset) {
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		
		HybridBitmap A = this.bsi[sliceOffset-this.offset];
		HybridBitmap C = new HybridBitmap(),S;
		
		S=A.xor(slice);
		C=A.and(slice);
		
		this.bsi[sliceOffset-this.offset]=S;
		int curPos = sliceOffset-this.offset+1;
		
		while(C.cardinality()>0){
			if(curPos<this.size){
				A=this.bsi[curPos];
				S=C.xor(A);
				C=C.and(A);
				this.bsi[curPos]=S;
				curPos++;
			}else{
				this.addSlice(C);
			}
		}
	}
	
	
	
	public BsiAttribute SUMunsigned(BsiAttribute a){	
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res = new BsiSigned();
		res.twosComplement=true;
		res.setPartitionID(a.getPartitionID());
		if(!this.twosComplement)
			this.signMagnitudeToTwos(this.size+1);
		
		int i = 0, s = a.size, p = this.size, aIndex=0, thisIndex=0;
		int minOffset = Math.min(a.offset, this.offset);
		res.offset = minOffset;
		
		if(a.offset>this.offset){
			for(int j=0;j<a.offset-minOffset; j++){
				if(j<this.size)
				res.bsi[res.size]=this.bsi[thisIndex];
				else if(this.lastSlice)
					res.bsi[res.size]=this.sign; //sign extend if contains the sign slice
				else
					res.bsi[res.size]=zeroBitmap;
				thisIndex++;
				res.size++;
			}
		}else if(this.offset>a.offset){
			for(int j=0;j<this.offset-minOffset;j++){
				if(j<a.size)
				res.bsi[res.size]=a.bsi[aIndex];
				else
					res.bsi[res.size]=zeroBitmap;
				res.size++;
				aIndex++;
			}
		}
		//adjust the remaining sizes for s and p
				s=s-aIndex;
				p=p-thisIndex;
				int minSP = Math.min(s, p);
				
				if(minSP<=0){ // one of the BSI attributes is exausted
					for(int j=thisIndex; j<this.size;j++){
						res.bsi[res.size]=this.bsi[j];
						res.size++;
					}
					HybridBitmap CC = null;
					for(int j=aIndex; j<a.size;j++){ 
						if(this.lastSlice){ // operate with the sign slice if contains the last slice
							if(j==aIndex){
								res.bsi[res.size]=a.bsi[j].xor(this.sign);
								CC=a.bsi[j].and(this.sign);
								res.lastSlice=true;
							}else{
								res.bsi[res.size]=XOR(a.bsi[j],this.sign,CC);
								CC=maj(a.bsi[j],this.sign,CC);
							}
							res.size++;
						}else{				
						res.bsi[res.size]=a.bsi[j];
						res.size++;}
					}			
					res.lastSlice=this.lastSlice;
					res.firstSlice=this.firstSlice|a.firstSlice;
					res.existenceBitmap = a.existenceBitmap.or(this.existenceBitmap);
					res.sign = res.bsi[res.size-1];
					return res;
				}else {					
					res.bsi[res.size] = a.bsi[aIndex].xor(this.bsi[thisIndex]);
					HybridBitmap C = a.bsi[aIndex].and(this.bsi[thisIndex]);
					res.size++;
					thisIndex++;
					aIndex++;
					
					for(i=1; i<minSP; i++){
						//res.bsi[i] = this.bsi[i].xor(a.bsi[i].xor(C));
						res.bsi[res.size] = XOR(a.bsi[aIndex], this.bsi[thisIndex], C);
						//res.bsi[i] = this.bsi[i].xor(this.bsi[i], a.bsi[i], C);
						C= maj(a.bsi[aIndex], this.bsi[thisIndex], C);			
						res.size++;
						thisIndex++;
						aIndex++;
					}
					
					if(s>p){
						for(i=p; i<s;i++){
							res.bsi[res.size] = this.bsi[thisIndex].xor(C);
							C=this.bsi[thisIndex].and(C);
							res.size++;
							thisIndex++;
						}
					}else{
						for(i=s; i<p;i++){
							if(this.lastSlice){
								res.bsi[res.size] = XOR(a.bsi[aIndex], this.sign, C);
								C = maj(a.bsi[aIndex], this.sign, C);
								res.size++;
								aIndex++;}
							else{					
								res.bsi[res.size] = a.bsi[aIndex].xor(C);
								C = a.bsi[aIndex].and(C);
								res.size++;
								aIndex++;}
						}
					}
					if(!this.lastSlice && C.cardinality()>0){
						res.bsi[res.size]= C;
						res.size++;
					}		
					
					res.lastSlice=this.lastSlice;
					res.firstSlice=this.firstSlice|a.firstSlice;
					res.existenceBitmap = a.existenceBitmap.or(this.existenceBitmap);
					res.sign = res.bsi[res.size-1];					
					return res;
				}		
	}

	public BsiAttribute SUMsigned(BsiAttribute a){
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res = new BsiSigned();
		res.twosComplement=true;
		res.setPartitionID(a.getPartitionID());
		
		if (!a.twosComplement)
			a.signMagnitudeToTwos(a.size+1); //plus one for the sign
		if (!this.twosComplement)
			this.signMagnitudeToTwos(this.size+1); //plus one for the sign
		
		int i = 0, s = a.size, p = this.size, aIndex=0, thisIndex=0;
		int minOffset = Math.min(a.offset, this.offset);
		res.offset = minOffset;
		
		if(this.offset>a.offset){
			for(int j=0;j<this.offset-minOffset; j++){
				if(j<a.size)
				res.bsi[res.size]=a.bsi[aIndex];
				else if(a.lastSlice)
					res.bsi[res.size]=a.sign; //sign extend if contains the sign slice
				else
					res.bsi[res.size]=zeroBitmap;
				aIndex++;
				res.size++;
			}
		}else if(a.offset>this.offset){
			for(int j=0;j<a.offset-minOffset;j++){
				if(j<this.size)
				res.bsi[res.size]=this.bsi[thisIndex];
				else if(this.lastSlice)
					res.bsi[res.size]=this.sign;
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

				if(minSP<=0){ // one of the BSI attributes is exausted
					HybridBitmap CC = null;
					for(int j=aIndex; j<a.size;j++){
						if(this.lastSlice){ // operate with the sign slice if contains the last slice
							if(j==aIndex){
								res.bsi[res.size]=a.bsi[j].xor(this.sign);
								CC=a.bsi[j].and(this.sign);
								res.lastSlice=true;
							}else{
								res.bsi[res.size]=XOR(a.bsi[j],this.sign,CC);
								CC=maj(a.bsi[j],this.sign,CC);
							}
							res.size++;
						}else{
						res.bsi[res.size]=a.bsi[j];
						res.size++;}
					}
					CC = null;
					for(int j=thisIndex; j<this.size;j++){ 
						if(a.lastSlice){ // operate with the sign slice if contains the last slice
							if(j==thisIndex){
								res.bsi[res.size]=this.bsi[j].xor(a.sign);
								CC=this.bsi[j].and(a.sign);
								res.lastSlice=true;
							}else{
								res.bsi[res.size]=XOR(this.bsi[j],a.sign,CC);
								CC=maj(this.bsi[j],a.sign,CC);
							}
							res.size++;
						}else{				
						res.bsi[res.size]=this.bsi[j];
						res.size++;}
					}				
					
					res.lastSlice=this.lastSlice;
					res.firstSlice=this.firstSlice|a.firstSlice;
					res.existenceBitmap = a.existenceBitmap.or(this.existenceBitmap);
					res.sign = res.bsi[res.size-1];
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
							if(this.lastSlice){
								res.bsi[res.size] = XOR(a.bsi[aIndex], this.sign, C);
								C = maj(a.bsi[aIndex], this.sign, C);
								res.size++;
								aIndex++;}
							res.bsi[res.size] = a.bsi[aIndex].xor(C);
							C=a.bsi[aIndex].and(C);
							res.size++;
							aIndex++;
						}
					}else{
						for(i=s; i<p;i++){
							if(a.lastSlice){
								res.bsi[res.size] = XOR(this.bsi[thisIndex], a.sign, C);
								C = maj(this.bsi[thisIndex], a.sign, C);
								res.size++;
								thisIndex++;}
							else{					
								res.bsi[res.size] = this.bsi[thisIndex].xor(C);
								C = this.bsi[thisIndex].and(C);
								res.size++;
								thisIndex++;}
						}
					}
					if(!this.lastSlice&&!a.lastSlice && C.cardinality()>0){
						res.bsi[res.size]= C;
						res.size++;
					}		
					res.sign = res.bsi[res.size-1];
					res.existenceBitmap = this.existenceBitmap.or(a.existenceBitmap);	
					res.lastSlice=a.lastSlice;					
					res.firstSlice=this.firstSlice|a.firstSlice;
					return res;
				}		
	}
	
	
	public BsiAttribute SUM(long a){
		if (a==0){
			return this;
		}else{
		int intSize = Long.toBinaryString(Math.abs(a)).length()+1; // plus one to allow for carry if positive
		//if(a<0) intSize++;
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res=new BsiSigned(Math.max(this.size, intSize)+1);
		res.twosComplement=true;
		HybridBitmap C;
			int minSP = Math.min(this.size, intSize);
			HybridBitmap allOnes = new HybridBitmap();
			allOnes.setSizeInBits(this.bsi[0].sizeInBits(), true);
			allOnes.density=1;
			if ((a&1)==0){
				res.bsi[0]=this.bsi[0];
				C = zeroBitmap;
				}
			else{
				res.bsi[0]=this.bsi[0].NOT();
				C=this.bsi[0];
			}
			res.size++;
			int i;
			for(i=1;i<this.size;i++){
				if((a&(1<<i))!=0){
					res.bsi[i]=C.xorNot(this.bsi[i]);
					//res.bsi[i] = C.xor(this.bsi[i].NOT());
					C=this.bsi[i].or(C);
				}else{
					res.bsi[i]=this.bsi[i].xor(C);
					C=this.bsi[i].and(C);
				}
				res.size++;
			}
			//long cCard = C.cardinality();
			if(intSize>this.size){
				while (i<intSize){
						if((a&(1<<i))!=0){
							res.bsi[i]=C.xorNot(this.bsi[this.size-1]);
							C=this.bsi[this.size-1].or(C);
						}else{
							res.bsi[i]=C.xor(this.bsi[this.size-1]);
							C=this.bsi[this.size-1].and(C);							
						}
					
					res.size++;
					i++;
				}
			}
			if(this.lastSlice && C.cardinality()>0 ){
			if(a>0){
				//res.addSlice(C.andNot(this.bsi[this.size-1]));
				res.addSlice(this.sign.andNot(C));				
				//System.out.println(res.size);
			}else{
				res.addSlice(XOR(C,allOnes,this.sign));
				//res.addSlice(C.xor(allOnes).xor(this.sign));
				//res.addSlice(C.and(this.sign));
			//res.addSlice(this.sign.xor(C).andNot(C));
			}
			}else{
				res.addSlice(C);
			}
		res.sign=res.bsi[res.size-1];
		res.firstSlice=this.firstSlice;
		res.lastSlice=this.lastSlice;
		res.existenceBitmap = this.existenceBitmap;
		return res;	
		}
	}
	
	@Override
	public BsiAttribute SUM(BsiAttribute a) {
		if (a.signed){
			return this.SUMsigned(a);
		}else{
			return this.SUMunsigned(a);
		}		
		
	}


public HybridBitmap topKMin(int k){
	HybridBitmap topK, SE, X;
//	EWAHCompressedBitmap G = topPrefEWAH.bitmapindex.getBitmap("B0");
//	EWAHCompressedBitmap E = topPrefEWAH.bitmapindex.getBitmap("B1");
	topK=new HybridBitmap();
//	HybridBitmap G = new HybridBitmap();
//	G.setSizeInBits(this.bsi[0].sizeInBits(),false);
//	HybridBitmap E = new HybridBitmap();	
//	E.setSizeInBits(this.bsi[0].sizeInBits(),true);
//	E.density=1;
//	
//	int n = 0;
//
//	for (int i = this.size - 1; i >= 0; i--) {
//		SE = E.and(this.bsi[i]);
//		X = G.or(SE);
//		n = X.cardinality();
//
//		if (n > k) {
//			E = SE;
//		}
//		if (n < k) {
//			G = X;
//			E = E.andNot(this.bsi[i]);
//
//		}
//		if (n == k) {
//			E = SE;
//			break;
//		}
//	}
//	n = G.cardinality();
//	topK = G.or(E);
	// topK = OR(G, E.first(k - n+ 1));

	return topK;

	
}


@Override
public BsiUnsigned abs() {
//	HybridBitmap zeroBitmap = new HybridBitmap();
//	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
	BsiUnsigned res = new BsiUnsigned(this.size);
	

	if(this.twosComplement){
		for (int i=0; i<this.size-1; i++){
			res.bsi[i]=this.bsi[i].xor(this.sign);
			res.size++;
		}
		//res.size=this.size;
		if(this.firstSlice){			
			res.addOneSliceSameOffset(this.sign);
		}
	}else{
		res.bsi=this.bsi;
		res.size=this.size;
	}
	res.existenceBitmap=this.existenceBitmap;
	res.setPartitionID(this.getPartitionID());
	res.firstSlice=this.firstSlice;
	res.lastSlice=this.lastSlice;
	return res;
}


@Override
public BsiUnsigned absScale(double range) {
//	HybridBitmap zeroBitmap = new HybridBitmap();
//	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
	
	HybridBitmap penalty = this.bsi[this.size-2].xor(this.sign);

	int resSize=0;
	for (int i=this.size-2;i>=0;i--){
		penalty=penalty.or(this.bsi[i].xor(this.sign));
		if(penalty.cardinality()>=(this.bsi[0].sizeInBits()*range)){
		//if(penalty.density>=0.9){
		//if(i==this.size-8){
			resSize=i;
			break;
		}
	}
		
	//BsiUnsigned res = new BsiUnsigned(resSize+2);
	BsiUnsigned res = new BsiUnsigned(2);
	
	
//	if(this.twosComplement){
//		for (int i=0; i<resSize; i++){
//			res.bsi[i]=this.bsi[i].xor(this.sign);
//			res.size++;
//			
//				
//		}
//		res.addSlice(penalty);
//		//res.size=this.size;
//		if(this.firstSlice){			
//			res.addOneSliceSameOffset(this.sign);
//		}
//	}else{
//		res.bsi=this.bsi;
//		res.size=this.size;
//	}
	res.addSlice(penalty);
	res.existenceBitmap=this.existenceBitmap;
	res.setPartitionID(this.getPartitionID());
	res.firstSlice=this.firstSlice;
	res.lastSlice=this.lastSlice;
	return res;
}



//@Override
//public BsiUnsigned abs() {
////	HybridBitmap zeroBitmap = new HybridBitmap();
////	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
//	BsiUnsigned res = new BsiUnsigned(this.size);
//	BsiUnsigned res1 = new BsiUnsigned(1);
//
//	if(this.twosComplement){
//		for (int i=0; i<this.size-1; i++){
//			res.bsi[i]=this.bsi[i].xor(this.sign);
//			res.size++;
//		}
//		//res.size=this.size;
//		if(this.firstSlice){			
//			res.addOneSliceSameOffset(this.sign);
//		}
//		HybridBitmap penalty = res.bsi[res.size-1];
//		int i=res.size-2;
//		while(penalty.cardinality()<0.9*res.bsi[0].sizeInBits()){
//			if(i<0)
//				break;
//			penalty=penalty.or(res.bsi[i]);
//			i--;
//		}
//		res1.addSlice(penalty);
//		
//	}else{
//		res.bsi=this.bsi;
//		res.size=this.size;
//	}
//	
//	res1.existenceBitmap=this.existenceBitmap;
//	res1.setPartitionID(this.getPartitionID());
//	res1.firstSlice=this.firstSlice;
//	res1.lastSlice=this.lastSlice;
//	return res1;
//}

public BsiUnsigned abs(int resultSlices, HybridBitmap EB) { //number of slices allocated for the result; Existence bitmap
//	HybridBitmap zeroBitmap = new HybridBitmap();
//	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);	
	int min = Math.min(this.size-1, resultSlices);
	BsiUnsigned res = new BsiUnsigned(min+1);

	if(this.twosComplement){
		for (int i=0; i<min; i++){
			res.bsi[i]=this.bsi[i].xor(this.sign);
					}
		res.size=min;
		if(this.firstSlice){			
			res.addOneSliceDiscardCarry(this.sign);
		}
	}else{
		for(int i=0;i<min; i++){
			res.bsi[i]=this.bsi[i];			
		}
		res.size=min;
	}
	res.addSlice(EB.NOT()); // this is for KNN to add one slice
	res.existenceBitmap=this.existenceBitmap;
	res.setPartitionID(this.getPartitionID());
	res.firstSlice=this.firstSlice;
	res.lastSlice=this.lastSlice;
	return res;
}



@Override
public long getValue(int pos) {
	if(this.twosComplement){
		boolean sign = this.bsi[this.size-1].getBit(pos);
		long sum=0; 
		HybridBitmap B_i;
		for (int i = 0; i < size-1; i++) {
			B_i = bsi[i];
			if(B_i.getBit(pos)^sign) 
			sum =sum|( 1<<(offset + i));
		}
		
		return (sum+((sign)?1:0))*((sign)?-1:1);
	}else{
		long sign = (this.sign.getBit(pos))?-1:1;
		long sum = 0;
		HybridBitmap B_i;
		
		
		for (int i = 0; i < size; i++) {
			B_i = bsi[i];
			if(B_i.getBit(pos)) 
			sum += 1<<(offset + i);
		}
		
		return sum*sign;
	}
	
}
@Override
public HybridBitmap rangeBetween(long lowerBound, long upperBound) {
	// TODO Auto-generated method stub
	return null;
}


}
