package com.company.topK;

import com.company.hybridewah.HybridBitmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.company.bsi.BsiAttributeEWAH;

public class topPrefEWAH {
	static boolean genRpt = true; // Set this to false to not print the results
	static String outputFile = "";
	static logger logFile = new logger();

//	static BitmapIndexEWAH bitmapindex;
	static Map<String, BsiAttributeEWAH> bsiBitmaps = new ConcurrentHashMap<String, BsiAttributeEWAH>();
	static String tablename = "intUniform";
	static String queryfile = "";
	static int[][] rawData;
	static int rows = 1000000;
	static int attribs = 50;
	static int newAttribs = 50;
//	public static double andThreshold=0.0001;
	//public static double orThreshold=0.0002;
	//public static double andThreshold=0.001;
	//public static double orThreshold=0.001;
    static boolean verbatim = true; // for verbatim set this to true and the compTresh to zero.
    static double compTresh =0;
	// static int bitsPerAtt = 10;
	static int[] maxPerAtt = new int[attribs];
	static BsiAttributeEWAH[] bitSlices = new BsiAttributeEWAH[attribs];
	
	// static int[] bitsxattrib = { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 };
	static int WORD = 64;
	static int[][] queries;
	static long[] power2 = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432,
			67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L };
	static boolean compressed = false;
	static boolean hasId = false;

	// read data from file
	public static void readRawData(String filename) {
		int W = WORD;
		if (compressed) {
			W = WORD - 1;
		}
		// String filename = datafolder+"/"+tablename;
		if (!filename.contains(".")) {
			filename = filename + ".txt";
		}
		rawData = new int[rows][attribs];

		FileInputStream fin;
		int seq = 0;
		try {
			// Open an input stream
			fin = new FileInputStream(filename);
			// Read a line of text
			DataInputStream input = new DataInputStream(fin);
			String line = input.readLine();
			while ((line != null && line.compareTo("") != 0) && (seq < rows)) {
				// System.out.println(line);
				seq++;
				StringTokenizer strT;
				strT = new StringTokenizer(line, ",\t");
				int id;
				if (hasId) {
					id = Integer.parseInt(strT.nextToken());
					
				} else {
					id = seq;
				}
				int attrNumber = 0;
				int toks = attribs;
				while ((strT.hasMoreTokens() && toks > 0)) {
					
					int f = Integer.parseInt(strT.nextToken());
					//double f = Double.parseDouble(strT.nextToken());
					
					rawData[seq - 1][attrNumber] = f;
					//System.out.println((int) Math.abs(f*100000000));
					//if ((int) Math.abs(f*100000000) > maxPerAtt[attrNumber]) {
						if (f > maxPerAtt[attrNumber]) {
						maxPerAtt[attrNumber] = f;
					}
					attrNumber++;
					toks--;
				}

				// // if (seq%10000==0)
				// // DEBUG: System.out.println("Line "+seq+": "+line);
				line = input.readLine();
			}
			// rows = seq;
			// Close our input stream
			fin.close();
			// Catches any error conditions
		} catch (IOException e) {
			System.err.println("Unable to read from file");
			e.printStackTrace();
		}
	}
	
	public static void simulateAttributes() {
		int[][] temp = rawData;
		int tempMax = maxPerAtt[0];
		
		rawData = new int[rows][newAttribs];
		maxPerAtt = new int[newAttribs];
				
		
		for(int i=0; i< newAttribs; i++){
			for(int j=0; j<rows; j++){	
			rawData[j][i] = temp[j][0];			
		} maxPerAtt[i] = tempMax;
			}
		
		attribs = newAttribs;
		
	}

	public static void readQueries(int nQueries) {
		String filename = queryfile;
		if (!filename.contains(".")) {
			filename = filename + ".txt";
		}
		queries = new int[nQueries][attribs];
		FileInputStream fin;
		int seq = 0;
		try {
			// Open an input stream
			fin = new FileInputStream(filename);
			// Read a line of text
			DataInputStream input = new DataInputStream(fin);
			String line = input.readLine();
			while (line != null && line.compareTo("") != 0 && seq < nQueries) {
				// System.out.println(line);
				seq++;
				StringTokenizer strT;
				strT = new StringTokenizer(line, ",\t");
				int attrNumber = 0;
				while (strT.hasMoreTokens() && attrNumber < attribs) {
					int f = Integer.parseInt(strT.nextToken());
					queries[seq - 1][attrNumber] = f;
					attrNumber++;
				}
				if (seq % 1000 == 0)
					genRpt: System.out.println("Line " + seq + ": " + line);
				line = input.readLine();
			}
			// rows = seq;
			// Close our input stream
			fin.close();
			// Catches any error conditions
		} catch (IOException e) {
			System.err.println("Unable to read from file");
			e.printStackTrace();
		}
	}

	// build the bitmap as verbatim
	public static void buildBitmaps_Verbatim(String fileOut) throws IOException {
		bitArray[][] bitmapDataRaw; // One for each column

		int thisBin = 0;
		int maxSplits = 0;

		for (int a = 0; a < attribs; a++) {
			// int splits = bitsxattrib[a];
			int splits = Integer.toBinaryString(maxPerAtt[a]).length();
			if (splits > maxSplits) {
				maxSplits = splits;
			}
		}

		bitmapDataRaw = new bitArray[attribs][maxSplits];
		int wordsNeeded = (int) Math.ceil((double) rows / WORD);

		for (int a = 0; a < attribs; a++) {
			for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
				bitmapDataRaw[a][c] = new bitArray(wordsNeeded);
				bitmapDataRaw[a][c].maxPos = wordsNeeded;
			}
		}

		// System.out.println(bitmapData[0].length+" "+bitmapData.length+" "+qData.length+" "+qData[0].length);
		for (int seq = 0; seq < rawData.length; seq++) {
			int w = seq / WORD;
			int offset = seq % WORD;
			for (int a = 0; a < attribs; a++) {
				thisBin = rawData[seq][a];
				// System.out.print(seq+" "+a+" "+w+" "+offset+" "+thisBin+"          ");
				// bitmapData[a][thisBin].vec[w] |= power2[offset];
				int slice = 0;
				while (thisBin > 0) {
					if ((thisBin & 1) == 1) {
						
						bitmapDataRaw[a][slice].vec[w] |= (1L << offset);
						
						
					}
					thisBin >>= 1;
					slice++;
				}
			}

		}

		

		HybridBitmap[][] bitmapData = new HybridBitmap[attribs][maxSplits];
		HybridBitmap[][] bitmapDataCompressed = new HybridBitmap[attribs][maxSplits];

		for (int a = 0; a < attribs; a++) {
			for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
				bitmapData[a][c] = new HybridBitmap(true);
				
				bitmapDataCompressed[a][c]= new HybridBitmap();
				// bitmapData[a][c].setVerbatim(true);
				for (int l = 0; l < bitmapDataRaw[a][c].maxPos; l++) {
					bitmapData[a][c].addVerbatim(bitmapDataRaw[a][c].vec[l]);
					bitmapDataCompressed[a][c].add(bitmapDataRaw[a][c].vec[l]);
				}
				// bitmapData[a][c].maxPos = wordsNeeded;
			}
		}

		for (int a = 0; a < attribs; a++) {
			BsiAttributeEWAH bsi = new BsiAttributeEWAH();			
			for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
				bsi.add(bitmapData[a][c]);
			}
			FileOutputStream out = new FileOutputStream(fileOut + "_" + a);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(bsi);
			oout.flush();
			out.close();
