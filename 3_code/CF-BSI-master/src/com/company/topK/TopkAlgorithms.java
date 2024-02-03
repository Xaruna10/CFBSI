package com.company.topK;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 // Description : this class implements BPA, BPA2, TA and FA algorithms
 */

public class TopkAlgorithms {
  Database database;
  int m;
  int n;

  public TopkAlgorithms(Database d)
  {
    database = d;
    m = database.m;
    n = database.n;
  }


  public void BPA(int k, MyInteger stopPosition, MyInteger sortedAccess,
      MyInteger randomAccess, AnswerElement[] answers, double[] query)
 {
   int i, j, u;

   for (i=0; i<k; ++i)
   {
     answers[i] = new AnswerElement();
     answers[i].dataID = 0;
     answers[i].overallScore = 0;
   }
   AnswerElement potentialAnswer;
   double [] seenDatalocalScores = new double [m];
   double seenDataOverallScore = 0;
   double minAnswerOverallScore = 0;
   int dataPosition = 0;
   int seenDataID;
   int bp;
   double bestPositionsOverallScore;
   int [] bestPositions = new int [m];
   double [] bestPositionLocalScores = new double [m];
   boolean [][] positionIsSeen = new boolean [m][n];

   for (i=0; i<m; ++i)
   {
     bestPositions[i] = -1;
   }
   for (i=0; i<m; ++i)
   {
     for (j = 0; j < n; ++j)
       positionIsSeen[i][j] = false;
   }


   // obtain top-k answers
   boolean stop = false;
   j=0;
   while ((stop == false) && (j < n))
   {
     for (i=0; i<m; ++i)
     {
       seenDataID = database.lists[i].elements[j].dataID;
       for (u=0; u<m; ++u)
       {
         dataPosition = database.dataPositions[seenDataID].positionsInLists[u];
          seenDatalocalScores[u] =
              database.lists[u].elements[dataPosition].score * query[u];
         //compute the best position in the list
         positionIsSeen[u][dataPosition] = true;
         bp = bestPositions[u];
         while (positionIsSeen[u][bp + 1] == true)
         {
           bp = bp + 1;
         }
         if (bp > bestPositions[u])
         {
           bestPositions[u] = bp;
            bestPositionLocalScores[u] =
 database.lists[u].elements[bp].score * query[u];
         }
       }
        seenDataOverallScore = computeOverallScore(seenDatalocalScores);
       if (seenDataOverallScore > minAnswerOverallScore)
       {
         potentialAnswer = new AnswerElement();
         potentialAnswer.dataID = seenDataID;
         potentialAnswer.overallScore = seenDataOverallScore;
         minAnswerOverallScore = updateAnswers (answers, k, potentialAnswer);
       }
     }
      bestPositionsOverallScore =
 computeOverallScore(bestPositionLocalScores);
     if (bestPositionsOverallScore <= minAnswerOverallScore)
       stop = true;
     ++j;
   }

   //compute the number of sorted access and random access
   stopPosition.setValue(j);
   sortedAccess.setValue(j*m);
   randomAccess.setValue(j*m*(m-1));
 }

  
  public void BPA(int k, MyInteger stopPosition, MyInteger sortedAccess,
      MyInteger randomAccess, AnswerElement[] answers)
 {
   int i, j, u;

   for (i=0; i<k; ++i)
   {
     answers[i] = new AnswerElement();
     answers[i].dataID = 0;
     answers[i].overallScore = 0;
   }
   AnswerElement potentialAnswer;
   double [] seenDatalocalScores = new double [m];
   double seenDataOverallScore = 0;
   double minAnswerOverallScore = 0;
   int dataPosition = 0;
   int seenDataID;
   int bp;
   double bestPositionsOverallScore;
   int [] bestPositions = new int [m];
   double [] bestPositionLocalScores = new double [m];
   boolean [][] positionIsSeen = new boolean [m][n];

   for (i=0; i<m; ++i)
   {
     bestPositions[i] = -1;
   }
   for (i=0; i<m; ++i)
   {
     for (j = 0; j < n; ++j)
       positionIsSeen[i][j] = false;
   }


   // obtain top-k answers
   boolean stop = false;
   j=0;
   while ((stop == false) && (j < n))
   {
     for (i=0; i<m; ++i)
     {
       seenDataID = database.lists[i].elements[j].dataID;
       for (u=0; u<m; ++u)
       {
         dataPosition = database.dataPositions[seenDataID].positionsInLists[u];
          seenDatalocalScores[u] =
              database.lists[u].elements[dataPosition].score;
         //compute the best position in the list
         positionIsSeen[u][dataPosition] = true;
         bp = bestPositions[u];
         while (positionIsSeen[u][bp + 1] == true)
         {
           bp = bp + 1;
         }
         if (bp > bestPositions[u])
         {
           bestPositions[u] = bp;
            bestPositionLocalScores[u] =
 database.lists[u].elements[bp].score;
         }
       }
        seenDataOverallScore = computeOverallScore(seenDatalocalScores);
       if (seenDataOverallScore > minAnswerOverallScore)
       {
         potentialAnswer = new AnswerElement();
         potentialAnswer.dataID = seenDataID;
         potentialAnswer.overallScore = seenDataOverallScore;
         minAnswerOverallScore = updateAnswers (answers, k, potentialAnswer);
       }
     }
      bestPositionsOverallScore =
 computeOverallScore(bestPositionLocalScores);
     if (bestPositionsOverallScore <= minAnswerOverallScore)
       stop = true;
     ++j;
   }

   //compute the number of sorted access and random access
   stopPosition.setValue(j);
   sortedAccess.setValue(j*m);
   randomAccess.setValue(j*m*(m-1));
 }

