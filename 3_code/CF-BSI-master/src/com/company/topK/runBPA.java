package com.company.topK;
public class runBPA {

  public static void main(String[] args) {
     
    MyInteger randomAccess = new MyInteger();
    MyInteger stopPosition = new MyInteger();
    MyInteger sortedAccess = new MyInteger();

    PrefQueriesTA pqs = new PrefQueriesTA();
    String foldername = "D:\\dblab\\research\\prefQueries\\data\\";
        
    pqs.tablename = foldername + "generated\\zipf1_a100_r100k_c1k";
    //pqs.tablename = "D:\\dblab\\research\\prefQueries\\data\\uniform_a10_r100k_c1000";
    //pqs.queryfile = "D:\\dblab\\research\\prefQueries\\data\\qqq";
    // pqs.queryfile = foldername + "queries\\coil";
    // pqs.outputFile = pqs.queryfile;
    pqs.attribs = 6;
    pqs.rows = 100000;
    pqs.readRawData();
    // pqs.readQueries(1000);
    int topK = 20;

    /*All 1 queries*/
    /*
    pqs.queryfile = foldername + "all1s_10a";
    pqs.outputFile = pqs.queryfile;
    pqs.queries=new double[1][pqs.attribs];
    for (int i=0; i<pqs.attribs; i++) {
        pqs.queries[0][i]=1;
    }
    * */

    Database d = new Database(pqs.attribs, pqs.rows, pqs.rawDataDouble);
   

    // d.printDatabase();

    TopkAlgorithms top = new TopkAlgorithms(d);

    AnswerElement[] answers = new AnswerElement[topK];

    long time = 0;
    long start=0, end=0;

    for (int i = 0; i < 10; i++) {

    start = System.nanoTime();
      top.FA(topK, stopPosition, sortedAccess, randomAccess, answers);
      System.out.println("FA time: " + (double) (System.nanoTime() - start)
        / 1000000);
    }
    // top.printAnswers(answers, topK);
    
    for (int i = 0; i < 10; i++) {

      start = System.nanoTime();
      top.TA(topK, stopPosition, sortedAccess, randomAccess, answers);
      System.out.println("TA time: " + (double) (System.nanoTime() - start)
          / 1000000);
    }
    // top.printAnswers(answers, topK);

    for (int i = 0; i < 10; i++) {

      start = System.nanoTime();
      top.BPA(topK, stopPosition, sortedAccess, randomAccess, answers);
      System.out.println("BPA time: " + (double) (System.nanoTime() - start)
          / 1000000);
    }
    // top.printAnswers(answers, topK);

    for (int i = 0; i < 10; i++) {

      start = System.nanoTime();
      top.BPA2(topK, stopPosition, sortedAccess, randomAccess, answers);
      System.out.println("BPA2 time: " + (double) (System.nanoTime() - start)
          / 1000000);
    }
    // top.printAnswers(answers, topK);

    /*
     * if (pqs.genRpt) {
     * pqs.logFile.initializeLogging(pqs.outputFile+"_TA.rpt");
     * pqs.logFile.log("QueryId,RankId,RowId,Score\n"); } for (int i = 0; i <
     * pqs.queries.length; i++) { start = System.nanoTime(); top.TA(topK,
     * stopPosition, sortedAccess, randomAccess, answers, pqs.queries[i]);
     * //top.TA(topK, stopPosition, sortedAccess, randomAccess, answers);
     * end=System.nanoTime(); time+= (end-start);
     * 
     * if (pqs.genRpt) { pqs.logFile.log(top.getAnswers(i+1, answers, topK)); }
     * } if (pqs.genRpt) { pqs.logFile.closeFiles(); }
     * System.out.println("TA Time : " + (double) (time)/ 1000000);
     * 
     * 
     * 
     * time=0; if (pqs.genRpt) {
     * pqs.logFile.initializeLogging(pqs.outputFile+"_BPA.rpt");
     * pqs.logFile.log("QueryId,RankId,RowId,Score\n"); } for (int i = 0; i <
     * pqs.queries.length; i++) { start = System.nanoTime(); top.BPA(topK,
     * stopPosition, sortedAccess, randomAccess, answers, pqs.queries[i]);
     * //top.BPA(topK, stopPosition, sortedAccess, randomAccess, answers);
     * end=System.nanoTime(); time+= (end-start);
     * 
     * if (pqs.genRpt) { pqs.logFile.log(top.getAnswers(i+1, answers, topK)); }
     * } if (pqs.genRpt) { pqs.logFile.closeFiles(); }
     * System.out.println("BPA Time : " + (double) (time)/ 1000000);
     * //top.printAnswers(answers, topK); // top.TA(5, stopPosition,
     * sortedAccess, randomAccess, answers);
     * 
     * // top.printAnswers(answers, 10); //top.BPA(5, stopPosition,
     * sortedAccess, randomAccess, answers);
     * 
     * // top.BPA2(10, stopPosition, sortedAccess, randomAccess, answers); //
     * top.FA(10, stopPosition, sortedAccess, randomAccess, answers);
     * 
     * // top.printAllOverallScores(); // d.printPositions();
     */



  }
}
