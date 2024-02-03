package com.company.bsi;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class testBSI {
	
	public static void main (String[] args) throws IOException {
		int rows =200;
		int atts=68;
		int decimals=0;
		boolean hasID=true;
		long[][] rawData=readRawData("D:/datasets/audiology_ints.txt",rows,atts,hasID,decimals);
		BsiAttribute[] bsiData = new BsiAttribute[atts];
		long[] query = rawData[9];
		
		for(int j=0;j<atts;j++){
			ArrayList<Long> array = new ArrayList<Long>();
			BsiAttribute bs = new BsiSigned(1);
			for(int i=0;i<rows;i++){
				array.add(rawData[i][j]);	
			}
			bsiData[j]=bs.buildBsiAttributeFromArray(array.iterator(), rows, 0.2);
		}
       // BsiAttribute result = bsiBitmaps.get("A0").SUM((-1)*(int)q[1]*mult).abs();
		BsiAttribute result = bsiData[1].SUM(query[1]*(-1)).abs();
		
		
		for(int i=0; i<rows;i++){
			System.out.println(Math.abs(rawData[i][1]-query[1])+" | "+bsiData[1].getValue(i)+"  |  "+result.getValue(i));
		}
		
	}
	
	public static  long[][] readRawData(String filename, int rows, int attribs, boolean hasId, int decimals) {

		long[][] rawData = new long[rows][attribs];

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
//				int id;
//				if (hasId) {
//					id = (int)Double.parseDouble(strT.nextToken());
//					
//				} else {
//					id = seq;
//				}
				int attrNumber = 0;
				int offset=0;
				if(hasId)
					offset=1;
				attrNumber+=offset;
				int toks = attribs;
				if(hasId)
					strT.nextToken();
				while ((strT.hasMoreTokens() && toks > 0)) {
					
					
					long f = (long)(Double.parseDouble(strT.nextToken())*Math.pow(10, decimals));
					rawData[seq - 1][attrNumber-offset] = f;					
					attrNumber++;
					toks--;
				}

				line = input.readLine();
			}

			fin.close();
			// Catches any error conditions
		} catch (IOException e) {
			System.err.println("Unable to read from file");
			e.printStackTrace();
		}
		
		return rawData;
	}

}
