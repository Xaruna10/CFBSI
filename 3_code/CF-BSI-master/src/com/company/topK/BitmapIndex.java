package com.company.topK;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/*
 * BitmapIndex.java
 *
 * Created on November 15, 2004, 5:04 PM
 */

/**
 * 
 * @author Guadalupe Canahuate Gheorghi Guzun
 */

public class BitmapIndex {
  static String fext = ".dat"; // Bitmap index file extension
  public static long[] power2 = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
      4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576,
      2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728,
      268435456, 536870912, 1073741824, 2147483648L };
  // Connection c;
  Map<String, bitArray> bitmaps = new ConcurrentHashMap<String, bitArray>();
  // // Holds
                                                                             // all
                                                                             // the
                                                                             // bitVectors
  private Map<String, bitArray> results =
      new ConcurrentHashMap<String, bitArray>(); // Holds temporary results
  // Hashtable bitmaps = new Hashtable();
  // Hashtable results = new Hashtable();
  String tablename = "";
  String foldername = "";
  String ctype = "";

  // String
  // sql="Select bitmap from bitmap..bitmaps_e where tablename = ? and colname = ? and nvalue = ?";
  // PreparedStatement statement;

  public Set<String> getKeys() {
    return bitmaps.keySet();
  }

  public int numberOfBitmaps() {
    return bitmaps.size();
  }

  /*
   * public boolean existsBitmap(String colname, String value) { return
   * existsBitmap(colname.trim(),Integer.parseInt(value)); }
   */
  public void put(String key, bitArray b) {
    bitmaps.put(key, b);
  }

  public void remove(String key, int b) {
    for (int i = 0; i < b; i++) {
      bitmaps.remove(key + "_" + b);
    }
  }

  public void clearResultsBitmap() {
    results.clear();
  }

  public bitArray getBitmap(String col1) {
    bitArray bV;
    if (bitmaps.containsKey(col1)) {
      bV = bitmaps.get(col1);
      // System.out.println("Retriving bitmap..."+col1);
    } else {
      // System.out.println("Bitmap Not Found. All zeros. " + col1);
      bV = bitmaps.get("B0");
      ;
    }
    // System.out.println(colname+" "+value+" "+bV.toString());
    return bV;
  }

  public bitArray getResultBitmap(String col1) {
    bitArray bV;
    if (results.containsKey(col1)) {
      bV = results.get(col1);
      // System.out.println("Retriving bitmap..."+col1);
    } else {
      // System.out.println("Bitmap Not Found. All zeros. " + col1);
      bV = bitmaps.get("B0");
      ;
    }
    // System.out.println(colname+" "+value+" "+bV.toString());
    return bV;
  }

  public boolean existsBitmap(String col1) {
    return (bitmaps.containsKey(col1));
  }

  public bitArray cloneBitmap(String col1) {
    bitArray bV = getBitmap(col1), bClone = new bitArray();
    // bClone.name = col1;
    bClone.vec = (long[]) bV.vec.clone();
    bClone.maxPos = bV.maxPos;
    return bClone;
  }

  public bitArray cloneBitmap(bitArray bV) {
    if (bV == null)
      return cloneBitmap("B0");
    bitArray bClone = new bitArray();
    bClone.vec = (long[]) bV.vec.clone();
    bClone.maxPos = bV.maxPos;
    return bClone;
  }

  public bitArray cloneResult(String col1) {
    bitArray bV = getResultBitmap(col1), bClone = new bitArray();
    // bClone.name = col1;
    bClone.vec = (long[]) bV.vec.clone();
    bClone.maxPos = bV.maxPos;
    return bClone;
  }

  public void getBitmapIndex(String folder, int words) {
    // Go through the directory passed and gets the file names
    // Calls method readBitmap over each file
    System.out.print(folder + " \t ");
    String[] files;
    String classFileName;
    try {
      File fd = new File(folder);
      files = fd.list();
      for (int i = 0; i < files.length; i++) {
        classFileName = files[i];
        if (classFileName.indexOf(fext) > 0) {
          // DEBUG: System.out.println(folder+'/'+files[i]);
          readBitmap(folder, files[i], words);
        }
      }
    } catch (IOException e) {
      System.err.println("File read failed");
    }
  }

  void readBitmap(String folder, String indexName, int words)
      throws IOException {
    // Takes a compressed bitmap file and converts it into a bitVector and adds
    // it to the bitmaps hashtable
    String file = folder + '/' + indexName;
    String colname = "";
    String bit = "";
    StringTokenizer st =
        new StringTokenizer(indexName.substring(0, indexName.length() - 4), "_");
    while (st.hasMoreTokens()) {
      String str = st.nextToken();
      if (str.compareTo("attrib") == 0) {
        colname = st.nextToken();
      } else if (str.compareTo("bit") == 0) {
        bit = st.nextToken();
      }
    }
    String key = colname + "_" + bit; // indexName.substring (0,
                                      // indexName.indexOf(fext))+ nline;
    try {
      // Wrap the FileInputStream with a DataInputStream
      FileInputStream file_input = new FileInputStream(file);
      DataInputStream data_in = new DataInputStream(file_input);
      bitArray bV = new bitArray(key, words);
      while (true) {
        try {
          bV.appendWord(0xFFFFFFFFL & data_in.readInt());
        } catch (EOFException eof) {
          // System.out.println ("End of File");
          break;
        }
      }
      // DEBUG:
      // System.out.println("Key: "+key+" Words: "+bV.getNumberOfWords());
      bitmaps.put(key, bV);
      data_in.close();
      file_input.close();
    } catch (IOException e) {
      System.out.println("IO Exception =: " + e);
    }

  }

  public long SUM_SIGNED(String colname, int bitslices) {
    long sum = 0;
    bitArray B_i;
    for (int i = 0; i < bitslices - 1; i++) {
      B_i = getBitmap(colname + "_" + i);
      sum += power2[i] * B_i.count();
    }
    B_i = getBitmap(colname + "_" + (bitslices - 1));
    if (B_i != null)
      sum -= power2[bitslices - 1] * B_i.count();
    return sum;
  }

  public long SUM(String colname, int bitslices) {
    long sum = 0;
    bitArray B_i;
    for (int i = 0; i < bitslices; i++) {
      B_i = getBitmap(colname + "_" + i);
      sum += power2[i] * B_i.count();
    }
    return sum;
  }

  public long SUM(String colname, int bitslices, String foundSet) {
    long sum = 0;
    bitArray B_f = getBitmap(foundSet);
    bitArray B_i;
    for (int i = 0; i < bitslices; i++) {
      B_i = getBitmap(colname + "_" + i);
      sum += power2[i] * AND(B_f, B_i).count();
    }
    return sum;
  }

  public int SUBTRACT_BSI(String a, int s, String b, int p, String resultName) {
    int q = Math.max(s, p) + 2;
    bitArray S = null;
    int r, t;
    t = COMPLEMENT2(b, p, b + "_2'");
    r = SUM_BSI(a, s, b + "_2'", t, resultName);
    return r;
  }

  public int SUBTRACT_BSI(String a, int s, String b, int p, String resultName,
      String fs) {
    int q = Math.max(s, p) + 2;
    bitArray S = null;
    int r, t;
    t = COMPLEMENT2(b, q, b + "_2'", fs);
    r = SUM_BSI(a, q, b + "_2'", q, resultName, fs);
    return q;
  }

  public int SUM_BSI_1b(String a, String b, int p) {
    // This method sums a 1-bitslice with p bitslices and put the result in b
    int r = 1; // r=1
    bitArray A, B;
    String b0 = b + "_0";
    A = getBitmap(a);
    B = getBitmap(b0);
    bitArray S = XOR(A, B);
    bitmaps.put(b0, S);
    bitArray C = AND(A, B);
    for (int i = 1; i < p; i++) {
      String bs = b + "_" + i;
      B = getBitmap(bs);
      S = XOR(B, C);
      bitmaps.put(bs, S);
      C = AND(B, C);
    }
    if (C.count() > 0) {
      bitmaps.put(b + "_" + p, C); // Carry bit
      p++;
    }
    return p;
  }


  public int SUM_BSI(String a, int s, String b, int p, String resultName) {
    int q = Math.max(s, p);
    int r = Math.min(s, p);

    bitArray A, B;
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_0");
    bitArray S = XOR(A, B);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray C = AND(A, B);
    for (int i = 1; i < r; i++) {
      A = getBitmap(a + "_" + i);
      B = getBitmap(b + "_" + i);
      S = XOR(XOR(A, B), C);
      bitmaps.put(resultName + "_" + i, S);
      C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
    }
    if (s > p) {
      for (int i = p; i < s; i++) {
        A = getBitmap(a + "_" + i);
        S = XOR(A, C);
        bitmaps.put(resultName + "_" + i, S);
        C = AND(A, C);
      }
    } else {
      for (int i = s; i < p; i++) {
        B = getBitmap(b + "_" + i);
        S = XOR(B, C);
        bitmaps.put(resultName + "_" + i, S);
        C = AND(B, C);
      }
    }
    if (C.count() > 0) {
      bitmaps.put(resultName + "_" + q, C); // Carry bit
      q++;
    }
    return q;
  }
  
  public int SUM_BSI_pref(String a, int s, String b, int p, String resultName,
      int offset) {
    int q = Math.max(s, p);
    int r = Math.min(s, p);

    bitArray A, B;
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_" + offset);
    bitArray S = XOR(A, B);
    bitmaps.put(resultName + "_" + offset, S);
    bitArray C = AND(A, B);
    for (int i = 1; i < r; i++) {
      A = getBitmap(a + "_" + i);
      B = getBitmap(b + "_" + (i + offset));
      S = XOR(XOR(A, B), C);
      bitmaps.put(resultName + "_" + (i + offset), S);
      C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
    }
    if (s > p) {
      for (int i = p; i < s; i++) {
        A = getBitmap(a + "_" + i);
        S = XOR(A, C);
        bitmaps.put(resultName + "_" + (i + offset), S);
        C = AND(A, C);
      }
    } else {
      for (int i = s; i < p; i++) {
        B = getBitmap(b + "_" + (i + offset));
        S = XOR(B, C);
        bitmaps.put(resultName + "_" + (i + offset), S);
        C = AND(B, C);
      }
    }
    if (C.count() > 0) {
      bitmaps.put(resultName + "_" + (q + offset), C); // Carry bit
      q++;
    }
    return (q + offset);
  }

  public int SUM_BSI(String a, int s, String b, int p, String resultName, int offset) {
    int q = Math.max(s, p);
    int r = Math.min(s, p);

    bitArray A, B;
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_" + offset);
    bitArray S = XOR(A, B);
    bitmaps.put(resultName + "_" + offset, S);
    bitArray C = AND(A, B);
    for (int i = 1; i < r; i++) {
      A = getBitmap(a + "_" + i);
      B = getBitmap(b + "_" + (i + offset));
      S = XOR(XOR(A, B), C);
      bitmaps.put(resultName + "_" + (i + offset), S);
      C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
    }
    if (s > p) {
      for (int i = p; i < s; i++) {
        A = getBitmap(a + "_" + i);
        S = XOR(A, C);
        bitmaps.put(resultName + "_" + (i + offset), S);
        C = AND(A, C);
      }
    } else {
      for (int i = s; i < p; i++) {
        B = getBitmap(b + "_" + (i + offset));
        S = XOR(B, C);
        bitmaps.put(resultName + "_" + (i + offset), S);
        C = AND(B, C);
      }
    }
    if (C.count() > 0) {
      bitmaps.put(resultName + "_" + (q + offset), C); // Carry bit
      q++;
    }
    return q;
  }


  public int SUM_BSI(String a, int s, String b, int p, String resultName,
      String fs) {
    int q = Math.max(s, p);
    int r = Math.min(s, p);
    bitArray A, B, F;
    F = getBitmap(fs);
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_0");
    bitArray S = AND(XOR(A, B), F);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray C = AND(AND(A, B), F);
    for (int i = 1; i < r; i++) {
      A = getBitmap(a + "_" + i);
      B = getBitmap(b + "_" + i);
      S = AND(XOR(XOR(A, B), C), F);
      bitmaps.put(resultName + "_" + i, S);
      C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
    }
    if (s > p) {
      for (int i = p; i < s; i++) {
        A = getBitmap(a + "_" + i);
        S = AND(XOR(A, C), F);
        bitmaps.put(resultName + "_" + i, S);
        C = AND(A, C);
      }
    } else {
      for (int i = s; i < p; i++) {
        B = getBitmap(b + "_" + i);
        S = AND(XOR(B, C), F);
        bitmaps.put(resultName + "_" + i, S);
        C = AND(B, C);
      }
    }
    C = AND(C, F);
    if (C.count() > 0) {
      bitmaps.put(resultName + "_" + q, C); // Carry bit
      q++;
    }
    return q;
  }

  public int ABS(String a, int s, String resultName) {
    bitArray SBS = getBitmap(a + "_" + (s - 1));
    bitmaps.put("SBS", SBS);
    bitArray a_i;
    for (int i = 0; i < s - 1; i++) {
      a_i = AND(XOR(SBS, getBitmap(a + "_" + i)), getBitmap("B1"));
      bitmaps.put("a'_" + i, a_i);
    }
    SUM_BSI("a'", s - 1, "SBS", resultName);
    return s - 1;
  }

  public int MULTIPLY(String a, int p, String b, int q, String resultName) {
    String c = "NOT" + a;
    String temp = "T";
    int r = p + q + 3;

    for (int i = 0; i < r; i++) {
      bitmaps.put(resultName + "_" + i, cloneBitmap("B0"));
    }

    for (int i = 0; i < p; i++) {
      bitmaps.put(c + "_" + i, NOT(getBitmap(a + "_" + i)));
    }
    bitArray B1ANDNOTB0;
    bitArray B0ANDNOTB1;
    bitArray T;
    // bitmaps.put(b+"_-1",cloneBitmap("B0"));
    for (int i = 0; i < q; i++) {
      // B1ANDNOTB0 = ANDNOT(getBitmap(b+"_"+i), getBitmap(b+"_"+(i-1)));
      B1ANDNOTB0 =
          AND(getBitmap(b + "_" + i), NOT(getBitmap(b + "_" + (i - 1))));
      // B0ANDNOTB1 = ANDNOT(getBitmap(b+"_"+(i-1)), getBitmap(b+"_"+i));
      B0ANDNOTB1 =
          AND(getBitmap(b + "_" + (i - 1)), NOT(getBitmap(b + "_" + i)));
      // bitmaps.put(temp+"_"+i,cloneBitmap("B0"));
      for (int j = 0; j < p; j++) {
        bitmaps.put(
            temp + "_" + j,
            OR(AND(B1ANDNOTB0, getBitmap(c + "_" + j)),
                AND(B0ANDNOTB1, getBitmap(a + "_" + j))));
      }
      SUM_BSI(resultName, r, temp, p, resultName);
    }
    return r;
  }

  public int getValue(int id, String dim, int slices) {
    int retValue = 0;
    for (int i = 0; i < slices; i++) {
      bitArray bA = getBitmap(dim + "_" + i);
      retValue += power2[i] * bA.getBit(id);
    }
    return retValue;
  }

  public int SUM_BSI(String a, int s, String b, String resultName) {
    bitArray a_i = cloneBitmap(a + "_0");
    bitArray B = cloneBitmap(b);
    bitArray S = XOR(a_i, B);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray C = AND(a_i, B);
    for (int i = 1; i < s; i++) {
      a_i = cloneBitmap(a + "_" + i);
      S = XOR(a_i, C);
      bitmaps.put(resultName + "_" + i, S);
      C = AND(a_i, C);
    }
    if (C.count() > 0) {
      // System.out.println("The last slice of the sum has: "+C.count()+" matches ("+s+")");
      bitmaps.put(resultName + "_" + s, C); // Carry bit
      s++;
    }
    return s;
  }

  public int SUM_BSI(String a, int s, String b, String resultName, String fs) {
    bitArray a_i = getBitmap(a + "_0");
    bitArray B = getBitmap(b);
    bitArray F = getBitmap(fs);
    bitArray S = AND(XOR(a_i, B), F);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray C = AND(AND(a_i, B), F);
    for (int i = 1; i < s; i++) {
      a_i = getBitmap(a + "_" + i);
      S = AND(XOR(a_i, C), F);
      bitmaps.put(resultName + "_" + i, S);
      C = AND(a_i, C);
    }
    if (C.count() > 0) {
      bitmaps.put(resultName + "_" + s, C); // Carry bit
      s++;
    }
    return s;
  }

  public int topKMax_bnn(String a, int s, int k, String resultName) {
    int n = 0;
    bitArray G = cloneBitmap("B0");
    bitArray E = cloneBitmap("B1");
    bitArray F, X, S, SE;
    for (int i = s - 1; i >= 0; i--) {
      S = cloneBitmap(a + "_" + i);
      SE = AND(E, S);
      X = OR(G, SE); // Maximum
      n = X.count();
      if (n > k) {
        E = SE;
      }
      if (n < k) {
        G = X;
        E = AND(E, NOT(S));
      }
      if (n == k) {
        E = SE;
        break;
      }
    }
    n = G.getMatches();

    F = OR(G, E.first(k - n));
    // F = OR(G,E); //With ties
    // System.out.println("F: "+F.count());
    bitmaps.put(resultName, F);
    bitmaps.put(resultName + "_E", E);
    bitmaps.put(resultName + "_G", G);
    bitmaps.put(resultName + "_F", OR(G, E));
    return n;
  }

  public int topKMax(String a, int s, int k, String resultName) {
    int n = 0;
    bitArray G = cloneBitmap("B0");
    bitArray E = cloneBitmap("B1");
    // System.out.println("E count all 1s: "+E.count());
    // System.out.println("G count all 0s: "+G.count());
    bitArray F, X, S, SE;
    for (int i = s - 1; i >= 0; i--) {
      S = cloneBitmap(a + "_" + i);
      SE = AND(E, S);
      X = OR(G, SE); // Maximum
      // System.out.println("BitSlice "+i+" Matches: "+S.count());
      // System.out.println("S AND E: "+SE.count());
      n = X.count();
      // System.out.println("X Matches: "+n);
      // System.out.println("E Matches: "+E.count());
      // System.out.println("G Matches: "+G.count());
      if (n > k) {
        E = SE;
        // System.out.println("New E (n>k): "+E.count());
      }
      if (n < k) {
        G = X;
        E = AND(E, NOT(S));
        // System.out.println("G: "+G.count());
        // System.out.println("New E (n<k): "+E.count());
      }
      if (n == k) {
        E = SE;
        // System.out.println("New E (n=k): "+E.count());
        break;
      }
    }
    n = G.getMatches();
    F = OR(G, E.first(k - n));
    // F = OR(G,E); //With ties
    // System.out.println("F: "+F.count());
    bitmaps.put(resultName, F);
    bitmaps.put(resultName + "_E", E);
    bitmaps.put(resultName + "_G", G);
    bitmaps.put(resultName + "_F", OR(G, E));
    return n;
  }

  public int topKMaxPref(String a, int s, int k, String resultName) {
    int n = 0;
    bitArray G = cloneBitmap("B0");
    bitArray E = cloneBitmap("B1");
    // System.out.println("E count all 1s: "+E.count());
    // System.out.println("G count all 0s: "+G.count());
    bitArray F, X, S, SE;
    for (int i = s - 1; i >= 0; i--) {
      S = cloneResult(a + "_" + i);
      SE = AND(E, S);
      X = OR(G, SE); // Maximum
      // System.out.println("BitSlice "+i+" Matches: "+S.count());
      // System.out.println("S AND E: "+SE.count());
      n = X.count();
      // System.out.println("X Matches: "+n);
      // System.out.println("E Matches: "+E.count());
      // System.out.println("G Matches: "+G.count());
      if (n > k) {
        E = SE;
        // System.out.println("New E (n>k): "+E.count());
      }
      if (n < k) {
        G = X;
        E = AND(E, NOT(S));
        // System.out.println("G: "+G.count());
        // System.out.println("New E (n<k): "+E.count());
      }
      if (n == k) {
        E = SE;
        // System.out.println("New E (n=k): "+E.count());
        break;
      }
    }
    n = G.getMatches();
    F = OR(G, E.first(k - n));
    // F = OR(G,E); //With ties
    // System.out.println("F: "+F.count());
    bitmaps.put(resultName, F);
    bitmaps.put(resultName + "_E", E);
    bitmaps.put(resultName + "_G", G);
    bitmaps.put(resultName + "_F", OR(G, E));
    return n;
  }

  public void topKMax(String a, int s, int k, String resultName,
      boolean relevant) {
    if (relevant)
      topKMax_Relevant(a, s, k, resultName);
    else
      topKMax(a, s, k, resultName);
  }

  public void topKMax_Relevant(String a, int s, int k, String resultName) {
    bitArray G = cloneBitmap("B0");
    bitArray E = cloneBitmap("B1");
    bitArray F, X, S, SE;
    for (int i = s - 1; i >= 0; i--) {
      S = cloneBitmap(a + "_" + i);
      SE = AND(E, S);
      X = OR(G, SE); // Maximum
      int n = X.count();
      if (n > k) {
        E = SE;
      }
      if (n < k) {
        G = X;
        E = AND(E, NOT(S));
      }
      if (n == k) {
        E = SE;
        break;
      }
    }
    int matches = G.getMatches();
    F = OR(G, E);
    /*
     * if (matches>2) { F = G; } else { F = OR(G,E.first(k-G.getMatches())); }
     */
    // //With ties
    // System.out.println("F: "+F.count());
    bitmaps.put(resultName, F);
  }

  public void topKMin(String a, int s, int k, String resultName) {
    bitArray G = cloneBitmap("B0");
    bitArray E = cloneBitmap("B1");
    bitArray F, X, S, SNOT;
    for (int i = s - 1; i >= 0; i--) {
      S = cloneBitmap(a + "_" + i);
      SNOT = AND(E, NOT(S));
      X = OR(G, SNOT); // Maximum
      int n = X.count();
      if (n > k) {
        E = SNOT;
      } else if (n < k) {
        G = X;
        E = AND(E, S);
      } else {
        E = SNOT;
        break;
      }
    }
    // F = OR(G,E.get(k-G.getMatches())); //Exact Number of Matches = K
    F = OR(G, E); // With ties
    bitmaps.put(resultName, F);
  }

  public void topKMin(String a, int s, int k, String resultName, String fs) {
    bitArray G = cloneBitmap("B0");
    bitArray E = cloneBitmap(fs);
    bitArray F, X, S, SNOT;
    for (int i = s - 1; i >= 0; i--) {
      S = cloneBitmap(a + "_" + i);
      SNOT = AND(E, NOT(S));
      X = OR(G, SNOT); // Maximum
      int n = X.count();
      if (n > k) {
        E = SNOT;
      } else if (n < k) {
        G = X;
        E = AND(E, S);
      } else {
        E = SNOT;
        break;
      }
    }
    // F = OR(G,E.get(k-G.getMatches())); //Exact Number of Matches = K
    F = OR(G, E); // With ties
    bitmaps.put(resultName, F);
  }

  public void COMPLEMENT(String a, int s, String resultKey) {
    bitArray S;
    for (int i = 0; i < s; i++) {
      S = getBitmap(a + "_" + i);
      /*
       * if (S==null) { bitmaps.put(resultKey+"_"+i, cloneBitmap("B1")); } else
       * {
       */
      bitmaps.put(resultKey + "_" + i, AND(NOT(S), getBitmap("B1")));
      // bitmaps.put(resultKey+"_"+i, ANDNOT(getBitmap("B1"),S));
      // }
    }
  }

  public void COMPLEMENT(String a, int s, String resultKey, String fs) {
    bitArray S;
    for (int i = 0; i < s; i++) {
      S = getBitmap(a + "_" + i);
      /*
       * if (S==null) { bitmaps.put(resultKey+"_"+i, cloneBitmap("B1")); } else
       * {
       */
      bitmaps.put(resultKey + "_" + i, AND(NOT(S), getBitmap(fs)));
      // bitmaps.put(resultKey+"_"+i, ANDNOT(getBitmap("B1"),S));
      // }
    }
  }

  public int COMPLEMENT2(String a, int s, String resultKey) {
    COMPLEMENT(a, s, a + "Compl");
    int r = SUM_BSI(a + "Compl", s, "B1", resultKey);
    return s;
  }

  public int COMPLEMENT2(String a, int s, String resultKey, String fs) {
    COMPLEMENT(a, s, a + "Compl", fs);
    int r = SUM_BSI(a + "Compl", s, fs, resultKey);
    // int r = SUM_BSI(a+"Compl",s,"B1",resultKey, fs);
    return s;
  }

  public void RANGE_BETWEEN(String colname, int bitslices, int lb, int ub,
      String resultKey) {
    String bitmapName = "";
    bitArray B_gt, B_lt, B_eq1, B_eq2, B_i, B_f;
    B_gt = cloneBitmap("B0");
    B_lt = cloneBitmap("B0");
    B_eq1 = cloneBitmap("B1"); // This should be the EBM if there is one
    B_eq2 = cloneBitmap("B1"); // This should be the EBM if there is one
    B_f = getBitmap(resultKey);
    if (B_f == null) {
      B_f = cloneBitmap("B1");
    }
    for (int i = bitslices - 1; i >= 0; i--) {
      B_i = getBitmap(colname + "_" + i);
      if ((ub & BitmapIndex.power2[i]) != 0) {// The bit i is set in ub
        // B_lt = OR(B_lt, ANDNOT(B_eq1, B_i));
        B_lt = OR(B_lt, AND(B_eq1, NOT(B_i)));
        B_eq1 = AND(B_eq1, B_i);
      } else { // The bit i is not set in ub
        // B_eq1 = ANDNOT(B_eq1, B_i);
        B_eq1 = AND(B_eq1, NOT(B_i));
      }
      if ((lb & BitmapIndex.power2[i]) != 0) {// The bit i is set in lb
        B_eq2 = AND(B_eq2, B_i);
      } else { // The bit i is not set in lb
        B_gt = OR(B_gt, AND(B_eq2, B_i));
        // B_eq2 = ANDNOT(B_eq2, B_i);
        B_eq2 = AND(B_eq2, NOT(B_i));
      }
    }
    B_lt = OR(B_lt, B_eq1);
    B_gt = OR(B_gt, B_eq2);
    B_f = AND(B_lt, AND(B_gt, B_f));
    bitmaps.put(resultKey, B_f);
  }

  public bitArray RANGE_GREATER(String colname, int bitslices, int lb) {
    String bitmapName = "";
    bitArray B_gt, B_lt, B_eq1, B_eq2, B_i, B_f;
    B_gt = cloneBitmap("B0");
    B_lt = cloneBitmap("B0");
    B_eq1 = cloneBitmap("B1"); // This should be the EBM if there is one
    B_eq2 = cloneBitmap("B1"); // This should be the EBM if there is one
    // B_f = getBitmap(resultKey);
    // if (B_f == null) {
    B_f = cloneBitmap("B1");
    // }
    for (int i = bitslices - 1; i >= 0; i--) {
      B_i = getBitmap(colname + "_" + i);
      if ((lb & BitmapIndex.power2[i]) != 0) {// The bit i is set in lb
        B_eq2 = AND(B_eq2, B_i);
      } else { // The bit i is not set in lb
        B_gt = OR(B_gt, AND(B_eq2, B_i));
        // B_eq2 = ANDNOT(B_eq2, B_i);
        B_eq2 = AND(B_eq2, NOT(B_i));
      }
    }
    // B_lt = OR(B_lt, B_eq1);
    B_gt = OR(B_gt, B_eq2);
    // B_f = AND(B_lt, AND(B_gt, B_f));
    B_f = AND(B_gt, B_f);
    return B_f;
  }

  public void EQUAL(String colname, int bitslices, int value, String resultKey) {
    String bitmapName = "";
    bitArray B_eq, B_i, B_f;
    B_f = getBitmap(resultKey);
    if (B_f.count() == 0) {
      B_f = getBitmap("B1");
    }
    B_eq = B_f; // This should be the EBM if there is one
    for (int i = bitslices - 1; i >= 0; i--) {
      B_i = getBitmap(colname + "_" + i);
      if ((value & BitmapIndex.power2[i]) != 0) {// The bit i is set in ub
        B_eq = AND(B_eq, B_i);
      } else { // The bit i is not set in ub
        // B_eq = ANDNOT(B_eq, B_i);
        B_eq = AND(B_eq, NOT(B_i));
      }
    }
    bitmaps.put(resultKey, B_eq);
  }

  public String RANGE(String colname, int bitslices, int comp1, int constant) {
    String bitmapName = colname + "_rs_" + comp1 + "_" + constant;
    return bitmapName;
  }

  public bitArray NOT(bitArray x) {
    int nWords;
    bitArray z = new bitArray(x.vec.length);
    x.pos = 0;
    while (x.pos < x.vec.length) {
      x.decode();
      z.active = 0xFFFFFFFFL & ~x.active;
      z.appendWord();
      x.pos++;
    }
    return z;
  }

  public bitArray AND(bitArray x, bitArray y) {
    bitArray z = new bitArray(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      x.decode(pos);
      y.decode(pos);
      z.active = x.active & y.active;
      z.appendWord();
      pos++;
    }
    return z;
  }

  public bitArray ANDNOT(bitArray x, bitArray y) {
    bitArray z = new bitArray(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      x.decode(pos);
      y.decode(pos);
      z.active = x.active & (~y.active);
      z.appendWord();
      pos++;
    }
    return z;
  }

  public bitArray OR(bitArray x, bitArray y) {
    // Input: Two bitArray x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray z = new bitArray(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      x.decode(pos);
      y.decode(pos);
      z.active = x.active | y.active;
      z.appendWord();
      pos++;
    }
    return z;
  }

  public bitArray XOR(bitArray x, bitArray y) {
    // Input: Two bitArray x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray z = new bitArray(x.vec.length);
    int pos = 0;
    while (pos < x.vec.length) {
      x.decode(pos);
      y.decode(pos);
      z.active = x.active ^ y.active;
      z.appendWord();
      pos++;
    }
    return z;
  }

  public void pruneBitmaps() {
    bitmaps.clear();
  }

  public BitmapIndex(String foldername, String tablename, String ctype,
      int words) {
    this.foldername = foldername;
    this.tablename = tablename;
    this.ctype = ctype;
    this.getBitmapIndex(foldername, words);
    // this.connect();
  }

  public BitmapIndex(String tablename, String ctype) {
    this.tablename = tablename;
    this.ctype = ctype;
    // this.connect();
  }
}