 public void BPA2(int k, MyInteger stopPosition, MyInteger directAccess, MyInteger randomAccess, AnswerElement [] answers)
 {
   int i, j, u;

   for (i=0; i<k; ++i)
   {
     answers[i] = new AnswerElement();
     answers[i].dataID = 0;
     answers[i].overallScore = 0;
   }
   AnswerElement potentialAnswer;
   double [] seenDatalocalScores = new double [m];
   double seenDataOverallScore = 0;
   double minAnswerOverallScore = 0;
   int dataPosition = 0;
   int seenDataID;
   int bp;
   double bestPositionsOverallScore;
   int [] bestPositions = new int [m];
   double [] bestPositionLocalScores = new double [m];
   boolean [][] positionIsSeen = new boolean [m][n];

   for (i=0; i<m; ++i)
   {
     bestPositions[i] = -1;
   }
   for (i=0; i<m; ++i)
   {
     for (j = 0; j < n; ++j)
       positionIsSeen[i][j] = false;
   }


   // obtain top-k answers
   int dirAccess=0;
   boolean stop = false;
   j=0;
   while ((stop == false) && (j < n))
   {
     for (i=0; i<m; ++i)
     {
       bp = bestPositions[i];
       // do direct access to Li[bp + 1]

       if (bp + 1 < n) {
         seenDataID = database.lists[i].elements[bp + 1].dataID;
         ++ dirAccess;
       }
       else
       {
         seenDataID = database.lists[i].elements[n-1].dataID;
       }
       for (u=0; u<m; ++u)
       {
         dataPosition = database.dataPositions[seenDataID].positionsInLists[u];
         seenDatalocalScores[u] = database.lists[u].elements[dataPosition].score;
         //compute the best position in the list
         positionIsSeen[u][dataPosition] = true;
         bp = bestPositions[u];
         if (bp + 1 < n)
         {
           while (positionIsSeen[u][bp + 1] == true) {
             bp = bp + 1;
             if (bp == n-1) break;
           }
           if (bp > bestPositions[u]) {
             bestPositions[u] = bp;
             bestPositionLocalScores[u] = database.lists[u].elements[bp].score;
           }
         }
       }
       seenDataOverallScore = computeOverallScore (seenDatalocalScores);
       if (seenDataOverallScore > minAnswerOverallScore)
       {
         potentialAnswer = new AnswerElement();
         potentialAnswer.dataID = seenDataID;
         potentialAnswer.overallScore = seenDataOverallScore;
         minAnswerOverallScore = updateAnswers (answers, k, potentialAnswer);
       }
     }
     bestPositionsOverallScore = computeOverallScore (bestPositionLocalScores);
     if (bestPositionsOverallScore <= minAnswerOverallScore)
       stop = true;
     ++j;
   }

   //compute the number of random access and direct access
   stopPosition.setValue(j);
   directAccess.setValue(dirAccess);
   randomAccess.setValue(dirAccess*(m-1));
 }


