/*
 * To change this template, choose Tools | Templates

 * and open the template in the editor.
 */
package com.company.topK;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
/**
 *
 * @author gcanahuate
 */
public class PrefQueriesSTA { 
     logger logFile = new logger();

     String tablename = "intUniform";
     String queryfile = "";
     int[][] sortIndex;
     double[][] rawDataDouble;
     double[][] R;
     double[][] queries;
     
  int rows = 10000000;
  int attribs = 20;
     boolean genRpt = true; //Set this to false to not print the results
     String outputFile = "";
       
     public void readRawData( ) {//Data goes to double and has no id
        String filename = tablename;
        if (!filename.contains(".")) {
            filename=filename+".txt";
        }   
        System.out.println("Reading file "+filename);
        rawDataDouble = new double[rows][attribs+2];//id+attribs+list
            
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
                    rawDataDouble[seq-1][0] = id;
        // while (strT.hasMoreTokens() && toks > 0) {
        for (int i = 1; i < attribs; i++) {
                        double f = Double.parseDouble(strT.nextToken());                    
                        attrNumber++;
                        toks--;
                        rawDataDouble[seq-1][attrNumber] = f;
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
  
    public void buildIndex () { //Generates the sortIndex (rows x attrib), in descending order...only stores the rowIds
        sortIndex = new int[rows][attribs];
        for (int i=1; i<attribs+1; i++) {
            double[][] column = new double[rows][2];
            for (int j=0; j<rows; j++) {
                column[j][0] = j;
                column[j][1] = rawDataDouble[j][i];
            }
            Arrays.sort(column, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                return ((Double) o2[1]).compareTo(o1[1]);
            }
            });

            for (int j=0; j<rows; j++) {
                sortIndex[j][i-1]=(int)column[j][0];
            }
        }
        buildRPrime();
    }
    
    public void buildRPrime() {
        R = new double[rows][attribs+2]; //The id + attribs + list
        byte[] alreadyCheck = new byte[rows];
        int i=0;
        int pos = 0;//this is pos
        while (i<rawDataDouble.length) {
            for (int a=0; a<attribs; a++) {//Go to the ith element of each list and compute the score
                if (alreadyCheck[sortIndex[i][a]]==0) {
                    R[pos]=rawDataDouble[sortIndex[i][a]];
                    R[pos][attribs+1] = a;
                    alreadyCheck[sortIndex[i][a]]=1;
                    pos++;
                }               
             }
            i++;                          
    }
    sortIndex = null;
    rawDataDouble = null;
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
    
  public double getMax(int attrib, HashMap h) {
    double max = -1;
    double thisVal;
    Iterator iter = h.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry mEntry = (Map.Entry) iter.next();
      thisVal = ((double[]) mEntry.getValue())[attrib];
      if (max < thisVal) {
        max = thisVal;
      }
    }
    return max;
    }
    
     public void readIndex (String filename) {
        sortIndex = new int[rows][attribs];
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
                    sortIndex[seq-1][attrNumber] = f;
                    attrNumber++;                
                }                         
                if (seq%10000==0)
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
  
    public PriorityQueue<double[]> executeQueryOverDouble(int k, double[] query) {
        Comparator<double[]> comparator = new RankComparator();
        PriorityQueue<double[]> topK = new PriorityQueue<double[]>(k, comparator);
        double[] T = new double[attribs];
    // double[] maxVals = new double[attribs];
        for (int b=0; b<attribs; b++) {
            T[b]=-1; //To indicate it is empty
      // maxVals[b]=Double.MIN_VALUE;
        }
        double kthscore = Double.MIN_VALUE;
        double TA = Double.MAX_VALUE;
    HashMap<Integer, double[]> DBG = new HashMap<>(attribs);

        int i=0;
        while (TA > kthscore && i<R.length) {
            double [] candidate = new double[2];
            candidate[0] = R[i][0];
            candidate[1] = getScore(R[i], query);
            if (topK.size()<k) {
                  topK.offer(candidate);
                  kthscore = topK.peek()[1];
            } else if (kthscore < candidate[1]) {
                  double[] victim = topK.poll();
                  while (topK.size()+1>k && victim[1] == topK.peek()[1]) {
                      topK.poll();
                  }
                  topK.offer(candidate);
                  kthscore = topK.peek()[1];
            } else if (kthscore == candidate[1]) {
                  topK.offer(candidate);
            }
            //Update TA
            int att = (int)(R[i][attribs+1]);
      // for (int a=0; a<attribs; a++) {
      // if (R[i][a+1] > maxVals[a]) {
      // maxVals[a]=R[i][a+1];
      // }
      // }
            if (!(DBG.containsKey(att))) {
                DBG.put(att,R[i]);
                T[att] = R[i][att+1];
            } else {
                TA=0;
                for (int a=0; a<attribs; a++) {
                    if (T[a]<0) {
            T[a] = getMax(a + 1, DBG);
                    }
          if (query[a] > 0)
                    TA+= query[a]*(T[a]);
                }
                DBG.clear(); DBG.put(att, R[i]);
            }
            i++;                          
        }    
        return topK;
    }
      
    public double getScore (double[] data, double[] q) {
        double score=0; 
        for (int i=0; i<data.length-2; i++) {
            score+= data[i+1]*q[i];
        }
        return score;
    }
    
