package com.company.topK;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
class RankComparator implements Comparator<double[]>
{
    @Override
    public int compare(double[] x, double[] y) //The second element has the rank (sorts from min to max - min scores are deleted when full))
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        if (x[1] < y[1])
        {
            return -1;
        } 
        if (x[1] > y[1])
        {
            return 1;
        } 
        return 0;
    }
}

/**
 *
 * @author gcanahuate
 */
public class PrefQueriesScan { 
    logger logFile = new logger();
   
     String tablename = "intUniform";
     String queryfile = "";
     static int[][] rawDataInt;
     double[][] rawDataDouble;
     double[][] queries;
     static int rows = 64;
     static int attribs = 50;
     boolean genRpt = true; //Set this to false to not print the results
     String outputFile = "";
                   
     public void readRawData(boolean asInt) {//Data needs to be integer and has no id
        String filename = tablename;
        if (!filename.contains(".")) {
            filename=filename+".txt";
        }   
        if (asInt) {
            rawDataInt = new int[rows][attribs+1];//id+attribs
        } else {
            rawDataDouble = new double[rows][attribs+1];//id+attribs
        }
            
        FileInputStream fin;		
        int seq = 0;
        try
        {
            // Open an input stream
            fin = new FileInputStream (filename);
            // Read a line of text
            DataInputStream input = new DataInputStream(fin);
            String line = input.readLine();
      while (line != null && line.compareTo("") != 0 && seq < this.rows) {
                //System.out.println(line);
                seq++;
                StringTokenizer strT;
                strT = new StringTokenizer(line,",\t");
                int ncount = strT.countTokens();
                int id=seq-1;
                int attrNumber = 0;
                int toks = ncount; //int toks = attribs;
                if (asInt) {
                    rawDataInt[seq-1][0] = id;
          // while (strT.hasMoreTokens() && toks>0) {
          for (int i = 1; i < attribs; i++) {
                        int f = Integer.parseInt(strT.nextToken());                    
                        attrNumber++;
                        toks--;
                        rawDataInt[seq-1][attrNumber] = f;
                    }                         
                } else {
                    rawDataDouble[seq-1][0] = id;
                    while (strT.hasMoreTokens() && toks>0) {
                        double f = Double.parseDouble(strT.nextToken());                    
                        attrNumber++;
                        toks--;
                        rawDataDouble[seq-1][attrNumber] = f;
                    }                         
                }
                //else {
                //    System.out.println("No class attribute");
                //    break;
                //}
                if (seq%100000==0)
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
  
    public void readQueries (int nQueries) {
        String filename = queryfile;
        if (!filename.contains(".")) {
            filename=filename+".txt";
        }   
        queries = new double[nQueries][attribs];
        FileInputStream fin;		
        int seq = 0;
        try
        {
            // Open an input stream
            fin = new FileInputStream (filename);
            // Read a line of text
            DataInputStream input = new DataInputStream(fin);
            String line = input.readLine();
      while (line != null && line.compareTo("") != 0 && seq < nQueries) {
                //System.out.println(line);
                seq++;
                StringTokenizer strT;
                strT = new StringTokenizer(line,",\t");                                
                int attrNumber = 0;
                while (strT.hasMoreTokens() && attrNumber<attribs) {
                    double f = Double.parseDouble(strT.nextToken());                                        
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
    public double getScore (int[] data, double[] q) {
        double score=0;        
        for (int i=0; i<data.length-1; i++) {
            score+= data[i+1]*q[i];
        }
        return score;
    }
    public double getScore (double[] data, double[] q) {
        double score=0;        
        for (int i=0; i<data.length-1; i++) {
            score+= data[i+1]*q[i];
        }
        return score;
    }        
    
    public PriorityQueue<double[]> executeQueryOverInt(int k, double[] query) {
        Comparator<double[]> comparator = new RankComparator();
        PriorityQueue<double[]> topK = new PriorityQueue<double[]>(k, comparator);
        
        double kthscore = Double.MIN_VALUE;
        double thisScore = 0;       
        for (int i=0; i<k; i++) {  
            double [] candidate = new double[2];
            thisScore = getScore(rawDataInt[i],query);
            candidate[0]=i+1;
            candidate[1] = thisScore;            
            topK.offer(candidate);
        }     
        kthscore = topK.peek()[1];
        for (int i=k; i<rawDataInt.length; i++) {
            thisScore = getScore(rawDataInt[i],query); 
            double [] candidate = new double[2];
            candidate[0]=i+1;
            candidate[1] = thisScore;
            if (thisScore > kthscore) { //This is a topK, have to remove the biggest
                double[] victim = topK.poll();
                while (topK.size()+1>k && victim[1] == topK.peek()[1]) {
                    topK.poll();
                }
                topK.offer(candidate);
                kthscore = topK.peek()[1];
            } else if (thisScore == kthscore) {
                topK.offer(candidate);               
            }
        }    
        return topK;
    }
            
    public PriorityQueue<double[]> executeQueryOverDouble(int k, double[] query) {
        Comparator<double[]> comparator = new RankComparator();
        PriorityQueue<double[]> topK = new PriorityQueue<double[]>(k, comparator);
        
        double kthscore = Double.MIN_VALUE;
        double thisScore = 0;       
        for (int i=0; i<k; i++) {  
            double [] candidate = new double[2];
            thisScore = getScore(rawDataDouble[i],query);
            candidate[0]=i+1;
            candidate[1] = thisScore;            
            topK.offer(candidate);
        }     
        kthscore = topK.peek()[1];
        for (int i=k; i<rawDataDouble.length; i++) {
            thisScore = getScore(rawDataDouble[i],query); 
            double [] candidate = new double[2];
            candidate[0]=i+1;
            candidate[1] = thisScore;
            if (thisScore > kthscore) { //This is a topK, have to remove the biggest
                double[] victim = topK.poll();
                while (topK.size()+1>k && victim[1] == topK.peek()[1]) {
                    topK.poll();
                }
                topK.offer(candidate);
                kthscore = topK.peek()[1];
            } else if (thisScore == kthscore) {
                topK.offer(candidate);               
            }
        }    
        return topK;
    }
      
    public double executeQueries (int k, boolean overInt) {
        double startTime=0, endTime=0, totTime=0;
        
        if (genRpt) {
            logFile.initializeLogging(outputFile+".rpt");
            logFile.log("QueryId,RankId,RowId,Score\n");
        }
        PriorityQueue<double[]> topK;
        for (int i=0; i<queries.length; i++) {
            if (overInt) {
                startTime = System.nanoTime();
                topK = executeQueryOverInt(k, queries[i]);            
                endTime = System.nanoTime();
                totTime+= endTime-startTime;
            } else {
                startTime = System.nanoTime();
                topK = executeQueryOverDouble(k, queries[i]);            
                endTime = System.nanoTime();
                totTime+= endTime-startTime;
            }                        
      // System.out.println("Query "+(i+1)+": \t"+(endTime-startTime));
            if (genRpt) {//Print top K to the file
                 int k_wties = topK.size();
                 double[][] scores = new double[k_wties][2];
                 for (int j=0; j<k_wties; j++) {
                     scores[k_wties-j-1] = topK.poll();                     
                 }
                 for (int j=0; j<k_wties; j++) {
                    logFile.log((i+1)+","+(j+1)+","+(int)(scores[j][0])+","+scores[j][1]+"\n");
                 }
            }
        }
    System.out.println("Tot Query Time (ns): \t" + (double) totTime / 1000000);
        if (genRpt) {
            logFile.closeFiles();
        }
        return totTime;
    }
    
    public static long runSum (int attributes, int rows) {
        long startTime=0, endTime=0, totTime=0;
        int[] sum = new int[rows];
      
        startTime = System.nanoTime();
        for (int i=0; i<attributes; i++) {
           
               
               // topK = executeQueryOverInt(k, queries[i]);  
                for(int j=0;j<rows; j++){
                	sum[j]=rawDataInt[j][i]+sum[j];
                }
                
                
               
                                   
      // System.out.println("Query "+(i+1)+": \t"+(endTime-startTime));
           
        }
        long total =0;
        for(int i=0;i<sum.length;i++)
        	total =total+sum[i];
        	
        
        endTime = System.nanoTime();
        totTime+= endTime-startTime;
        System.out.println("sum= "+total);
   
        return totTime;
    }
      
    public static void main(String[] args) {                
        double qtns=0;

    String foldername = "D:\\dblab\\research\\prefQueries\\data\\";
        PrefQueriesScan pqs = new PrefQueriesScan();
    pqs.tablename = foldername + "generated\\test1000_zipf1";
    pqs.rows = 64;
    pqs.attribs = 50;
    pqs.readRawData(true); // As integers

    // pqs.outputFile = foldername + "queriesAsInt";
    // pqs.genRpt=true;
    /*
     * pqs.queryfile = foldername + "queries\\a20_r1M_c1K"; pqs.readQueries(10);
     * // Number of queries for (int i = 0; i < 3; i++) qtns =
     * pqs.executeQueries(20, true); // top K, overInt
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_100"; pqs.readQueries(10);
     * // Number of queries for (int i = 0; i < 3; i++) qtns =
     * pqs.executeQueries(20, true); // top K, overInt
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_200"; pqs.readQueries(10);
     * // Number of queries for (int i = 0; i < 3; i++) qtns =
     * pqs.executeQueries(20, true); // top K, overInt
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_500"; pqs.readQueries(10);
     * // Number of queries for (int i = 0; i < 3; i++) qtns =
     * pqs.executeQueries(20, true); // top K, overInt
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_1000";
     * pqs.readQueries(10); // Number of queries for (int i = 0; i < 3; i++)
     * qtns = pqs.executeQueries(20, true); // top K, overInt
     */

    pqs.queryfile = foldername + "queries\\a5_r1k_c10";
    pqs.readQueries(5); // Number of queries

//    System.out.println("Top 5: ");
//    for (int i = 0; i < 5; i++)
//      qtns = pqs.executeQueries(20, true); // top K, overInt
    long timeSum=0;
 	// System.out.println("sparse 10");
 	for (int i = 0; i < 10; i++) {

 		//BsiAttributeVerb.wordsNeeded = (int) Math.ceil((double) rows / WORD);
 		// System.out.println("Mine: ");
 		long time0 = 0;
 		// time0 = run_queries(5);
 		// System.out.println("TIme:  " + (double) time0 / 1000000);

 		

 		System.out.println("Summation time:  ");
 		
// 		time0 = run_queries_inplace(10,verbatim);
 		time0=runSum(attribs,rows);
 		
 		
 		System.out.println("Time:  " + (double) time0 / 1000000);
 		if(i>0)
 			timeSum+=time0;
 		
// 		System.out.println("Gheorghi's: ");
// 		long time = 0;
// 		long startTime = System.nanoTime();
// 		run_queries2();
// 		time = System.nanoTime() - startTime;
// 		System.out.println("TIme:  " + (double) time / 1000000);

 	}
 	System.out.println("AvgTime:  " + (double) timeSum / 9000000);
 	
 	
 	Runtime runtime = Runtime.getRuntime();
 	runtime.gc();
 	long memory=runtime.totalMemory()-runtime.freeMemory();
 	System.out.println("Used memory in bytes: "+memory);

//    System.out.println("Top 10: ");
//    for (int i = 0; i < 5; i++)
//      qtns = pqs.executeQueries(10, true); // top K, overInt
//
//    System.out.println("Top 20: ");
//    for (int i = 0; i < 10; i++)
//      qtns = pqs.executeQueries(20, true); // top K, overInt
//
//    System.out.println("Top 50: ");
//    for (int i = 0; i < 5; i++)
//      qtns = pqs.executeQueries(50, true); // top K, overInt
//
//    System.out.println("Top 100: ");
//    for (int i = 0; i < 5; i++)
//      qtns = pqs.executeQueries(100, true); // top K, overInt
//
//    System.out.println("Top 1000: ");
//    for (int i = 0; i < 5; i++)
//      qtns = pqs.executeQueries(1000, true); // top K, overInt


    // pqs.readRawData(false); //As doubles
    // pqs.readQueries(10);
    // pqs.readQueries(1000); // Number of queries
    // pqs.outputFile = foldername+"queriesAsDouble";
    // pqs.genRpt=true;
    // qtns = pqs.executeQueries (10, false); //top K, overDouble
      
    /*
     * pqs.queryfile = foldername + "all1s_10a"; pqs.outputFile =
     * pqs.queryfile+"_seqScan"; pqs.queries=new double[1][pqs.attribs]; for
     * (int i=0; i<pqs.attribs; i++) { pqs.queries[0][i]=1; } qtns =
     * pqs.executeQueries(10, true); qtns = pqs.executeQueries(10, true); //
     * pqs.genRpt=false; // qtns = pqs.executeQueries(10, true); // top K,
     * overInt
     */
    }

}
