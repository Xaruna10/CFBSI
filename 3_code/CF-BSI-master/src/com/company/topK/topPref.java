package com.company.topK;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;


public class topPref {
   static boolean genRpt = true; //Set this to false to not print the results
   static String outputFile = "";
   static logger  logFile = new logger();
    
    
  static BitmapIndex64 bitmapindex;
  static Map<String, BsiAttributeVerb> bsiBitmaps =
      new ConcurrentHashMap<String, BsiAttributeVerb>();
  static String tablename = "intUniform";
  static String queryfile = "";
  static int[][] rawData;
	static int rows = 10000000;
	static int attribs = 5;
  // static int bitsPerAtt = 10;
  static int[] maxPerAtt = new int[attribs];
  static BsiAttributeVerb[] bitSlices = new BsiAttributeVerb[attribs];
  // static int[] bitsxattrib = { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 };
	static int WORD = 64;
  static int[][] queries;
  static long[] power2 = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
      4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576,
      2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728,
      268435456, 536870912, 1073741824, 2147483648L };
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
    rawData = new int[rows][attribs + 1];

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
          rawData[seq - 1][attrNumber] = f;
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
    bitArray64[][] bitmapData; // One for each column

    int thisBin = 0;
    int maxSplits = 0;

    for (int a = 0; a < attribs; a++) {
      // int splits = bitsxattrib[a];
      int splits = Integer.toBinaryString(maxPerAtt[a]).length();
      if (splits > maxSplits) {
        maxSplits = splits;
      }
    }

    bitmapData = new bitArray64[attribs][maxSplits];
    int wordsNeeded = (int) Math.ceil((double) rows / WORD);

    for (int a = 0; a < attribs; a++) {
      for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
        bitmapData[a][c] = new bitArray64(wordsNeeded);
        bitmapData[a][c].maxPos = wordsNeeded;
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
            bitmapData[a][slice].vec[w] |= (1L << offset);
          }
          thisBin >>= 1;
          slice++;
        }
      }

    }

    /* Put the bitarrays in the bitmap index */
    bitmapindex = new BitmapIndex64(tablename, "");

    bitmapindex.put("B0", new bitArray64(wordsNeeded));
    bitArray64 all1s = new bitArray64(wordsNeeded);
    all1s.maxPos = wordsNeeded;
    for (int w = 0; w < wordsNeeded; w++) {
      // all1s.vec[w] = 0x7FFFFFFF;
      all1s.vec[w] = 0x7FFFFFFFFFFFFFFFL;

    }

    bitmapindex.put("B1", all1s);
    // for (int a = 0; a < attribs; a++) {
    // for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
    // bitmapData[a][c].setCount();
    // bitmapindex.put(a + "_" + c, bitmapData[a][c]);
    // }
    // }
    
    for (int a = 0; a < attribs; a++) {
      BsiAttributeVerb bsi = new BsiAttributeVerb();
      for (int c = 0; c < Integer.toBinaryString(maxPerAtt[a]).length(); c++) {
        bsi.add(bitmapData[a][c]);
        // System.out
        // .println("bsi before writing: " + c + "  " + bitmapData[a][c]);
        // System.out.println("bsi before writing: " + c + "  " + bsi.bsi[c]);
        bitmapData[a][c].setCount();
        bitmapindex.put(a + "_" + c, bitmapData[a][c]);

      }
      FileOutputStream out = new FileOutputStream(fileOut + "_" + a);
      ObjectOutputStream oout = new ObjectOutputStream(out);
      oout.writeObject(bsi);
      oout.close();

    }

  }

  public static void createAllZeros() {
    bitmapindex = new BitmapIndex64(tablename, "");
    int wordsNeeded = (int) Math.ceil((double) rows / WORD);
    bitmapindex.put("B0", new bitArray64(wordsNeeded));
    bitArray64 all1s = new bitArray64(wordsNeeded);
    all1s.maxPos = wordsNeeded;
    for (int w = 0; w < wordsNeeded; w++) {
      // all1s.vec[w] = 0x7FFFFFFF;
      all1s.vec[w] = 0x7FFFFFFFFFFFFFFFL;

    }

    bitmapindex.put("B1", all1s);
  }

  public static void readBitmapsFromDisk(String fileName, int attNum)
      throws ClassNotFoundException, IOException {

    for (int i = 0; i < attNum; i++) {
      FileInputStream fin = new FileInputStream(fileName + "_" + i);
      ObjectInputStream ois = new ObjectInputStream(fin);
      bitSlices[i] = (BsiAttributeVerb) ois.readObject();
      // bsiBitmaps.put(Integer.toString(i), (BsiAttribute) ois.readObject());
      ois.close();
      // System.out.println("Checking if loading is correct: " + i + " "
      // + bitSlices[i].bsi[0].toString() + "  the size is: "
      // + bitSlices[i].size);
    }

  }

  public static void boolean_queries() {
    boolean[][] queries = queryGenerator.query_boolean(1000, 5);
    
    // create res with all zeros
    // bitmapindex.bitmaps.put("res_0", bitmapindex.bitmaps.get("B0"));

    for (int i = 0; i < queries.length; i++) {
      for (int j = 0; j < queries[i].length; j++) {
        if (queries[i][j]) {
          bitmapindex.SUM_BSI(Integer.toString(j), 10, "res", 10, "res");
        }

      }
    }
  }

  public static void one_decimal_queries() {



    int j, k, i, p = 0;

    for (i = 0; i < queries.length; i++) {
      p = 0;
      for (j = 0; j < queries[0].length; j++) {
      k = 0;
      while ((queries[i][j] >> k) > 0) { // iterate to shift.. this is for
                                         // integer weights
          if (((queries[i][j] >> k) & 1) == 1) {

            p =
                bitmapindex.SUM_BSI_pref(Integer.toString(j), 10, "res", p,
                    "res", k);

        }

        k++;

      }
    }
      topPref.bitmapindex.topKMax("res", p, 10, "top");
      // bitmapindex.clearResultsBitmap();


      // System.out.println(topPref.bitmapindex.bitmaps.get("top"));
      // topPref.bitmapindex.bitmaps.remove("res");
      // topPref.bitmapindex.bitmaps.remove("top");

    }


  }

  public static void run_queries(){
    int i,j,k;

    for (i = 0; i < queries.length; i++) {
      BsiAttributeVerb res = new BsiAttributeVerb();
      for (j = 0; j < queries[0].length; j++) {
        k=0;
        while ((queries[i][j] >> k) > 0) {
          if (((queries[i][j] >> k) & 1) == 1){
                res.BSI_SUM(bitSlices[j], k);
          }
          k++;
        }
      }
      bitArray64 top = res.topKMax(20);
    }
    
  }

  public static void run_queries2() {
    int i, j, k;

    for (i = 0; i < queries.length; i++) {
      BsiAttributeVerb res = new BsiAttributeVerb();
      for (j = 0; j < queries[0].length; j++) {
        BsiAttributeVerb multiRes = new BsiAttributeVerb();
        k = 0;
        while ((queries[i][j] >> k) > 0) {
          if (((queries[i][j] >> k) & 1) == 1) {
            multiRes.BSI_SUM(bitSlices[j], k);
          }
          k++;
        }
        if (multiRes.bsi[0] != null)
        res.BSI_SUM(multiRes, 0);

      }

      bitArray64 top = res.topKMax(20);

    }
  }

  public static void readQueries (int nQueries, String filename) {
        if (!filename.contains(".")) {
            filename=filename+".txt";
        }   
        queries = new int[nQueries][attribs];
        FileInputStream fin;		
        int seq = 0;
        try
        {
            // Open an input stream
            fin = new FileInputStream (filename);
            // Read a line of text
            DataInputStream input = new DataInputStream(fin);
            String line = input.readLine();
            while (line!=null && line.compareTo("")!=0) {
                //System.out.println(line);
                seq++;
                StringTokenizer strT;
                strT = new StringTokenizer(line,",\t");                                
                int attrNumber = 0;
                while (strT.hasMoreTokens() && attrNumber<attribs) {
                    int f = Integer.parseInt(strT.nextToken());                                        
                    queries[seq-1][attrNumber] = f;
                    attrNumber++;                
                }                         
                if (seq%1000==0)
                    genRpt: System.out.println("Line "+seq+": "+line);
                line = input.readLine();
            }
            //rows = seq;
            // Close our input stream
            fin.close();		       
        // Catches any error conditions
        } catch (IOException e) {
            System.err.println ("Unable to read from file");                    
            e.printStackTrace();
        }                         
    }
        
        /*MAIN FROM GHEORGHI*/
        /*
  public static void main(String[] args) throws IOException,
      ClassNotFoundException {
    readRawData("D:\\dblab\\bitmapNN\\prefQueries\\uniform_a10_r100k_c1000");
    queryfile = "D:\\dblab\\bitmapNN\\prefQueries\\qqq";
    readQueries(1000);

    buildBitmaps_Verbatim("uniform");
    readBitmapsFromDisk("uniform", 10);
    createAllZeros();
    // System.out.println(bitmapindex.bitmaps.get("0_0"));
    // System.out.println(bitmapindex.bitmaps.get("0_3"));
    // bitmapindex.SUM_BSI("0", 10, "1", 10, "result");
    // bitmapindex.SUM_BSI("2", 10, "result", 10, "result");
    // System.out.println(bitmapindex.bitmaps.get("result_1"));
    // queries = queryGenerator.query_int(5, 5);
    System.out.println("Done generating queries");


    int taskSize = 100;
    int numTasks = queries.length / taskSize;
    System.out.println(numTasks);

    ForkJoinPool pool = new ForkJoinPool();


    long time = 0;
    long startTime = System.nanoTime();
      // pool.invoke(new PerformDecimalQuery(6));
    // one_decimal_queries();
    run_queries();
    // one_decimal_queries();
    // pool.invoke(task1);
    // one_decimal_queries();
      // // for (int i = 0; i < numTasks; i++)
    // pool.invoke(new PerformDecimalQuery(i, taskSize));

      // This is for fork-join... uses all CPUs

      // for (int i = 0; i < 1; i++) {
      // pool.invoke(new PerformDecimalQuery(taskSize, i * 1000, (i + 1) *
      // 1000));
      // bitmapindex.clearResultsBitmap();
      // }

    // pool.shutdown();

      // boolean_queries();
    // bitmapindex.topKMax_bnn("res", 31, 104, "top");
    // System.out.println(bitmapindex.bitmaps.get("res_7"));


    // System.out.println(bitmapindex.bitmaps.get("top"));
    time = System.nanoTime() - startTime;
    System.out.println("TIme:  " + (double)time/1000000);


    // experimentsSuite();
  }
*/
  /*MAIN FROM GHEORGHI*/
  /*
public static void main(String[] args) throws IOException,
      ClassNotFoundException {
     
    String foldername = "D:\\dblab\\research\\prefQueries\\data\\";
    
    
//    Test data    
//    rows = 100;
//    attribs = 3;    
//    queryfile = foldername+ "uniform_a3_10query";
//    readQueries (10);
//    
//    readRawData(foldername + "uniform_a3_r100_c11");
//    //buildBitmaps_Verbatim(foldername+"bitmaps\\test"); 
//    readBitmapsFromDisk(foldername+"bitmaps\\test", attribs);
//    createAllZeros();
    
    
//    100K data
      
    rows = 100000;
    attribs = 10;
    readRawData(foldername + "uniform_a10_r100k_c1000");
    //buildBitmaps_Verbatim(foldername+"bitmaps\\uniform");
    readBitmapsFromDisk(foldername+"bitmaps\\uniform", attribs);
    createAllZeros();
    queryfile = foldername+ "a10_1Kquery";
    readQueries (1000);
    
    
    int taskSize = 100;
    int numTasks = queries.length / taskSize;
    System.out.println(numTasks);

    ForkJoinPool pool = new ForkJoinPool();

    long time = 0;
    long startTime = System.nanoTime();
      // pool.invoke(new PerformDecimalQuery(6));
    // one_decimal_queries();
    run_queries();
    time = System.nanoTime() - startTime;
    System.out.println("TIme:  " + (double)time/1000000);
  }
*/
  
  public static bitArray64 executeQuery(int topK, int[] query) {      
      int k, p = 0, j=0;
      BsiAttributeVerb res = null, temp;//new BsiAttribute();     
      for (j = 0; j < query.length; j++) 
      {
          if (query[j]>0) {
        temp = bitSlices[j].multiply(query[j]);
              if (res==null) {
                  res = temp;
              } else {
          res = res.SUM_BSI(temp);
              }            
              //System.out.println("SUM "+j+": "+res.SUM());
          }                  
      }
      //System.out.println("SUM: "+res.SUM());
    bitArray64 tRes = null;
    if (res != null)
      tRes = res.topKMax(topK);
      //bitmapindex.topKMax("res", p, topK, "top");
      //System.out.println(tRes.toString());
      return tRes;
  }
  
    public static bitArray64 executeQuery_inplace(int topK, int[] query) {      
      int k, p = 0, j=0;
      BsiAttributeVerb res = null, temp;//new BsiAttribute();     
      for (j = 0; j < query.length; j++) 
      {
          if (query[j]>0) {
           //temp = bitSlices[j].multiply_inPlace(query[j]);
        	  temp=bitSlices[j];
              if (res==null) {
                  res = temp;
              } else {
                res = res.SUM_BSI_inPlace(temp);
              }            
          //    System.out.println("SUM "+j+": "+res.SUM());
          }                  
      }
      //System.out.println("SUM: "+res.SUM());
    bitArray64 tRes = null;
    if (res != null)
      tRes = res.topKMax(topK);
      //bitmapindex.topKMax("res", p, topK, "top");
      //System.out.println(tRes.toString());
      return tRes;
  }
    
 public static double run_queries(int topK) {
     if (genRpt) {
      logFile.initializeLogging(outputFile+"_mine.rpt");
      logFile.log("QueryId,Time,RowIds\n");
     }
      bitArray64 b = null;
      double time=0, start, end;
        int j=0, k, i, p = 0;
        for (j = 0; j < queries.length; j++) 
        {
            start = System.nanoTime();
            b = executeQuery(topK, queries[j]);
            end=System.nanoTime();            
            time+= (end-start);
//            if (j<10) {
//                System.out.println(b.getIDs());
//            }
            if (genRpt) {//Print top K to the file                 
               logFile.log((j+1)+","+(end-start)/1000000+","+b.getIDs()+"\n");                       
            }
        }        
      // bitmapindex.clearResultsBitmap();
      //System.out.println(topPref.bitmapindex.bitmaps.get("top"));
      // topPref.bitmapindex.bitmaps.remove("res");
      // topPref.bitmapindex.bitmaps.remove("top");    
        if (genRpt) {
            logFile.closeFiles();
        }

        return time;

  }

 public static double run_queries_inplace(int topK) {
     if (genRpt) {
      logFile.initializeLogging(outputFile+"_inplace.rpt");
      logFile.log("QueryId,Time,RowIds\n");
     }
     
      bitArray64 b = null;
      double time=0, start, end;
        int j=0, k, i, p = 0;
        for (j = 0; j < queries.length; j++) 
        {
            start = System.nanoTime();
            b = executeQuery_inplace(topK, queries[j]);
            end=System.nanoTime();            
            time+= (end-start);
//            if (j<10) {
//                System.out.println(b.getIDs());
//            }
            if (genRpt) {//Print top K to the file                 
               logFile.log((j+1)+","+(end-start)/1000000+","+b.getIDs()+"\n");                 
            }
       }        
      // bitmapindex.clearResultsBitmap();
      //System.out.println(topPref.bitmapindex.bitmaps.get("top"));
      // topPref.bitmapindex.bitmaps.remove("res");
      // topPref.bitmapindex.bitmaps.remove("top");    
              // System.out.println("Query "+(i+1)+": \t"+(endTime-startTime));
          
        if (genRpt) {
            logFile.closeFiles();
        }
    return time;

  }
   