  public void TA(int k, MyInteger stopPosition, MyInteger sortedAccess,
      MyInteger randomAccess, AnswerElement[] answers, double[] query)
  {
    int i, j, u;

    for (i=0; i<k; ++i)
    {
      answers[i] = new AnswerElement();
      answers[i].dataID = 0;
      answers[i].overallScore = 0;
    }
    AnswerElement potentialAnswer;
    double [] seenDatalocalScores = new double [m];
    double [] currentPositionScores = new double [m];
    double seenDataOverallScore = 0;
    double minAnswerOverallScore = 0;
    int dataPosition = 0;
    int seenDataID;
    double threshold;

    // obtain top-k answers
    boolean stop = false;
    j=0;
    while ((stop == false) && (j < n))
    {
      for (i=0; i<m; ++i)
      {
        currentPositionScores[i] =
            database.lists[i].elements[j].score * query[i];
        seenDataID = database.lists[i].elements[j].dataID;
        for (u=0; u<m; ++u)
        {
          dataPosition = database.dataPositions[seenDataID].positionsInLists[u];
          seenDatalocalScores[u] =
              database.lists[u].elements[dataPosition].score * query[u];
        }
        seenDataOverallScore = computeOverallScore(seenDatalocalScores);
        if (seenDataOverallScore > minAnswerOverallScore)
        {
          potentialAnswer = new AnswerElement();
          potentialAnswer.dataID = seenDataID;
          potentialAnswer.overallScore = seenDataOverallScore;
          minAnswerOverallScore = updateAnswers (answers, k, potentialAnswer);
        }
      }
      threshold = computeOverallScore(currentPositionScores, query);
      if (threshold <= minAnswerOverallScore)
        stop = true;
      ++j;
    }

    //compute the number of sorted access and random access
    stopPosition.setValue(j);
    sortedAccess.setValue(j*m);
    randomAccess.setValue(j*m*(m-1));

  }

  
  public void TA(int k, MyInteger stopPosition, MyInteger sortedAccess,
      MyInteger randomAccess, AnswerElement[] answers)
  {
    int i, j, u;

    for (i=0; i<k; ++i)
    {
      answers[i] = new AnswerElement();
      answers[i].dataID = 0;
      answers[i].overallScore = 0;
    }
    AnswerElement potentialAnswer;
    double [] seenDatalocalScores = new double [m];
    double [] currentPositionScores = new double [m];
    double seenDataOverallScore = 0;
    double minAnswerOverallScore = 0;
    int dataPosition = 0;
    int seenDataID;
    double threshold;

    // obtain top-k answers
    boolean stop = false;
    j=0;
    while ((stop == false) && (j < n))
    {
      for (i=0; i<m; ++i)
      {
        currentPositionScores[i] = database.lists[i].elements[j].score;
        seenDataID = database.lists[i].elements[j].dataID;
        for (u=0; u<m; ++u)
        {
          dataPosition = database.dataPositions[seenDataID].positionsInLists[u];
          seenDatalocalScores[u] =
              database.lists[u].elements[dataPosition].score;
        }
        seenDataOverallScore = computeOverallScore(seenDatalocalScores);
        if (seenDataOverallScore > minAnswerOverallScore)
        {
          potentialAnswer = new AnswerElement();
          potentialAnswer.dataID = seenDataID;
          potentialAnswer.overallScore = seenDataOverallScore;
          minAnswerOverallScore = updateAnswers (answers, k, potentialAnswer);
        }
      }
      threshold = computeOverallScore(currentPositionScores);
      if (threshold <= minAnswerOverallScore)
        stop = true;
      ++j;
    }

    //compute the number of sorted access and random access
    stopPosition.setValue(j);
    sortedAccess.setValue(j*m);
    randomAccess.setValue(j*m*(m-1));

  }

public void FA(int k, MyInteger stopPosition, MyInteger sortedAccess, MyInteger randomAccess, AnswerElement [] answers)
  {
    int i, j;
    int dataID;
    double score;
    int [] accessNumber;
    accessNumber = new int [n];
    for (j=0; j<n; ++j)
      accessNumber[j] = 0;

    boolean stop = false;
    j=0;
    int seenInAllLists = 0;
    while ((stop == false) && (j < n))
    {
      for (i=0; i<m; ++i)
      {
        dataID = database.lists[i].elements[j].dataID;
        ++ accessNumber[dataID];
        if (accessNumber[dataID] == m)
        {
          ++ seenInAllLists;
          if (seenInAllLists >= k)
            stop = true;
        }
      }
      ++j;
    }

    //compute the number of sorted access and random access
    stopPosition.setValue(j);
    sortedAccess.setValue(j*m);
    int rAccess=0;
    for (j=0; j<n; ++j)
      if (accessNumber[j] > 0)
        rAccess = rAccess + (m - accessNumber[j]);
    randomAccess.setValue(rAccess);

    // obtain top-k answers
    for (i=0; i<k; ++i)
    {
      answers[i] = new AnswerElement();
      answers[i].dataID = 0;
      answers[i].overallScore = 0;
    }
    AnswerElement potentialAnswer;
    double [] seenDatalocalScores = new double [m];
    double seenDataOverallScore = 0;
    double minAnswerOverallScore = 0;
    int dataPosition = 0;
    int seenDataID;
    for (j=0; j<n; ++j)
      if (accessNumber[j] > 0)
      {
        seenDataID = j;
        for (i=0; i<m; ++i)
        {
          dataPosition = database.dataPositions[seenDataID].positionsInLists[i];
          seenDatalocalScores[i] = database.lists[i].elements[dataPosition].score;
        }
        seenDataOverallScore = computeOverallScore (seenDatalocalScores);
        if (seenDataOverallScore > minAnswerOverallScore)
        {
          potentialAnswer = new AnswerElement();
          potentialAnswer.dataID = seenDataID;
          potentialAnswer.overallScore = seenDataOverallScore;
          minAnswerOverallScore = updateAnswers (answers, k, potentialAnswer);
        }
      }
  }

