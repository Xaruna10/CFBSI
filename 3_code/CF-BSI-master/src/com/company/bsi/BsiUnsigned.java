package com.company.bsi;

import com.company.hybridewah.HybridBitmap;

import java.io.Serializable;

/**
 * @author gguzun
 * This class defines a signed data attribute stored as BSI slices.
 * The least significant bit/slice is slice 0
 */

public class BsiUnsigned extends BsiAttribute implements Serializable {
	

	
	public BsiUnsigned() {
		size = 0;
		bsi = new HybridBitmap[32];

	}
	
	
	public BsiUnsigned(int maxSize) {
		size = 0;
		bsi = new HybridBitmap[maxSize];

	}
	
	/**
	 * 
	 * @param maxSize - maximum number of slices allowed for this attribute
	 * @param numOfRows - The number of rows (tuples) in the attribute
	 */
	public BsiUnsigned(int maxSize, int numOfRows) {
		size = 0;
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
	public BsiUnsigned(int maxSize, int numOfRows, long partitionID) {
		size = 0;
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
	
	public BsiUnsigned(int maxSize, long numOfRows, long partitionID, HybridBitmap ex) {
		size = 0;
		bsi = new HybridBitmap[maxSize];
		existenceBitmap = ex;
		this.index=partitionID;

		this.rows = numOfRows;
	}
	
	@Override
	public HybridBitmap getExistenceBitmap() {
		return this.existenceBitmap;
	}
	
	@Override
	public void setExistenceBitmap(HybridBitmap exBitmap) {
		this.existenceBitmap=exBitmap;
	}

	
	
	
	
	
	/**
	 * 
	 * @param a
	 * @return res
	 */
	public BsiAttribute SUMsigned(BsiAttribute a){
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res = new BsiSigned(Math.max((this.size+this.offset), (a.size+a.offset))+2);
		res.twosComplement=true;
		res.index = (this.index);
		res.existenceBitmap = this.existenceBitmap.or(a.existenceBitmap);
		if (!a.twosComplement)
			a.signMagnitudeToTwos(a.size+1); //plus one for the sign
		
		int i = 0, s = a.size, p = this.size;
		int minOffset = Math.min(a.offset, this.offset);
		res.offset = minOffset;
		
		int aIndex = 0;
		int thisIndex =0;
		
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
			for(int j=aIndex; j<a.size;j++){
				res.bsi[res.size]=a.bsi[j];
				res.size++;
			}
			HybridBitmap CC = null;
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
			
			//res.existenceBitmap = this.existenceBitmap.or(a.existenceBitmap);
			res.sign = res.bsi[res.size-1];
			res.lastSlice=a.lastSlice;
			res.firstSlice=this.firstSlice|a.firstSlice;
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
			
			if(s>p){ //a has more bits (the two's complement)
				for(i=p; i<s;i++){
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
				if(this.lastSlice){
				res.bsi[res.size]= C.xor(a.sign);
				C=C.and(a.sign); //
				res.size++;}
			}
			if(!a.lastSlice && C.cardinality()>0){
			//if(!a.lastSlice){
				res.bsi[res.size]= C;
				res.size++;
			}		
			
				
			res.lastSlice=a.lastSlice;
			res.firstSlice=this.firstSlice|a.firstSlice;
			res.sign = res.bsi[res.size-1];
			return res;
		}

	}

	

	
	public BsiAttribute SUMunsigned(BsiAttribute a) {
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res = new BsiUnsigned(Math.max(this.size+this.offset, a.size+a.offset)+1);
		res.setPartitionID(a.getPartitionID());
		res.existenceBitmap = this.existenceBitmap.or(a.existenceBitmap);
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
		
		if(minSP<=0){ // one of the BSI attributes is exausted
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
			//if(!(this.lastSlice && a.lastSlice) && (C.cardinality()>0)){
			if(C.cardinality()>0){
				res.bsi[res.size]= C;
				res.size++;
			}		
			
			
			return res;
		}
			
	}
	/**
	 * 
	 */
	public BsiAttribute SUM(long a){
		if (a==0){
			return this;
		}else{
		int intSize = Long.toBinaryString(Math.abs(a)).length();
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res;		
		HybridBitmap C;
		if(a<0){
			//int minSP = Math.min(this.size, (intSize+1));
			res = new BsiSigned(Math.max(this.size, (intSize+1))+1);
			res.twosComplement=true;
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
			for( i=1; i<this.size; i++ ){
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
			if((intSize+1)>this.size){
			while(i<(intSize+1)){
				if((a&(1<<i))!=0){
					res.bsi[i]=C.NOT();										
					//C=this.bsi[i].or(C);
				}else{
					res.bsi[i]=C;
					C=zeroBitmap;					
				}
				i++;
				res.size++;
			}}else{
				res.addSlice(C.NOT());
			}
		//	if(C.cardinality()!=0){
		//	res.bsi[res.size]=C;
		//res.size++;}
			res.sign=res.bsi[res.size-1];			
		}else{
			int minSP = Math.min(this.size, intSize);
			res = new BsiUnsigned(Math.max(this.size, intSize)+1);
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
			for(i=1;i<minSP;i++){
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
			long cCard = C.cardinality();
			if(this.size>minSP){
				while(i<this.size){
					if(cCard>0){
						res.bsi[i]=this.bsi[i].xor(C);
						C=this.bsi[i].and(C);
						cCard=C.cardinality();
					}else{
					res.bsi[i]=this.bsi[i];}
					res.size++;
					i++;
				}
			}else{
				while (i<intSize){
					if(cCard>0){
						if((a&(1<<i))!=0){
							res.bsi[i]=C.NOT();							
						}else{
							res.bsi[i]=C;
							C=zeroBitmap;
							cCard=0;
						}

					}else{
						if((a&(1<<i))!=0){res.bsi[i]=allOnes;
						}else {res.bsi[i]=zeroBitmap;}

					}
					res.size++;
					i++;
				}
			}
			if(cCard>0){
				res.bsi[i]=C;
				res.size++;
			}
			
		}
		res.firstSlice=this.firstSlice;
		res.lastSlice=this.lastSlice;
		res.existenceBitmap = this.existenceBitmap;
		return res;	
		}
	}

	
	/**
	 * 
	 */
	public BsiAttribute SUM(long a, HybridBitmap EB, int rangeSlices){
		if (a==0){
			return this;
		}else{
		int intSize = Math.min(Long.toBinaryString(Math.abs(a)).length(), rangeSlices);
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
		BsiAttribute res;		
		HybridBitmap C;
		if(a<0){
			//int minSP = Math.min(this.size, (intSize+1));
			res = new BsiUnsigned(intSize+1);
			//res.twosComplement=true;
			if ((a&1)==0){
				res.bsi[0]=this.bsi[0].and(EB);
				C = zeroBitmap;
				}
			else{
				res.bsi[0]=EB.andNot(this.bsi[0]);
				C=this.bsi[0].and(EB);
			}
			res.size++;
			int i;
			for( i=1; i<intSize; i++ ){
				if((a&(1<<i))!=0){
					res.bsi[i]=C.xorNot(EB.and(this.bsi[i]));
					//res.bsi[i] = C.xor(this.bsi[i].NOT());
					C=EB.and(this.bsi[i]).or(C);
				}else{
					res.bsi[i]=C.xor(EB.and(this.bsi[i]));
					//C=this.bsi[i].and(C);
					C=and(EB,this.bsi[i],C);
				}
				res.size++;

			}
			
			res.addSlice(EB.NOT());
			//res.addSlice(C.and(EB));
		//	if(C.cardinality()!=0){
		//	res.bsi[res.size]=C;
		//res.size++;}
			res.sign=res.bsi[res.size-1];
			res.firstSlice=this.firstSlice;
			res.lastSlice=this.lastSlice;
		}else{
			int minSP = Math.min(this.size, intSize);
			res = new BsiUnsigned(Math.max(this.size, intSize)+1);
			//TODO implement this part
		}
		
		res.existenceBitmap = this.existenceBitmap;
		res.setPartitionID(this.getPartitionID());
		return res;	
		}
	}
	
	@Override
	public HybridBitmap rangeBetween(long lowerBound, long upperBound){	
		HybridBitmap B_gt = new HybridBitmap();
		HybridBitmap B_lt = new HybridBitmap();
		HybridBitmap B_eq1 = new HybridBitmap();	
		HybridBitmap B_eq2 = new HybridBitmap();
		HybridBitmap B_f = this.existenceBitmap;	
		B_gt.setSizeInBits(this.bsi[0].sizeInBits(),false);
		B_lt.setSizeInBits(this.bsi[0].sizeInBits(),false);
		B_eq1.setSizeInBits(this.bsi[0].sizeInBits(),true); B_eq1.density=1;
		B_eq2.setSizeInBits(this.bsi[0].sizeInBits(),true); B_eq2.density=1;		
		
		for(int i=this.getNumberOfSlices()-1; i>=0; i--){
			if((upperBound & (1<<i)) !=0){ //the i'th bit is set in upperBound
				B_lt = B_lt.or(B_eq1.andNot(this.bsi[i]));
				B_eq1 = B_eq1.and(this.bsi[i]);
			}else{ //The i'th bit is not set in uppperBound
				B_eq1=B_eq1.andNot(this.bsi[i]);
			}
			if((lowerBound & (1<<i)) != 0){ // the I'th bit is set in lowerBound
				B_eq2 = B_eq2.and(this.bsi[i]);
			}else{ //the i'th bit is not set in lowerBouond
				B_gt = B_gt.or(B_eq2.and(this.bsi[i]));
				B_eq2 = B_eq2.andNot(this.bsi[i]);
			}
		}
		B_lt = B_lt.or(B_eq1);
		B_gt = B_gt.or(B_eq2);
		B_f = B_lt.and(B_gt.and(B_f));	
		return B_f;
	}
	


	
	public BsiAttribute multiplyByConstant(int number) {
		BsiUnsigned res = null;		
		HybridBitmap C, S;
		int k = 0;
		while (number > 0) {
			if ((number & 1) == 1) {
				if (res == null) {
					res = new BsiUnsigned();
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
					for (int i = 1; i < this.size; i++) {// Add the slices of this to the current res
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
					for (int i = this.size + k; i < res.size; i++) {// Add the remaining slices of res with the Carry C
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




	public BsiSigned convertToTwos(int bits){
		BsiSigned res = new BsiSigned(bits);
		res.offset=this.offset;
		res.existenceBitmap = this.existenceBitmap;
		
		HybridBitmap zeroBitmap = new HybridBitmap();
		zeroBitmap.addStreamOfEmptyWords(false,this.existenceBitmap.actualsizeinwords);
		int i=0;
		for(i=0; i<this.getNumberOfSlices(); i++){
			res.addSlice(this.bsi[i]);
			}
		while(i<bits){
			res.addSlice(zeroBitmap);
			i++;
		}
		//this.setNumberOfSlices(bits);		
		res.setTwosFlag(true);		
			
		return res;
	}

	
	public BsiAttribute negate(){	
		
		HybridBitmap onesBitmap = new HybridBitmap();
		onesBitmap.setSizeInBits(this.bsi[0].sizeInBits(),true);
		onesBitmap.density=1;
		
		int signslicesize=1;
		if(this.firstSlice)
			signslicesize=2;
		
		BsiSigned res = new BsiSigned(this.getNumberOfSlices()+signslicesize);
		for(int i=0; i<this.getNumberOfSlices(); i++){
			res.bsi[i]=this.bsi[i].NOT();
//			try {
//				res.bsi[i]=(HybridBitmap) this.bsi[i].clone();
//			} catch (CloneNotSupportedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			res.bsi[i].not();
			res.size++;
			}		
			res.addSlice(onesBitmap);
		
		if(this.firstSlice){			
			res.addOneSliceNoSignExt(onesBitmap);
		}
		res.existenceBitmap=this.existenceBitmap;
		res.setPartitionID(this.getPartitionID());
		res.sign=res.bsi[res.size-1];
		res.firstSlice=this.firstSlice;
		res.lastSlice=this.lastSlice;
		res.setTwosFlag(true);			
		return res;			
	}



	@Override
	public BsiAttribute SUM(BsiAttribute a) {
		if (a.signed){
			return this.SUMsigned(a);
		}else{
			return this.SUMunsigned(a);
		}		
	}
	
	public HybridBitmap topKMax(int k) {
		HybridBitmap topK, SE, X;
		HybridBitmap G = new HybridBitmap();
		HybridBitmap E = new HybridBitmap();
		G.setSizeInBits(this.bsi[0].sizeInBits(),false);
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
	

	public HybridBitmap topKMin(int k){
		HybridBitmap topK, SNOT, X;
		HybridBitmap G = new HybridBitmap();
		HybridBitmap E = this.existenceBitmap;
		G.setSizeInBits(this.bsi[0].sizeInBits(),false);
		//E.setSizeInBits(this.bsi[0].sizeInBits(),true);
		//E.density=1;
		int n = 0;

		for (int i = this.size - 1; i >= 0; i--) {
			SNOT = E.andNot(this.bsi[i]);
			X = G.or(SNOT); //Maximum
			n = X.cardinality();
			if (n > k) {
				E = SNOT;
			}
			else if (n < k) {
				G = X;
				E = E.and(this.bsi[i]);
			}
			else {
				E = SNOT;
				break;
			}
		}
//		n = G.cardinality();
		topK = G.or(E); //with ties
		// topK = OR(G, E.first(k - n+ 1)); //Exact number of topK

		return topK;
	}
	  
	   

@Override
public BsiUnsigned abs() {
	
	return this;
}

public BsiUnsigned abs(int resultSlices, HybridBitmap EB) { //number of slices allocated for the result; Existence bitmap
//	HybridBitmap zeroBitmap = new HybridBitmap();
//	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);	
	int min = Math.min(this.size-1, resultSlices);
	BsiUnsigned res = new BsiUnsigned(min+1);
		for (int i=0; i<min; i++){
			res.bsi[i]=this.bsi[i];
			res.size++;
		}	
		res.size=min;	
	res.addSlice(EB.NOT()); // this is for KNN to add one slice
	res.existenceBitmap=this.existenceBitmap;
	res.setPartitionID(this.getPartitionID());
	res.firstSlice=this.firstSlice;
	res.lastSlice=this.lastSlice;
	return res;
}


@Override
public long getValue(int pos) {
	long sum = 0;
	HybridBitmap B_i;
	
	
	for (int i = 0; i < size; i++) {
		B_i = bsi[i];
		if(B_i.getBit(pos)) 
		sum += 1l<<(offset + i);
	}
	return sum;
}


public long sumBsiItself(){

		long sum=0;
		int position =1;

		for(int i=0; i<this.size; i++){
				sum += position*this.getSlice(i).cardinality();
			position = position*2;
		}
		return sum;
	}



	@Override
public BsiUnsigned absScale(double range) {
//	HybridBitmap zeroBitmap = new HybridBitmap();
//	zeroBitmap.setSizeInBits(this.bsi[0].sizeInBits(),false);
	
	HybridBitmap penalty = this.bsi[this.size-1];

	int resSize=0;
	for (int i=this.size-1;i>=0;i--){
		penalty=penalty.or(this.bsi[i]);
		if(penalty.cardinality()>=(this.bsi[0].sizeInBits()*range)){
		//if(penalty.density>=0.9){
		//if(i==this.size-8){
			resSize=i;
			break;
		}
	}
		
	BsiUnsigned res = new BsiUnsigned(resSize+1);
	
	
	

		for (int i=0; i<resSize; i++){
			res.bsi[i]=this.bsi[i];
			res.size++;
			
				
		}
		res.addSlice(penalty);
		
	
	res.existenceBitmap=this.existenceBitmap;
	res.setPartitionID(this.getPartitionID());
	res.firstSlice=this.firstSlice;
	res.lastSlice=this.lastSlice;
	return res;
}

//@Override
//public HybridBitmap rangeBetween(int lowerBound, int upperBound){	
//	HybridBitmap B_gt = new HybridBitmap();
//	HybridBitmap B_lt = new HybridBitmap();
//	HybridBitmap B_eq1 = new HybridBitmap();	
//	HybridBitmap B_eq2 = new HybridBitmap();
//	HybridBitmap B_f = this.existenceBitmap;	
//	B_gt.setSizeInBits(this.bsi[0].sizeInBits(),false);
//	B_lt.setSizeInBits(this.bsi[0].sizeInBits(),false);
//	B_eq1.setSizeInBits(this.bsi[0].sizeInBits(),true); B_eq1.density=1;
//	B_eq2.setSizeInBits(this.bsi[0].sizeInBits(),true); B_eq2.density=1;
//	
//	
//	
//	
//	for(int i=this.getNumberOfSlices()-1; i>=0; i--){
//		if((upperBound & (1<<i)) !=0){ //the i'th bit is set in upperBound
//			B_lt = B_lt.or(B_eq1.andNot(this.bsi[i]));
//			B_eq1 = B_eq1.and(this.bsi[i]);
//		}else{ //The i'th bit is not set in uppperBound
//			B_eq1=B_eq1.andNot(this.bsi[i]);
//		}
//		if((lowerBound & (1<<i)) != 0){ // the I'th bit is set in lowerBound
//			B_eq2 = B_eq2.and(this.bsi[i]);
//		}else{ //the i'th bit is not set in lowerBouond
//			B_gt = B_gt.or(B_eq2.and(this.bsi[i]));
//			B_eq2 = B_eq2.andNot(this.bsi[i]);
//		}
//	}
//	B_lt = B_lt.or(B_eq1);
//	B_gt = B_gt.or(B_eq2);
//	B_f = B_lt.and(B_gt.and(B_f));	
//	return B_f;
//}

//
//@Override
//public HybridBitmap rangeBetween(int lowerBound, int upperBound){	
//	HybridBitmap B_gt =this.bsi[this.size-1];
//	HybridBitmap B_lt = this.bsi[this.size-1].NOT();
//	HybridBitmap B_eq1 = new HybridBitmap();	
//	HybridBitmap B_eq2 = new HybridBitmap();
//	if((upperBound & (1<<(this.size-1))) !=0){
//		B_eq1 = B_gt;
//	}else{
//		B_eq1 = B_lt;
//		}
//		
//	if((lowerBound & (1<<(this.size-1))) !=0){
//		B_eq2 = B_lt;
//	}else{
//		B_eq2 =B_gt;
//	}
//	
//	HybridBitmap B_f = this.existenceBitmap;	
////	B_gt.setSizeInBits(this.bsi[0].sizeInBits(),false);
////	B_lt.setSizeInBits(this.bsi[0].sizeInBits(),false);
////	B_eq1.setSizeInBits(this.bsi[0].sizeInBits(),true); B_eq1.density=1;
////	B_eq2.setSizeInBits(this.bsi[0].sizeInBits(),true); B_eq2.density=1;	
//	
//	for(int i=this.getNumberOfSlices()-2; i>=0; i--){
//		if((upperBound & (1<<i)) !=0){ //the i'th bit is set in upperBound
//			B_lt = B_lt.or(B_eq1.andNot(this.bsi[i]));
//			B_eq1 = B_eq1.and(this.bsi[i]);
//		}else{ //The i'th bit is not set in uppperBound
//			B_eq1=B_eq1.andNot(this.bsi[i]);
//		}
//		if((lowerBound & (1<<i)) != 0){ // the I'th bit is set in lowerBound
//			B_eq2 = B_eq2.and(this.bsi[i]);
//		}else{ //the i'th bit is not set in lowerBouond
//			B_gt = B_gt.or(B_eq2.and(this.bsi[i]));
//			B_eq2 = B_eq2.andNot(this.bsi[i]);
//		}
//	}
//	B_lt = B_lt.or(B_eq1);
//	B_gt = B_gt.or(B_eq2);
//	B_f = B_lt.and(B_gt.and(B_f));	
//	return B_f;
//}

	
	
	
	

}