    public double executeQueries (int k) {
        double startTime=0, endTime=0, totTime=0;
        if (genRpt) {
            logFile.initializeLogging(outputFile+"_sta.rpt");
            logFile.log("QueryId,RankId,RowId,Score\n");
        }        
        PriorityQueue<double[]> topK;
        for (int i=0; i<queries.length; i++) {
                startTime = System.nanoTime();
               
                topK = executeQueryOverDouble(k, queries[i]);            
                endTime = System.nanoTime();
                totTime+= endTime-startTime;
            
      // System.out.println("Query "+(i+1)+": \t"+(endTime-startTime));
            if (genRpt) {//Print top K to the file
                 int k_wties = topK.size();
                 double[][] scores = new double[k_wties][2];
                 for (int j=0; j<k_wties; j++) {
                     scores[k_wties-j-1] = topK.poll();                     
                 }
                 for (int j=0; j<k_wties; j++) {
                    logFile.log((i+1)+","+(j+1)+","+(int)(scores[j][0]+1)+","+scores[j][1]+"\n");
                 }
            }
        }
    System.out.println("Tot Query Time (ns): \t" + (double) totTime / 1000000);
        if (genRpt) {
            logFile.closeFiles();
        }
        return totTime;
    }
      
       
    public void writeIndexToFile (String output) {
        logFile.initializeLogging(output);
        for (int i=0; i<rows; i++) {
            logFile.log(Integer.toString((int)(R[i][0])));
            for (int j=1; j<attribs+2; j++) {
               logFile.log(","+R[i][j]);
            }
            logFile.log("\n");
        }
        logFile.closeFiles();
    }
     
    public static void main(String[] args) {                
        double qtns=0;
    String foldername = "D:\\dblab\\research\\prefQueries\\data\\";
    // String foldername = "D:\\dblab\\bitmapNN\\prefQueries\\";
        PrefQueriesSTA pqs = new PrefQueriesSTA();
    // pqs.tablename = foldername + "generated\\zipf15_a20_r100K_c1k";
    pqs.tablename = foldername + "generated\\zipf1_a20_r10M_c1K";
    pqs.rows = 10000000;
    pqs.attribs = 20;

    pqs.readRawData(); // As doubles
    pqs.buildIndex(); // List of sorted ints;

    // pqs.writeIndexToFile(pqs.tablename + "_sortedIndex.txt");
    // pqs.readIndex(pqs.tablename + "_sortedIndex.txt"); // If the index has
    // been
    /*
     * pqs.queryfile = foldername + "queries\\a1K_r1K_10"; pqs.readQueries(10);
     * // Number of queries
     * 
     * System.out.println("TA 10"); pqs.genRpt = false;
     * 
     * for (int i = 0; i < 3; i++) qtns = pqs.executeQueries(20); // top K //
     * written to file
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_100"; pqs.readQueries(10);
     * // Number of queries
     * 
     * System.out.println("TA 100"); pqs.genRpt = false;
     * 
     * for (int i = 0; i < 3; i++) qtns = pqs.executeQueries(20); // top K
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_200"; pqs.readQueries(10);
     * // Number of queries
     * 
     * System.out.println("TA 200"); pqs.genRpt = false;
     * 
     * for (int i = 0; i < 3; i++) qtns = pqs.executeQueries(20); // top K
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_500"; pqs.readQueries(10);
     * // Number of queries
     * 
     * System.out.println("TA 500"); pqs.genRpt = false;
     * 
     * for (int i = 0; i < 3; i++) qtns = pqs.executeQueries(20); // top K
     * 
     * pqs.queryfile = foldername + "queries\\a1K_r1K_1000";
     * pqs.readQueries(10); // Number of queries
     * 
     * System.out.println("TA 1000"); pqs.genRpt = false;
     * 
     * for (int i = 0; i < 3; i++) qtns = pqs.executeQueries(20); // top K
     */

    pqs.queryfile = foldername + "queries\\a20_r1K_c10";
    pqs.readQueries(10); // Number of queries

    System.out.println("STA 1");
    pqs.genRpt = false;

    System.out.println("TOP 10: ");
    for (int i = 0; i < 5; i++)
      qtns = pqs.executeQueries(10); // top K // written to file

    System.out.println("TOP 20: ");
    for (int i = 0; i < 5; i++)
      qtns = pqs.executeQueries(20); // top K // written to file

    System.out.println("TOP 50: ");
    for (int i = 0; i < 5; i++)
      qtns = pqs.executeQueries(50); // top K // written to file

    System.out.println("TOP 100: ");
    for (int i = 0; i < 5; i++)
      qtns = pqs.executeQueries(100); // top K // written to file

    System.out.println("TOP 1000: ");
    for (int i = 0; i < 5; i++)
      qtns = pqs.executeQueries(1000); // top K // written to file


    /*
     * System.out.println("TOP 10: "); for (int i = 0; i < 5; i++) qtns =
     * pqs.executeQueries(10); // top K // written to file
     * 
     * System.out.println("TOP 20: "); for (int i = 0; i < 5; i++) qtns =
     * pqs.executeQueries(20); // top K // written to file
     * 
     * System.out.println("TOP 50: "); for (int i = 0; i < 5; i++) qtns =
     * pqs.executeQueries(50); // top K // written to file
     * 
     * System.out.println("TOP 100: "); for (int i = 0; i < 5; i++) qtns =
     * pqs.executeQueries(100); // top K // written to file
     * 
     * System.out.println("TOP 1000: "); for (int i = 0; i < 5; i++) qtns =
     * pqs.executeQueries(1000); // top K // written to file
     * 
     * System.out.println("TOP 10000: "); for (int i = 0; i < 5; i++) qtns =
     * pqs.executeQueries(10000); // top K // written to file
     */
    }




}
