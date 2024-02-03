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

public class BitmapIndex64 {

  static String fext = ".dat"; // Bitmap index file extension
  static long[] power2 = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
      4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576,
      2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728,
      268435456, 536870912, 1073741824, 2147483648L };
  // Connection c;
  Map<String, bitArray64> bitmaps = new ConcurrentHashMap<String, bitArray64>();
  // // Holds
  // all
  // the
  // bitVectors
  private Map<String, bitArray64> results =
      new ConcurrentHashMap<String, bitArray64>(); // Holds temporary results
  // Hashtable bitmaps = new Hashtable();
  // Hashtable results = new Hashtable();
  String tablename = "";
  String foldername = "";
  String ctype = "";
  bitArray64[] resArray;

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
  public void put(String key, bitArray64 b) {
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

  public bitArray64 getBitmap(String col1) {
    bitArray64 bV;
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

  public bitArray64 getResultBitmap(String col1) {
    bitArray64 bV;
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

  public bitArray64 cloneBitmap(String col1) {
    bitArray64 bV = getBitmap(col1), bClone = new bitArray64();
    // bClone.name = col1;
    bClone.vec = (long[]) bV.vec.clone();
    bClone.maxPos = bV.maxPos;
    return bClone;
  }

  public bitArray64 cloneBitmap(bitArray64 bV) {
    if (bV == null)
      return cloneBitmap("B0");
    bitArray64 bClone = new bitArray64();
    bClone.vec = (long[]) bV.vec.clone();
    bClone.maxPos = bV.maxPos;
    return bClone;
  }

  public bitArray64 cloneResult(String col1) {
    bitArray64 bV = getResultBitmap(col1), bClone = new bitArray64();
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
      bitArray64 bV = new bitArray64(key, words);
      while (true) {
        try {
          bV.appendWord(0xFFFFFFFFFFFFFFFFL & data_in.readInt());
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
    bitArray64 B_i;
    for (int i = 0; i < bitslices - 1; i++) {
      B_i = getBitmap(colname + "_" + i);
      sum += (1L << i) * B_i.count();
    }
    B_i = getBitmap(colname + "_" + (bitslices - 1));
    if (B_i != null)
      sum -= (1L << (bitslices - 1)) * B_i.count();
    return sum;
  }

  public long SUM(String colname, int bitslices) {
    long sum = 0;
    bitArray64 B_i;
    for (int i = 0; i < bitslices; i++) {
      B_i = getBitmap(colname + "_" + i);
      sum += (1L << i) * B_i.count();
    }
    return sum;
  }

  public long SUM(String colname, int bitslices, String foundSet) {
    long sum = 0;
    bitArray64 B_f = getBitmap(foundSet);
    bitArray64 B_i;
    for (int i = 0; i < bitslices; i++) {
      B_i = getBitmap(colname + "_" + i);
      sum += (1L << i) * AND(B_f, B_i).count();
    }
    return sum;
  }

  public int SUBTRACT_BSI(String a, int s, String b, int p, String resultName) {
    int q = Math.max(s, p) + 2;
    bitArray64 S = null;
    int r, t;
    t = COMPLEMENT2(b, p, b + "_2'");
    r = SUM_BSI(a, s, b + "_2'", t, resultName);
    return r;
  }

  public int SUBTRACT_BSI(String a, int s, String b, int p, String resultName,
      String fs) {
    int q = Math.max(s, p) + 2;
    bitArray64 S = null;
    int r, t;
    t = COMPLEMENT2(b, q, b + "_2'", fs);
    r = SUM_BSI(a, q, b + "_2'", q, resultName, fs);
    return q;
  }

  public int SUM_BSI_1b(String a, String b, int p) {
    // This method sums a 1-bitslice with p bitslices and put the result in b
    int r = 1; // r=1
    bitArray64 A, B;
    String b0 = b + "_0";
    A = getBitmap(a);
    B = getBitmap(b0);
    bitArray64 S = XOR(A, B);
    bitmaps.put(b0, S);
    bitArray64 C = AND(A, B);
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

    bitArray64 A, B;
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_0");
    bitArray64 S = XOR(A, B);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray64 C = AND(A, B);
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

    bitArray64 A, B;
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_" + offset);
    bitArray64 S = XOR(A, B);
    bitmaps.put(resultName + "_" + offset, S);
    bitArray64 C = AND(A, B);
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

  public int SUM_BSI_array(String a, int s, int p, int offset) {
    int q = Math.max(s, p);
    int r = Math.min(s, (p));

    bitArray64 A, B, C, S;
    A = getBitmap(a + "_0");
    if (resArray[offset] == null) {
      resArray[offset] = A;
      C = cloneBitmap("B0");
    } else {
    B = resArray[offset];
      S = XOR(A, B);
    resArray[offset] = S;
      C = AND(A, B);
    }

    for (int i = 1; i < r; i++) {
      A = getBitmap(a + "_" + i);
      B = resArray[i + offset];
      S = XOR(XOR(A, B), C);
      resArray[i + offset] = S;
      C = OR(OR(AND(A, B), AND(A, C)), AND(C, B));
    }
    if (s > (p)) {
      for (int i = p; i < s; i++) {
        A = getBitmap(a + "_" + i);
        S = XOR(A, C);
        resArray[i + offset] = S;
        C = AND(A, C);
      }
    } else {
      for (int i = s; i < p; i++) {
        if(resArray[i+offset]==null)
          B=cloneBitmap("B0");
        else
        B = resArray[i + offset];
        S = XOR(B, C);
        resArray[i + offset] = S;
        C = AND(B, C);
      }
    }
    if (C.getMatches() > 0) {
      resArray[q + offset] = C;

      q++;
    }
    return (q + offset);
  }

  public int SUM_BSI(String a, int s, String b, int p, String resultName,
      int offset) {
    int q = Math.max(s, p);
    int r = Math.min(s, p);

    bitArray64 A, B;
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_" + offset);
    bitArray64 S = XOR(A, B);
    bitmaps.put(resultName + "_" + offset, S);
    bitArray64 C = AND(A, B);
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
    bitArray64 A, B, F;
    F = getBitmap(fs);
    A = getBitmap(a + "_0");
    B = getBitmap(b + "_0");
    bitArray64 S = AND(XOR(A, B), F);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray64 C = AND(AND(A, B), F);
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
    bitArray64 SBS = getBitmap(a + "_" + (s - 1));
    bitmaps.put("SBS", SBS);
    bitArray64 a_i;
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
    bitArray64 B1ANDNOTB0;
    bitArray64 B0ANDNOTB1;
    bitArray64 T;
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
      bitArray64 bA = getBitmap(dim + "_" + i);
      retValue += (1 << i) * bA.getBit(id);
    }
    return retValue;
  }

  public int SUM_BSI(String a, int s, String b, String resultName) {
    bitArray64 a_i = cloneBitmap(a + "_0");
    bitArray64 B = cloneBitmap(b);
    bitArray64 S = XOR(a_i, B);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray64 C = AND(a_i, B);
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
    bitArray64 a_i = getBitmap(a + "_0");
    bitArray64 B = getBitmap(b);
    bitArray64 F = getBitmap(fs);
    bitArray64 S = AND(XOR(a_i, B), F);
    bitmaps.put(resultName + "_" + 0, S);
    bitArray64 C = AND(AND(a_i, B), F);
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
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap("B1");
    bitArray64 F, X, S, SE;
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
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap("B1");
    // System.out.println("E count all 1s: "+E.count());
    // System.out.println("G count all 0s: "+G.count());
    bitArray64 F, X, S, SE;
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
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap("B1");
    // System.out.println("E count all 1s: "+E.count());
    // System.out.println("G count all 0s: "+G.count());
    bitArray64 F, X, S, SE;
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

  public bitArray64 topKMax_ressArray(int s, int k) {
    int n = 0;
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap("B1");
    // System.out.println("E count all 1s: "+E.count());
    // System.out.println("G count all 0s: "+G.count());
    bitArray64 F, X, S, SE;
    for (int i = s - 1; i >= 0; i--) {
      S = resArray[i];
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
    resArray[s] = F;

    // bitmaps.put(resultName + "_E", E);
    // bitmaps.put(resultName + "_G", G);
    // bitmaps.put(resultName + "_F", OR(G, E));
    return resArray[s];
  }

  public void topKMax(String a, int s, int k, String resultName,
      boolean relevant) {
    if (relevant)
      topKMax_Relevant(a, s, k, resultName);
    else
      topKMax(a, s, k, resultName);
  }

  public void topKMax_Relevant(String a, int s, int k, String resultName) {
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap("B1");
    bitArray64 F, X, S, SE;
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
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap("B1");
    bitArray64 F, X, S, SNOT;
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
    bitArray64 G = cloneBitmap("B0");
    bitArray64 E = cloneBitmap(fs);
    bitArray64 F, X, S, SNOT;
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
    bitArray64 S;
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
    bitArray64 S;
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
    bitArray64 B_gt, B_lt, B_eq1, B_eq2, B_i, B_f;
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
      if ((ub & (1 << i)) != 0) {// The bit i is set in ub
        // B_lt = OR(B_lt, ANDNOT(B_eq1, B_i));
        B_lt = OR(B_lt, AND(B_eq1, NOT(B_i)));
        B_eq1 = AND(B_eq1, B_i);
      } else { // The bit i is not set in ub
        // B_eq1 = ANDNOT(B_eq1, B_i);
        B_eq1 = AND(B_eq1, NOT(B_i));
      }
      if ((lb & (1 << i)) != 0) {// The bit i is set in lb
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

  public bitArray64 RANGE_GREATER(String colname, int bitslices, int lb) {
    String bitmapName = "";
    bitArray64 B_gt, B_lt, B_eq1, B_eq2, B_i, B_f;
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
      if ((lb & (1 << i)) != 0) {// The bit i is set in lb
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
    bitArray64 B_eq, B_i, B_f;
    B_f = getBitmap(resultKey);
    if (B_f.count() == 0) {
      B_f = getBitmap("B1");
    }
    B_eq = B_f; // This should be the EBM if there is one
    for (int i = bitslices - 1; i >= 0; i--) {
      B_i = getBitmap(colname + "_" + i);
      if ((value & (1 << i)) != 0) {// The bit i is set in ub
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

  public bitArray64 NOT(bitArray64 x) {
    int nWords;
    bitArray64 z = new bitArray64(x.vec.length);
    x.pos = 0;
    while (x.pos < x.vec.length) {
      x.decode();
      z.active = 0xFFFFFFFFFFFFFFFFL & ~x.active;
      z.appendWord();
      x.pos++;
    }
    return z;
  }

  public bitArray64 AND(bitArray64 x, bitArray64 y) {
    bitArray64 z = new bitArray64(x.vec.length);
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

  public bitArray64 ANDNOT(bitArray64 x, bitArray64 y) {
    bitArray64 z = new bitArray64(x.vec.length);
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

  public bitArray64 OR(bitArray64 x, bitArray64 y) {
    // Input: Two bitArray64 x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray64 z = new bitArray64(x.vec.length);
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

  public bitArray64 XOR(bitArray64 x, bitArray64 y) {
    // Input: Two bitArray64 x and y containing the same number of bits.
    // Output: The result of a bitwise OR operation as z.
    bitArray64 z = new bitArray64(x.vec.length);
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

  public BitmapIndex64(String foldername, String tablename, String ctype,
      int words) {
    this.foldername = foldername;
    this.tablename = tablename;
    this.ctype = ctype;
    this.getBitmapIndex(foldername, words);
    // this.connect();
  }

  public BitmapIndex64(String tablename, String ctype) {
    this.tablename = tablename;
    this.ctype = ctype;
    // this.connect();
  }

}
