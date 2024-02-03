package com.company;

import java.io.*;
import java.util.*;
import com.company.bsi.BsiSigned;
import com.company.bsi.BsiAttribute;
import com.company.bsi.BsiUnsigned;
import com.company.hybridewah.HybridBitmap;

public class CollaborativeFiltering {


    public static ArrayList<Long> getSlice(ArrayList<Integer> arr){
        int size = (int) Math.ceil((double) arr.size() / 64);
        ArrayList<Long> a= new ArrayList<>();
        long bitWord = 0;
        long bitDensity = 0;
        int last_w = 0;
        a.add(bitDensity);
        for (int i=0; i< arr.size(); i++){
            int w = (i / 64);
            int offset = i % 64;
            if (arr.get(i) > 2) {
                bitWord |= (1L << offset);
                bitDensity++;
            }
            if (w != last_w){
                a.add(bitWord);
                last_w = w;
                bitWord = 0;
            }
        }
        a.set(0,bitDensity);
        return a;
    }

    public static ArrayList<HybridBitmap> createBitmaps(String inputFile){
        ArrayList<HybridBitmap> bitmaps = new ArrayList<>();
        ArrayList<Long> tempArray = new ArrayList<>();
        ArrayList<ArrayList<Long>> bitSlices = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(" ");
                for (int i=0; i<values.length -1 ; i++){
                    long temp =  (Long.parseLong(values[i]));
                    if (temp > 3){  // threshold value based on rating scale
                        temp =1;
                    }else {
                        temp = 0;
                    }
                    tempArray.add(temp); //*** Temp array wil always have 0/1
                }
                BsiSigned bs = new BsiSigned();
                BsiAttribute bsi =  bs.buildBsiAttributeFromArray(tempArray.iterator(),tempArray.size(),0.2);
                bitmaps.add(bsi.getSlice(0));
                tempArray.clear();
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished building slices");

//        for (ArrayList<Long> slice: bitSlices) {
//            bitmaps.add(getHybridBitmap(slice,0.2));
//        }
        System.out.println("Finish Building bitmaps");
        return  bitmaps;
    }

