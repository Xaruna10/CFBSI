package com.company.topK;

import com.company.hybridewah.HybridBitmap;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;




public class test {

  public static void main(String[] args) throws IOException, ClassNotFoundException {
	  
//	 // System.out.println(Long.toBinaryString(-9223372036854775807l +1));
//	  long alloctime = System.nanoTime();
//	  int[] array = new int[100000000];
//	  System.out.println("Allocation time:  "+(System.nanoTime()-alloctime));
//	  ArrayList<Integer> arrayList = new ArrayList<Integer>(100000000);
//	  
//	  Random ran = new Random();
//	  int temp;
//	  for(int i=0; i< array.length; i++){
//		  temp=ran.nextInt();
//		  array[i]=temp;
//		  arrayList.add(temp);
//	  }
//	 
//	  for(int i=0; i< 10; i++){
//	long time = System.nanoTime();
//	accessArray(array);
//	System.out.println("time for array: "+(System.nanoTime()-time));
//	
//	long time2 = System.nanoTime();
//	accessArrayList(arrayList);
//	System.out.println("time for arrayList: "+(System.nanoTime()-time2));
//	  }
	  
	  byte data = 1;
	  
	  System.out.println(data<<9);

	 HybridBitmap c = new HybridBitmap();
	 HybridBitmap v = new HybridBitmap(true);
	 
	 
	 for(int i=0; i<160000; i++){
		 long randLong = generateRandomLong(10);
		 c.add(randLong);
		 v.addVerbatim(randLong);
	 }
	 
	 long startTime; 
	 
	 for(int i=0; i<20; i++){
		 
		 startTime = System.nanoTime();
		 int[] vPos = v.getPositions();
		 System.out.println("verbatim : "+(System.nanoTime()-startTime)/(double)1000000);
	 
	 startTime = System.nanoTime();
	 int[] optvPos = v.getPositionsOptimized();
	 System.out.println("verbatim optimized : "+(System.nanoTime()-startTime)/(double)1000000);
	 
	
	 startTime = System.nanoTime();
	 int[] cPos = c.getPositions();
	 System.out.println("compressed : "+(System.nanoTime()-startTime)/(double)1000000);
	
	 
	 startTime = System.nanoTime();
	 int[] optcPos = c.getPositionsOptimized();
	 System.out.println("compressed optimized : "+(System.nanoTime()-startTime)/(double)1000000);
	 
	 
	 
	 System.out.println();
	 }
	 
	
	 
	 
	  
  }
  public static long generateRandomLong(int density){
	  Random ran = new Random();
	  
	  long res =0L;
	  for(int i=0; i<64; i++){
		 int randomVal =  ran.nextInt(density);
		 if(randomVal==0)
			 res+=res+1;
		 res=res<<1;		  
	  }	  
	  return res;	  
  }
  
  
  public static boolean isNumeric(String str)  
  {  
    try  
    {  
      double d = Double.parseDouble(str);  
    }  
    catch(NumberFormatException nfe)  
    {  
      return false;  
    }  
    return true;  
  }
  
  public static void accessArray(int[] array){
	  
	  int temp=0;
	  for (int i=0; i<array.length; i++){
		  temp=array[i];
	  }
  }
  
  public static void accessArrayList(ArrayList<Integer> arrayList){
	  
	  int temp=0;
	  for (int i=0; i<arrayList.size(); i++){
		  temp=arrayList.get(i);
	  }
  }
  
  public static ArrayList<Integer> lemire(int w){
	ArrayList<Integer> S = new ArrayList<Integer>();
	  while (w!=0){
		  int t=w&(-w);
		  S.add(Integer.bitCount(t-1));
		  w=w&(w-1);
	  }
	  
	  return S;
	  
  }

}
