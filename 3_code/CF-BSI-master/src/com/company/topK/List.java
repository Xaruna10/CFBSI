package com.company.topK;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class List {
  public ListElement [] elements;

  public void sortElements(int n) {
    int i,j;
    ListElement tempElement;
    int maxIndex;
    for (i=0; i<n; ++i) {
      maxIndex = i;
      for (j=i+1; j<n; ++j) {
        if (elements[j].score > elements[maxIndex].score)
          maxIndex = j;
      }
      if (maxIndex > i) {
        tempElement = elements[i];
        elements[i] = elements[maxIndex];
        elements[maxIndex] = tempElement;
      }
    }
  }

  public void sortLimitedIntScoreLists(int n, int limit)
  {
    int [] buckets = new int [limit + 1];
    int i, j, u;
    for (j = 0; j < limit + 1; ++j)
      buckets[j] = 0;
    for (i = 0; i < n; ++i)
    {
      j = (int) elements[i].score;
      buckets[j] = buckets[j] + 1;
    }
    int tempInt1, tempInt2;
    tempInt1 = buckets[0];
    buckets[0] = 0;
    for (j = 1; j < limit + 1; ++j)
    {
      tempInt2 = buckets[j];
      buckets[j] = tempInt1 + buckets[j-1];
      tempInt1 = tempInt2;
    }
    ListElement [] elems = new ListElement[n];
    for (i = 0; i < n; ++i)
    {
      elems[i] = new ListElement();
    }

    for (i = 0; i < n; ++i)
    {
      j = (int) elements[i].score;
      u = buckets[j];
      elems[u] = elements[i];
      buckets[j] = buckets[j] + 1;
    }
    for (i = 0; i < n; ++i)
    {
      elements[i] = elems[n - i - 1];
    }

  }

}