public static void main(String[] args) throws IOException,
      ClassNotFoundException {    
      
    String foldername = "/user/g/gguzun/opt/data/";
    
    /*Test data*/
    /*
     * rows = 100; attribs = 3; queryfile = foldername+ "uniform_a3_10query";
     * readQueries (10); int taskSize = 100; int numTasks = queries.length /
     * taskSize; System.out.println(numTasks); readRawData(foldername +
     * "uniform_a3_r100_c11");
     * //buildBitmaps_Verbatim(foldername+"bitmaps\\test");
     * readBitmapsFromDisk(foldername+"bitmaps\\test", attribs);
     * createAllZeros();
     */
    
    /*100K data*/       


		rows = 10000000;
		attribs = 5;

    

		 readRawData(foldername + "generated/uniform_c1m_a5_r10m");
		 buildBitmaps_Verbatim(foldername + "bitmaps/uniform_c1m_a5_r10m");
		
		readBitmapsFromDisk(foldername + "bitmaps/uniform_c1m_a5_r10m",
        attribs);
    createAllZeros();



		queryfile = foldername + "queries/a5_r1k_c10.txt";
		readQueries(20);
    // System.out.println("sparse 10");
		for (int i = 0; i < 15; i++) {

      BsiAttributeVerb.wordsNeeded = (int) Math.ceil((double) rows / WORD);
      // System.out.println("Mine: ");
      double time0 = 0;
      // time0 = run_queries(5);
      // System.out.println("TIme:  " + (double) time0 / 1000000);

//			System.out.println("Gheorghi's: ");
//      long time = 0;
//			long startTime = System.nanoTime();
//			run_queries2();
//			time = System.nanoTime() - startTime;
//			System.out.println("TIme:  " + (double) time / 1000000);

      System.out.println("In place: ");
			time0 = run_queries_inplace(20);
      System.out.println("TIme:  " + (double) time0 / 1000000);

    }
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory=runtime.totalMemory()-runtime.freeMemory();
		System.out.println("Used memory in bytes: "+memory);
    /*
     * queryfile = foldername + "queries\\a1K_r1K_100"; readQueries(10);
     * System.out.println("sparse 100"); for (int i = 0; i < 5; i++) {
     * 
     * BsiAttribute.wordsNeeded = (int) Math.ceil((double) rows / WORD);
     * System.out.println("Mine: "); double time0 = 0; time0 = run_queries(20);
     * System.out.println("TIme:  " + (double) time0 / 1000000);
     * 
     * System.out.println("Gheorghi's: "); long time = 0; long startTime =
     * System.nanoTime(); run_queries2(); time = System.nanoTime() - startTime;
     * System.out.println("TIme:  " + (double) time / 1000000);
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
     * System.out.println("Mine: "); double time0 = 0; time0 = run_queries(20);
     * System.out.println("TIme:  " + (double) time0 / 1000000);
     * 
     * System.out.println("Gheorghi's: "); long time = 0; long startTime =
     * System.nanoTime(); run_queries2(); time = System.nanoTime() - startTime;
     * System.out.println("TIme:  " + (double) time / 1000000);
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
     * System.out.println("Mine: "); double time0 = 0; time0 = run_queries(20);
     * System.out.println("TIme:  " + (double) time0 / 1000000);
     * 
     * System.out.println("Gheorghi's: "); long time = 0; long startTime =
     * System.nanoTime(); run_queries2(); time = System.nanoTime() - startTime;
     * System.out.println("TIme:  " + (double) time / 1000000);
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
     * System.out.println("Mine: "); double time0 = 0; time0 = run_queries(20);
     * System.out.println("TIme:  " + (double) time0 / 1000000);
     * 
     * System.out.println("Gheorghi's: "); long time = 0; long startTime =
     * System.nanoTime(); run_queries2(); time = System.nanoTime() - startTime;
     * System.out.println("TIme:  " + (double) time / 1000000);
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
     * System.out.println("Mine: "); double time0 = 0; time0 = run_queries(20);
     * System.out.println("TIme:  " + (double) time0 / 1000000);
     * 
     * System.out.println("Gheorghi's: "); long time = 0; long startTime =
     * System.nanoTime(); run_queries2(); time = System.nanoTime() - startTime;
     * System.out.println("TIme:  " + (double) time / 1000000);
     * 
     * System.out.println("In place: "); time0 = run_queries_inplace(20);
     * System.out.println("TIme:  " + (double) time0 / 1000000);
     * 
     * }
     */
  }

}
