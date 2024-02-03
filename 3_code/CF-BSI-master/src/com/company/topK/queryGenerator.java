
package com.company.topK;
import java.util.Random;


public class queryGenerator {

  /***
   * Generates queries in form of an array of booleans
   * 
   * @param N - number of queries
   * @param atts - number of attributes weighted in the query
   * 
   */
  public static boolean[][] query_boolean(int N, int atts) {

    boolean[][] queries = new boolean[N][atts];
    Random random = new Random();

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < atts; j++) {
        queries[i][j] = random.nextInt(5) == 0;
        // System.out.print(queries[i][j] + "   ");
      }
      // System.out.println();
    }
    return queries;

  }

  /***
   * Generates queries in form of an array of one decimal doubles
   * 
   * @param N - number of queries
   * @param atts - number of attributes weighted in the query
   * 
   */
  public static double[][] query_oneDecimal(int N, int atts) {

    double[][] queries = new double[N][atts];
    Random random = new Random();

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < atts; j++) {
        queries[i][j] = (double) (random.nextInt(10)) / 10;
        System.out.print(queries[i][j] + "   ");
      }
      System.out.println();
    }
    return queries;

  }

  /***
   * Generates queries in form of an array of integers (1-10)
   * 
   * @param N - number of queries
   * @param atts - number of attributes weighted in the query
   * 
   */
  public static int[][] query_int(int N, int atts) {

    int[][] queries = new int[N][atts];
    Random random = new Random();

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < atts; j++) {
        queries[i][j] = (random.nextInt(11));
        // queries[i][j] = 1;
        System.out.print(queries[i][j] + ",");
      }
      System.out.println();
    }
    return queries;

  }

  // public static void main(String[] args) {

  // }

}