//			
//			XMLEncoder e = new XMLEncoder(
//                    new BufferedOutputStream(
//                        new FileOutputStream(fileOut + "_" + a)));
//			e.writeObject(bsi);
//			e.close();
		}
		
		for (int a = 0; a < attribs; a++) {
			BsiAttributeEWAH bsi = new BsiAttributeEWAH();			
			for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
				bsi.add(bitmapDataCompressed[a][c]);
			}
			FileOutputStream out = new FileOutputStream(fileOut + "C_" + a);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(bsi);
			oout.flush();
			out.close();
			
//			XMLEncoder e = new XMLEncoder(
//                    new BufferedOutputStream(
//                        new FileOutputStream(fileOut + "C_" + a)));
//			e.writeObject(bsi);
//			e.close();
			
		}
	}
	
	
//
//	public static void createAllZeros() {
//		bitmapindex = new BitmapIndexEWAH(tablename, "");
//		int wordsNeeded = (int) Math.ceil((double) rows / WORD);
//		EWAHCompressedBitmap all1s = new EWAHCompressedBitmap();
//		// all1s.setVerbatim(true);
//		
//		
//		EWAHCompressedBitmap all0s = new EWAHCompressedBitmap();
//		// all0s.setVerbatim(true);
//		
//		if(verbatim){
//			 all1s = new EWAHCompressedBitmap(true);
//			 all0s = new EWAHCompressedBitmap(true);
//			
//			
//			for (int w = 0; w < wordsNeeded; w++) {
//				// all1s.vec[w] = 0x7FFFFFFF;
//				all1s.addVerbatim(0xFFFFFFFFFFFFFFFFL);
//				all0s.addVerbatim(0);
//			}			
//		}else{
//			// all1s.maxPos = wordsNeeded;
//			for (int w = 0; w < wordsNeeded; w++) {
//				// all1s.vec[w] = 0x7FFFFFFF;
//				all1s.add(0xFFFFFFFFFFFFFFFFL);
//				all0s.add(0);
//			}			
//		}		
//		
//		all0s.density = 0;
//		all1s.density = 1;
//
//		bitmapindex.put("B1", all1s);
//		bitmapindex.put("B0", all0s);
//	}
	
