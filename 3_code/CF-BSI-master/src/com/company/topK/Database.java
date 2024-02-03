package com.company.topK;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Database {
  public List [] lists;
  public int m;
  public int n;
  public DataItemPositions  [] dataPositions;
  private int maxIntRandom = 10000;
  private boolean flag = true;

  public Database(int in_m, int in_n, double[][] rawdata)
  {

    m = in_m;
    n = in_n;
    lists = new List[m];
    int i, j;
    for (i=0; i<m; ++i) {
      lists[i] = new List();
      lists[i].elements = new ListElement[n];
      for (j=0; j<n; ++j)
      {
        lists[i].elements[j] = new ListElement(j, rawdata[j][i + 1]);
        // dataID++;
      }
      lists[i].sortElements(n);
    }

    dataPositions = new DataItemPositions [n];
    for (j=0; j<n; ++j) {
      dataPositions[j] = new DataItemPositions();
      dataPositions[j].dataID = j;
      dataPositions[j].positionsInLists = new int [m];
    }

    for (int k = 0; k < m; k++) {
      for (int b = 0; b < lists[k].elements.length; b++) {
        dataPositions[lists[k].elements[b].dataID].positionsInLists[k] = b;
      }

    }

  }

 

 public void printDatabase() {
   int i, j;
   double temp;

   int dataID;
   for (j=0; j<n; ++j) {
     System.out.print((j+1) + "  -> " );
     for (i=0; i<m; ++i){
       temp = Math.round ((lists[i].elements[j].score) * 1000);
       temp = temp / 1000;
       dataID = lists[i].elements[j].dataID;
       System.out.print (dataID + " : " + temp + " ,   ");
     }

     System.out.println();
   }
 }

  /*
   * public void populatePositions() { for (int i = 0; i < dataPositions.length;
   * i++) { for (int j = 0; j < dataPositions[i].positionsInLists.length; j++)
   * dataPositions[i].positionsInLists[j] = i; } }
   */
 public void printPositions() {
   int i, j;
   double temp;
   for (j=0; j<n; ++j) {
     System.out.print (j + " : ");
     for (i=0; i<m; ++i){
       temp = Math.round ((dataPositions[j].positionsInLists[i]) * 1000);
       temp = temp / 1000;
       temp = temp + 1;
       System.out.print(temp + " , ");
     }
     System.out.println();
   }
 }

}