  private double computeOverallScore (double [] localScores)
  {
    int i=0;
    double overallScore =0;
    for (i=0; i<m; ++i)
      overallScore = overallScore + localScores [i];

    return overallScore;
  }

  private double computeOverallScore(double[] localScores, double[] query) {
    int i = 0;
    double overallScore = 0;
    for (i = 0; i < m; ++i)
      overallScore = overallScore + localScores[i] * query[i];

    return overallScore;
  }

  private double updateAnswers (AnswerElement [] answers, int k, AnswerElement potentialAnswer)
  {
    int i;
    double minAnswerOverallScore = answers[0].overallScore;
    int minIndex = 0;
    for (i=1; i<k; ++i)
    {
      if (answers[i].overallScore < minAnswerOverallScore) {
        minAnswerOverallScore = answers[i].overallScore;
        minIndex = i;
      }
    }

    for (i=0; i<k; ++i)
    {
      if (answers[i].dataID == potentialAnswer.dataID)
        return minAnswerOverallScore; //data item is yet in the list
    }

    if (potentialAnswer.overallScore > minAnswerOverallScore)
    {
      answers[minIndex] = potentialAnswer;
    }

    minAnswerOverallScore = answers[0].overallScore;
    minIndex = 0;
    for (i=1; i<k; ++i)
      if (answers[i].overallScore < minAnswerOverallScore)
      {
        minAnswerOverallScore = answers[i].overallScore;
        minIndex = i;
      }
    return minAnswerOverallScore;
  }

  private void sortAnswers(AnswerElement [] answers, int k) {
    int i,j;
    AnswerElement tempElement;
    int maxIndex;
    for (i=0; i<k; ++i) {
      maxIndex = i;
      for (j=i+1; j<k; ++j) {
        if (answers[j].overallScore > answers[maxIndex].overallScore)
          maxIndex = j;
      }
      if (maxIndex > i) {
        tempElement = answers[i];
        answers[i] = answers[maxIndex];
        answers[maxIndex] = tempElement;
      }
    }
  }

 
 public void printAllOverallScores()
  {
    int i, j;
    int dataPosition = 0;
    int seenDataID;
    double[] dataItemLocalScores = new double[m];
    double seenDataOverallScore = 0;
    AnswerElement [] allDataItems = new AnswerElement[n];
    for (j=0; j<n; ++j)
      allDataItems[j] = new AnswerElement();
    for (j=0; j<n; ++j)
    {
      seenDataID = j;
      for (i = 0; i < m; ++i) {
        dataPosition = database.dataPositions[seenDataID].positionsInLists[i];
        dataItemLocalScores[i] = database.lists[i].elements[dataPosition].score;
      }
      seenDataOverallScore = computeOverallScore(dataItemLocalScores);
      allDataItems[j].dataID = seenDataID;
      allDataItems[j].overallScore = seenDataOverallScore;
    }
    printAnswers(allDataItems, n);
  }

  public void printAnswers(AnswerElement [] answers, int k)
  {
    int i;
    sortAnswers(answers, k);
    System.out.println();
    for (i=0; i<k; ++i)
      System.out.print(answers[i].dataID + " : " + answers[i].overallScore + " ,   ");
  }
  
  public String getAnswers(int queryId, AnswerElement [] answers, int k)
  {
    int i;
    sortAnswers(answers, k);
    String ans="";
    for (i=0; i<k; ++i)
      ans = ans+queryId+","+(i+1)+","+(answers[i].dataID+1)+ ","+answers[i].overallScore+"\n";
    return ans;
  }
}
