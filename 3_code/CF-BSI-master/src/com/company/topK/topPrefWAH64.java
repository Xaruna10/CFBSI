package com.company.topK;
import com.company.hybridewah.HybridBitmap;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.company.bsi.BsiAttributeWAH;

public class topPrefWAH64 {
	static boolean genRpt = true; // Set this to false to not print the results
	static String outputFile = "";
	static logger logFile = new logger();

	//static BitmapIndexEWAH bitmapindex;
	static Map<String, BsiAttributeWAH> bsiBitmaps = new ConcurrentHashMap<String, BsiAttributeWAH>();
	static String tablename = "intUniform";
	static String queryfile = "";
	static int[][] rawData;
	static int rows = 10000000;
	static int attribs = 5;
	
	
	// static int bitsPerAtt = 10;
	static int[] maxPerAtt = new int[attribs];
	static BsiAttributeWAH[] bitSlices = new BsiAttributeWAH[attribs];
	
	// static int[] bitsxattrib = { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 };
	static int WORD = 63;
	static int[][] queries;

	static boolean compressed = true;
	static boolean hasId = false;

	// read data from file
	public static void readRawData(String filename) {
		
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

		
		bitArrayWAH[][] bitmapDataCompressed = new bitArrayWAH[attribs][maxSplits];

		for (int a = 0; a < attribs; a++) {
			for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {				
				
				bitmapDataCompressed[a][c]= new bitArrayWAH(bitmapDataRaw[a][c].maxPos);
				// bitmapData[a][c].setVerbatim(true);
				for (int l = 0; l < bitmapDataRaw[a][c].maxPos; l++) {					
					bitmapDataCompressed[a][c].appendWord(bitmapDataRaw[a][c].vec[l]);
				}
				// bitmapData[a][c].maxPos = wordsNeeded;
			}
		}

		
		
		for (int a = 0; a < attribs; a++) {
			BsiAttributeWAH bsi = new BsiAttributeWAH();			
			for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
				bsi.add(bitmapDataCompressed[a][c]);
			}
			FileOutputStream out = new FileOutputStream(fileOut + "_" + a);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(bsi);
			oout.flush();
			out.close();			

			
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
			bitSlices[i] = (BsiAttributeWAH) ois.readObject();
			ois.close();		
			
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

	

	

	public static bitArrayWAH executeQuery(int topK, int[] query) {
		int k, p = 0, j = 0;
		BsiAttributeWAH res = null, temp;// new BsiAttribute();
		
		for (j = 0; j < query.length; j++) {
			if (query[j] > 0) {
				//temp = bitSlices[j].multiply_inPlace(query[j]);
				temp = bitSlices[j];
				if (res == null) {
					res = temp;
				} else {
					//System.out.println("TEMP "+j+": "+temp.SUM());
					res = res.SUM_BSI_inPlace(temp);
					
				}
				// System.out.println("SUM "+j+": "+res.SUM());
			}
		}
		// System.out.println("SUM: "+res.SUM());
		bitArrayWAH tRes = null;
		if (res != null)
			tRes = res.topKMax(topK);
		// bitmapindex.topKMax("res", p, topK, "top");
		// System.out.println(tRes.toString());
		return tRes;
	}

	
	

	public static double run_queries(int topK) {
		if (genRpt) {
			logFile.initializeLogging(outputFile + "_inplace.rpt");
			logFile.log("QueryId,Time,RowIds\n");
		}

		bitArrayWAH b = null;
		double time = 0, start, end;
		int j = 0, k, i, p = 0;
		for (j = 0; j < queries.length; j++) {
			start = System.nanoTime();			
			b = executeQuery(topK, queries[j]);
			end = System.nanoTime();
			time += (end - start);
			// if (j<10) {
			// System.out.println(b.getIDs());
			// }
			if (genRpt) {// Print top K to the file
				logFile.log((j + 1) + "," + (end - start) / 1000000 + "," + b.getSetBitIDs() + "\n");
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

		rows = 10000000;
		attribs = 5;

		readRawData(foldername + "generated/uniform_c1k_a5_r10m");
		buildBitmaps_Verbatim(foldername +
		 "bitmaps/WAH/uniform_c1k_a5_r10m");
		
		readBitmapsFromDisk(foldername + "bitmaps//WAH//uniform_c1k_a5_r10m", attribs);
		//createAllZeros();
		//groupBy(1, attribs);

		queryfile = foldername + "queries/a5_r1k_c10.txt";
		readQueries(5);
		// System.out.println("sparse 10");
		for (int i = 0; i < 10; i++) {

			BsiAttributeVerb.wordsNeeded = (int) Math.ceil((double) rows / WORD);
			// System.out.println("Mine: ");
			double time0 = 0;
			// time0 = run_queries(5);
			// System.out.println("TIme:  " + (double) time0 / 1000000);

			

			System.out.println("In place: ");
			
			time0 = run_queries(10);
			
			System.out.println("TIme:  " + (double) time0 / 1000000);
			
//			System.out.println("Gheorghi's: ");
//			long time = 0;
//			long startTime = System.nanoTime();
//			run_queries2();
//			time = System.nanoTime() - startTime;
//			System.out.println("TIme:  " + (double) time / 1000000);

		}
		
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory=runtime.totalMemory()-runtime.freeMemory();
		System.out.println("Used memory in bytes: "+memory);
		
	}

	

}