    public  static  void storeSimilarityMatrix(String outputFile, ArrayList<ArrayList<Integer>> simMatrix) throws IOException {
        FileOutputStream fos = null;
        fos = new FileOutputStream(outputFile);
        ObjectOutputStream oos = null;
        oos = new ObjectOutputStream(fos);
        oos.writeObject(simMatrix);
        oos.close();
        System.out.println("Finished storing similarity matrix");
    }
    public static  ArrayList<ArrayList<Integer>> loadSimilarityMAtrix(String inputFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(inputFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<ArrayList<Integer>> simMatrix= (ArrayList<ArrayList<Integer>>) ois.readObject();
        ois.close();
        System.out.println("Finished loading bitmaps");
        return  simMatrix;
    }
    public static void storeHybridBitmaps(String outputFile, ArrayList<HybridBitmap> bitmaps) throws IOException {
        FileOutputStream fos = null;
        fos = new FileOutputStream(outputFile);
        ObjectOutputStream oos = null;
        oos = new ObjectOutputStream(fos);
        oos.writeObject(bitmaps);
        oos.close();
        System.out.println("Finished storing bitmaps");
    }

    public  static ArrayList<HybridBitmap> loadHybridBitmaps(String inputFile) throws IOException, ClassNotFoundException {
//        ArrayList<HybridBitmap> bitmaps = new ArrayList<>();
        FileInputStream fis = new FileInputStream(inputFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<HybridBitmap> bitmaps= (ArrayList<HybridBitmap>) ois.readObject();
        ois.close();
        System.out.println("Finished loading bitmaps");
        return  bitmaps;
    }


    public static HybridBitmap getHybridBitmap(ArrayList<Long> slice, double compressThreshold){
        float bitDensity = slice.get(0)/(float)slice.size();
        double compressRatio = 1-Math.pow((1-bitDensity), (2*64))-Math.pow(bitDensity, (2*64));
        if(compressRatio<compressThreshold){
            //build compressed bitmap
            HybridBitmap bitmap = new HybridBitmap();
            for(int j=1; j<slice.size(); j++){
                bitmap.add(slice.get(j));
            }
            bitmap.density=bitDensity;
            return bitmap;
        }else {
            //build verbatim Bitmap
            HybridBitmap bitmap = new HybridBitmap(true,slice.size()-1);
            for(int i=1; i<slice.size(); i++){
                bitmap.buffer[i-1]=slice.get(i);
            }
//            bitmap.actualsizeinwords=slice.size()-1;
            //bitmap.setSizeInBits(bitmapDataRaw[i].length*WORD);
//            bitmap.setSizeInBits((slice.size()-1)*64);
            bitmap.density=bitDensity;
            return bitmap;
        }
    }


    public static int findSimilarity(HybridBitmap bitmap_1, HybridBitmap bitmap_2){
        HybridBitmap similarity_bitmap = bitmap_1.and(bitmap_2);
        int similar_users = similarity_bitmap.cardinality();
        return  similar_users;
    }

    public  static ArrayList<ArrayList<Integer>> buildSimilarityMatrix(ArrayList<HybridBitmap> bitmaps){
        ArrayList<ArrayList<Integer>> similarityMatrix = new ArrayList<>();
        for(int i=0; i<bitmaps.size(); i++){
            HybridBitmap baseBitmap= bitmaps.get(i);
            ArrayList<Integer> userSimilarityList = new ArrayList<>();
            for(int j=0; j<bitmaps.size(); j++){
                int similarity = 0;
                if (j<i){
                    similarity = similarityMatrix.get(j).get(i);
                }else if (i==j){
                    similarity = 0;
                }else{
                    similarity = findSimilarity(baseBitmap,bitmaps.get(j));
                }
                userSimilarityList.add(similarity);
            }
            similarityMatrix.add(userSimilarityList);
        }
        System.out.println("Finished building similarity matrix");
        return similarityMatrix;
    }
// calculating KNN a distance metric ( Manhattan)

    public static ArrayList<Integer> findKNearest(ArrayList<Integer> array, int k){
        if (array == null){
            return new ArrayList<>();
        }
        if (k > array.size()){
            return array;
        }
        ArrayList<Integer> ans = new ArrayList<>();
        PriorityQueue<Integer> heap = new PriorityQueue<Integer>();
        HashMap<Integer, ArrayList<Integer>> valueIndexMap = new HashMap<>();
        for (int i=0; i<k; i++){
            heap.add(array.get(i));
            if (!valueIndexMap.containsKey(array.get(i))){
                valueIndexMap.put(array.get(i), new ArrayList<Integer>());
            }
            valueIndexMap.get(array.get(i)).add(i);
        }
        for(int i=k; i< array.size(); i++){
            if(array.get(i) > heap.peek()){
                int key = heap.poll();
                heap.add(array.get(i));
                if (valueIndexMap.get(key).size()>1){
                    valueIndexMap.get(key).remove(valueIndexMap.get(key).size()-1); // replace with pop()
                    if (!valueIndexMap.containsKey(array.get(i))){
                        valueIndexMap.put(array.get(i), new ArrayList<Integer>());
                    }
                    valueIndexMap.get(array.get(i)).add(i);
                }else{
                    valueIndexMap.remove(key);
                    if (!valueIndexMap.containsKey(array.get(i))){
                        valueIndexMap.put(array.get(i), new ArrayList<Integer>());
                    }
                    valueIndexMap.get(array.get(i)).add(i);
                }
            }
        }
        for (Map.Entry entry: valueIndexMap.entrySet()){
            ArrayList<Integer> value = (ArrayList<Integer>) entry.getValue();
            for (int i=0; i< value.size(); i++){
                ans.add(value.get(i));
            }
        }
        Collections.sort(ans);
        return ans;
    }

    public static long createHybridBitmapWord(long word, ArrayList<Integer> offsets ){
        for (int i=0; i< offsets.size(); i++){
            word |= (1L << offsets.get(i));
        }
        return  word;
    }

    public static HybridBitmap createHybridBitmapWithIndex(ArrayList<Integer> array, int bitmapSize){
        HybridBitmap hybridBitmap = new HybridBitmap();
        ArrayList<Integer> offsets = new ArrayList<>();
        ArrayList<Long> hybridBitmapWords = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        int position =  array.get(0)/64;
        offsets.add(array.get(0)%64);

        for (int i=1; i<array.size(); i++){
            int wordIndex = array.get(i)/64;
            int offset = array.get(i)%64;
            if (wordIndex != position){
                long word = createHybridBitmapWord(0L,offsets);
                hybridBitmapWords.add(word);
                indices.add(position);
                offsets.clear();
                position = wordIndex;
            }
            offsets.add(offset);
        }
        if (offsets.size() > 0){
            long word = createHybridBitmapWord(0L,offsets);
            hybridBitmapWords.add(word);
            indices.add(position);
        }

        int previousIndex = 0;
        for(int i=0; i<indices.size();i++){
            int numerOfEmptyWords = indices.get(i) - previousIndex -1;
            if (numerOfEmptyWords >0) {
                for(int j=0; j< numerOfEmptyWords; j++) {
                    hybridBitmap.add(0L);
                }
            }
            hybridBitmap.add(hybridBitmapWords.get(i));
            previousIndex = indices.get(i);
        }
        int numerOfEmptyWords = (int) (Math.ceil((bitmapSize/64)) - previousIndex + 1);
        for (int i=0; i< numerOfEmptyWords; i++){
            hybridBitmap.add(0L);
        }
//        if (numerOfEmptyWords > 0) {
//            hybridBitmap.addStreamOfEmptyWords(false, numerOfEmptyWords);
//        }
        hybridBitmap.density =  (bitmapSize/64)/(float)(array.size());
        return hybridBitmap;
    }

    public  static HybridBitmap getFilterBitmap(ArrayList<Integer> simArray, int k){
        ArrayList<Integer> indices = findKNearest(simArray,k);
        return createHybridBitmapWithIndex(indices,simArray.size());
    }


    public static ArrayList<Integer> recommendItemsToUser(ArrayList<ArrayList<Integer>> similariyMatrix, ArrayList<HybridBitmap> itemBitmaps,
                                                          ArrayList<HybridBitmap> userBitmaps, int userId, int userk, int itemK){
        HybridBitmap filter = getFilterBitmap(similariyMatrix.get(userId),userk);

        HybridBitmap userBitmap = userBitmaps.get(userId);
//        ArrayList<Integer> itemIndices;
        ArrayList<Integer> itemVotes = new ArrayList<>();
        int[] itemIndicesRatedByUser = userBitmap.getPositions();

        if (itemIndicesRatedByUser.length == 0){
            return null;
        }

        int userRatedIndex = 0;

        for (int i=0; i<itemBitmaps.size(); i++){
//            if(userId == i){
//                continue;
//            }
            if (i == itemIndicesRatedByUser[userRatedIndex]){
                if (userRatedIndex + 1 < itemIndicesRatedByUser.length ){
                    userRatedIndex ++;
                }
                itemVotes.add(0);
                continue;
            }

            HybridBitmap itemBitmap = itemBitmaps.get(i).and(filter);
            itemVotes.add(itemBitmap.cardinality());
        }

        //return itemVotes;
        return  findKNearest(itemVotes,itemK);
    }

    public  static  ArrayList<Integer>  getTestUsers(String testUserFileName){
        ArrayList<Integer> testUsers = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(testUserFileName));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                for (int i=0; i<values.length; i++){
                    if(values[i].isEmpty()){
                        continue;
                    }
                    int temp =  (Integer.parseInt(values[i]));
                    testUsers.add(temp);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  testUsers;
    }

    public static  void recommendMoviesForAllTestUsers(ArrayList<Integer> testUsers, String outputFile, ArrayList<ArrayList<Integer>> similariyMatrix,
                                                       ArrayList<HybridBitmap> itemBitmaps, ArrayList<HybridBitmap> userBitmaps,
                                                       int[] userks, int[] itemKs) throws IOException {

        File file = new File(outputFile);
//        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        ArrayList<Integer> itemIndices;
        for(int i=0; i<testUsers.size(); i++){
            for (int u_k=0; u_k <userks.length; u_k++){
                for (int it_k=0; it_k < itemKs.length; it_k++){
                    itemIndices = recommendItemsToUser(similariyMatrix,itemBitmaps,userBitmaps,testUsers.get(i),userks[u_k],itemKs[it_k]);
                    if(itemIndices == null){ // if they are no top(itemK) number of  items for a user
                        continue;
                    }
                    writer.write(testUsers.get(i).toString()+ ", ");
                    writer.write(userks[u_k] + ", ");
                    writer.write(itemKs[it_k]+", ");
                    for (int re=0; re < itemIndices.size(); re++){
                        writer.write(itemIndices.get(re).toString()+", ");
                    }
                    writer.write("\n");
                }
            }
        }
//        writer.write(testUsers.toString());
        writer.close();
        System.out.println("Finished storing similarity matrix");
    }
}