private static void groupBy(double d, int attNum) {
	int wordsNeeded = (int) Math.ceil((double) rows / WORD);
	HybridBitmap groupByBitmap =generateGroupByBitmap(d,wordsNeeded);
	for (int i = 0; i < attNum; i++) {
		for (int j = 0; j < bitSlices[i].getSize(); j++) {
			bitSlices[i].bsi[j] = groupByBitmap.and(bitSlices[i].bsi[j]);
			bitSlices[i].bsi[j].density = bitSlices[i].bsi[j].cardinality()/(double)bitSlices[i].bsi[j].sizeInBits();
		}
	}
	
		
	}

	private static HybridBitmap generateGroupByBitmap(double d, int wordsNeeded) {
		Random rand = new Random();
		HybridBitmap ewahBitmap1 = new HybridBitmap();

		String bitSTR = "";
		int density = (int) (1/d);		
		int random;
		for (int i = 0; i < wordsNeeded; i++) {
			random = rand.nextInt(density);
			if (random == 0)
				bitSTR = "1";
			else
				bitSTR = "0";
			for (int j = 0; j < 63; j++) {
				random = rand.nextInt(density);
				if (random == 0)
					bitSTR += "1";
				else
					bitSTR += "0";
			}
			ewahBitmap1.add(Long.parseUnsignedLong(bitSTR, 2));
		}
		return ewahBitmap1;
	}

	public static void readBitmapsFromDisk(String fileName, int attNum) throws ClassNotFoundException, IOException {

		for (int i = 0; i < attNum; i++) {
			FileInputStream fin = new FileInputStream(fileName + "_" + i);
			ObjectInputStream ois = new ObjectInputStream(fin);
			bitSlices[i] = (BsiAttributeEWAH) ois.readObject();
			ois.close();
			
//			 XMLDecoder d = new XMLDecoder(
//                     new BufferedInputStream(
//                         new FileInputStream(fileName + "_" + i)));
//			 bitSlices[i] = (BsiAttributeEWAH) d.readObject();
//			 d.close();
			 
			
			FileInputStream finn = new FileInputStream(fileName + "C_" + i);
			ObjectInputStream oiss = new ObjectInputStream(finn);
			BsiAttributeEWAH compressedBSI = (BsiAttributeEWAH) oiss.readObject();
			oiss.close();
			
//			 XMLDecoder dd = new XMLDecoder(
//                     new BufferedInputStream(
//                         new FileInputStream(fileName + "C_" + i)));
//			 BsiAttributeEWAH compressedBSI = (BsiAttributeEWAH) dd.readObject();
//			 dd.close();
			
			for (int j = 0; j < bitSlices[i].getSize(); j++) {
				if(compressedBSI.bsi[j].actualsizeinwords<compTresh*bitSlices[i].bsi[j].actualsizeinwords){
					bitSlices[i].bsi[j]=compressedBSI.bsi[j];
				}else{
				bitSlices[i].bsi[j].setVerbatim(true);}
				//bitSlices[i].bsi[j].setbits = bitSlices[i].bsi[j].cardinality();
				bitSlices[i].bsi[j].density = bitSlices[i].bsi[j].cardinality()/(double)bitSlices[i].bsi[j].sizeInBits();
				//bitSlices[i].bsi[j].setSizeInBits(rows);
			}
			
		}

	}

	

	

	public static void readQueries(int nQueries, String filename) {
		if (!filename.contains(".")) {
			filename = filename + ".txt";
		}
		queries = new int[nQueries][attribs];
		FileInputStream fin;
		int seq = 0;
		try {
			// Open an input stream
			fin = new FileInputStream(filename);
			// Read a line of text
			DataInputStream input = new DataInputStream(fin);
			String line = input.readLine();
			while (line != null && line.compareTo("") != 0) {
				// System.out.println(line);
				seq++;
				StringTokenizer strT;
				strT = new StringTokenizer(line, ",\t");
				int attrNumber = 0;
				while (strT.hasMoreTokens() && attrNumber < attribs) {
					int f = Integer.parseInt(strT.nextToken());
					queries[seq - 1][attrNumber] = f;
					attrNumber++;
				}
				if (seq % 1000 == 0)
					genRpt: System.out.println("Line " + seq + ": " + line);
				line = input.readLine();
			}
			// rows = seq;
			// Close our input stream
			fin.close();
			// Catches any error conditions
		} catch (IOException e) {
			System.err.println("Unable to read from file");
			e.printStackTrace();
		}
	}

	

	public static HybridBitmap executeQuery_inplace(int topK, int[] query) {
		int k, p = 0, j = 0;
		BsiAttributeEWAH res = null, temp;// new BsiAttribute();
		
		for (j = 0; j < query.length; j++) {
			if (query[j] > 0) {
				//temp = bitSlices[j].multiply_inPlace(query[j]);
				temp = bitSlices[j];
				if (res == null) {
					res = temp;
				} else {
					//System.out.println("TEMP "+j+": "+temp.SUM());
					res = res.SUM_BSI_offset(temp);
					
				}
				// System.out.println("SUM "+j+": "+res.SUM());
			}
		}
		// System.out.println("SUM: "+res.SUM());
		HybridBitmap tRes = null;
		if (res != null)
			tRes = res.topKMax(topK);
		// bitmapindex.topKMax("res", p, topK, "top");
		// System.out.println(tRes.toString());
		return tRes;
	}
	
	public static BsiAttributeEWAH executeSum(int attributes) {
		int k, p = 0, j = 0;
		BsiAttributeEWAH res = bitSlices[0];
		BsiAttributeEWAH temp=null;// new BsiAttribute();
		
		for (j = 1; j < attributes; j++) {
			
				//temp = bitSlices[j].multiply_inPlace(query[j]);
				temp = bitSlices[j];
				
					//System.out.println("TEMP "+j+": "+temp.SUM());
					res = res.SUM_BSI_offset(temp);
					//res=res.SUM_BSI_noOffset(temp);
					//res=res.SUM_BSI_inPlace(temp);
					
				
				// System.out.println("SUM "+j+": "+res.SUM());
			
		}
		long total=0;
		for(int i=0;i<res.getSize();i++){
			total=res.bsi[i].cardinality()*(long)Math.pow(2, i)+total;
			
		}
			
		 System.out.println("SUM: "+total);
		
		// bitmapindex.topKMax("res", p, topK, "top");
		// System.out.println(tRes.toString());
		return res;
	}

	public static HybridBitmap executeQuery_verbatim(int topK, int[] query) {
		int k, p = 0, j = 0;
		BsiAttributeEWAH res = null, temp;// new BsiAttribute();
		for (j = 0; j < query.length; j++) {
			if (query[j] > 0) {
				//temp = bitSlices[j].multiply_verbatim(query[j]);
				temp = bitSlices[j];
				if (res == null) {
					res = temp;
				} else {
					//System.out.println("TEMP "+j+": "+temp.SUM());
					res = res.SUM_BSI_verbatim(temp);
					
				}
				// System.out.println("SUM "+j+": "+res.SUM());
			}
		}
		// System.out.println("SUM: "+res.SUM());
		HybridBitmap tRes = null;
		if (res != null)
			tRes = res.topKMax_verbatim(topK);
		// bitmapindex.topKMax("res", p, topK, "top");
		// System.out.println(tRes.toString());
		return tRes;
	}
	

	public static long run_queries_inplace(int topK, boolean verbatm) {
		if (genRpt) {
			logFile.initializeLogging(outputFile + "_inplace.rpt");
			logFile.log("QueryId,Time,RowIds\n");
		}

		HybridBitmap b = null;
		long time = 0, start, end;
		int j = 0, k, i, p = 0;
		for (j = 0; j < queries.length; j++) {
			start = System.nanoTime();
			if(verbatm)
			b=	executeQuery_verbatim(topK, queries[j]);
			else
			b = executeQuery_inplace(topK, queries[j]);
			end = System.nanoTime();
			time += (end - start);
			// if (j<10) {
			// System.out.println(b.getIDs());
			// }
			if (genRpt) {// Print top K to the file
				logFile.log((j + 1) + "," + (end - start) / (double)1000000 + "," + Arrays.toString(b.getPositions()) + "\n");
			}
		}
		// bitmapindex.clearResultsBitmap();
		// System.out.println(topPref.bitmapindex.bitmaps.get("top"));
		// topPref.bitmapindex.bitmaps.remove("res");
		// topPref.bitmapindex.bitmaps.remove("top");
		// System.out.println("Query "+(i+1)+": \t"+(endTime-startTime));

		if (genRpt) {
			logFile.closeFiles();
		}
		return time;

	}
	
	public static long runSum(int attributes) {
		BsiAttributeEWAH b = null;
		long time = 0, start, end;
		int j = 0, k, i, p = 0;
		
			start = System.nanoTime();
			
			b = executeSum(attributes);
			end = System.nanoTime();
			time += (end - start);
			// if (j<10) {
			// System.out.println(b.getIDs());
			// }
			
		
		// bitmapindex.clearResultsBitmap();
		// System.out.println(topPref.bitmapindex.bitmaps.get("top"));
		// topPref.bitmapindex.bitmaps.remove("res");
		// topPref.bitmapindex.bitmaps.remove("top");
		// System.out.println("Query "+(i+1)+": \t"+(endTime-startTime));

		
		return time;

	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		String foldername = "D://dblab//research//prefQueries//data//";

		/* Test data */
		/*
		 * rows = 100; attribs = 3; queryfile = foldername+
		 * "uniform_a3_10query"; readQueries (10); int taskSize = 100; int
		 * numTasks = queries.length / taskSize; System.out.println(numTasks);
		 * readRawData(foldername + "uniform_a3_r100_c11");
		 * //buildBitmaps_Verbatim(foldername+"bitmaps\\test");
		 * readBitmapsFromDisk(foldername+"bitmaps\\test", attribs);
		 * createAllZeros();
		 */

		/* 100K data */

		rows = 1000000;
		attribs = 50;

		//readRawData(foldername + "generated\\test1M_zipf1");
		//simulateAttributes(); //copies the first attribute *newAttributes times
		//buildBitmaps_Verbatim(foldername +"bitmaps/EWAH/test1M_zipf1");
		// bitSlices = new BsiAttributeEWAH[newAttribs];
		// attribs = newAttribs;
		readBitmapsFromDisk(foldername + "bitmaps//EWAH//test1M_zipf1", attribs);
	//	createAllZeros();
		//groupBy(1, attribs);

		queryfile = foldername + "queries/kegg.txt";
		readQueries(100);
		long timeSum=0;
		// System.out.println("sparse 10");
		for (int i = 0; i < 10; i++) {

			//BsiAttributeVerb.wordsNeeded = (int) Math.ceil((double) rows / WORD);
			// System.out.println("Mine: ");
			long time0 = 0;
			// time0 = run_queries(5);
			// System.out.println("TIme:  " + (double) time0 / 1000000);

			

			System.out.println("Summation time:  ");
			
//			time0 = run_queries_inplace(10,verbatim);
			time0=runSum(attribs);
			
			
			System.out.println("Time:  " + (double) time0 / 1000000);
			if(i>0)
				timeSum+=time0;
			
//			System.out.println("Gheorghi's: ");
//			long time = 0;
//			long startTime = System.nanoTime();
//			run_queries2();
//			time = System.nanoTime() - startTime;
//			System.out.println("TIme:  " + (double) time / 1000000);

		}
		System.out.println("AvgTime:  " + (double) timeSum / 9000000);
		
		
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory=runtime.totalMemory()-runtime.freeMemory();
		System.out.println("Used memory in bytes: "+memory);
		/*
		 * queryfile = foldername + "queries\\a1K_r1K_100"; readQueries(10);
		 * System.out.println("sparse 100"); for (int i = 0; i < 5; i++) {
		 * 
		 * BsiAttribute.wordsNeeded = (int) Math.ceil((double) rows / WORD);
		 * System.out.println("Mine: "); double time0 = 0; time0 =
		 * run_queries(20); System.out.println("TIme:  " + (double) time0 /
		 * 1000000);
		 * 
		 * System.out.println("Gheorghi's: "); long time = 0; long startTime =
		 * System.nanoTime(); run_queries2(); time = System.nanoTime() -
		 * startTime; System.out.println("TIme:  " + (double) time / 1000000);
		 * 
		 * System.out.println("In place: "); time0 = run_queries_inplace(20);
		 * System.out.println("TIme:  " + (double) time0 / 1000000);
		 * 
		 * }
		 * 
		 * queryfile = foldername + "queries\\a1K_r1K_200"; readQueries(10);
		 * System.out.println("sparse 200"); for (int i = 0; i < 5; i++) {
		 * 
		 * BsiAttribute.wordsNeeded = (int) Math.ceil((double) rows / WORD);
		 * System.out.println("Mine: "); double time0 = 0; time0 =
		 * run_queries(20); System.out.println("TIme:  " + (double) time0 /
		 * 1000000);
		 * 
		 * System.out.println("Gheorghi's: "); long time = 0; long startTime =
		 * System.nanoTime(); run_queries2(); time = System.nanoTime() -
		 * startTime; System.out.println("TIme:  " + (double) time / 1000000);
		 * 
		 * System.out.println("In place: "); time0 = run_queries_inplace(20);
		 * System.out.println("TIme:  " + (double) time0 / 1000000);
		 * 
		 * }
		 * 
		 * queryfile = foldername + "queries\\a1K_r1K_500"; readQueries(10);
		 * System.out.println("sparse 500"); for (int i = 0; i < 5; i++) {
		 * 
		 * BsiAttribute.wordsNeeded = (int) Math.ceil((double) rows / WORD);
		 * System.out.println("Mine: "); double time0 = 0; time0 =
		 * run_queries(20); System.out.println("TIme:  " + (double) time0 /
		 * 1000000);
		 * 
		 * System.out.println("Gheorghi's: "); long time = 0; long startTime =
		 * System.nanoTime(); run_queries2(); time = System.nanoTime() -
		 * startTime; System.out.println("TIme:  " + (double) time / 1000000);
		 * 
		 * System.out.println("In place: "); time0 = run_queries_inplace(20);
		 * System.out.println("TIme:  " + (double) time0 / 1000000);
		 * 
		 * }
		 * 
		 * queryfile = foldername + "queries\\a1K_r1K_800"; readQueries(10);
		 * System.out.println("sparse 800"); for (int i = 0; i < 5; i++) {
		 * 
		 * BsiAttribute.wordsNeeded = (int) Math.ceil((double) rows / WORD);
		 * System.out.println("Mine: "); double time0 = 0; time0 =
		 * run_queries(20); System.out.println("TIme:  " + (double) time0 /
		 * 1000000);
		 * 
		 * System.out.println("Gheorghi's: "); long time = 0; long startTime =
		 * System.nanoTime(); run_queries2(); time = System.nanoTime() -
		 * startTime; System.out.println("TIme:  " + (double) time / 1000000);
		 * 
		 * System.out.println("In place: "); time0 = run_queries_inplace(20);
		 * System.out.println("TIme:  " + (double) time0 / 1000000);
		 * 
		 * }
		 * 
		 * queryfile = foldername + "queries\\a1K_r1K_1000"; readQueries(10);
		 * System.out.println("sparse 1000"); for (int i = 0; i < 5; i++) {
		 * 
		 * BsiAttribute.wordsNeeded = (int) Math.ceil((double) rows / WORD);
		 * System.out.println("Mine: "); double time0 = 0; time0 =
		 * run_queries(20); System.out.println("TIme:  " + (double) time0 /
		 * 1000000);
		 * 
		 * System.out.println("Gheorghi's: "); long time = 0; long startTime =
		 * System.nanoTime(); run_queries2(); time = System.nanoTime() -
		 * startTime; System.out.println("TIme:  " + (double) time / 1000000);
		 * 
		 * System.out.println("In place: "); time0 = run_queries_inplace(20);
		 * System.out.println("TIme:  " + (double) time0 / 1000000);
		 * 
		 * }
		 */
	}

	

}
