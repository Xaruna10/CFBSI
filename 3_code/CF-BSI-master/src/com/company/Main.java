package com.company;
import com.company.hybridewah.HybridBitmap;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString()+"/";

        String currentDirectory = s;
        String filename1 = currentDirectory + "movie100kdataset/users_m.txt";
        String filename2 = currentDirectory + "movie100kdataset/items_m.txt";
        String userSimilarityFile = currentDirectory + "movie100kdataset/user_similarity_matrix.tmp";
        String userBitmapFile = currentDirectory + "movie100kdataset/user_bitmap.tmp";
        String itemBitmapFile = currentDirectory + "movie100kdataset/item_bitmap.tmp";
        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering();
        ArrayList<HybridBitmap> userbitmaps = collaborativeFiltering.createBitmaps(filename1);
        collaborativeFiltering.storeHybridBitmaps(userBitmapFile,userbitmaps);
        ArrayList<HybridBitmap> itembitmaps = collaborativeFiltering.createBitmaps(filename2);
        collaborativeFiltering.storeHybridBitmaps(itemBitmapFile,itembitmaps);

        ArrayList<HybridBitmap> userBitmaps = collaborativeFiltering.loadHybridBitmaps(userBitmapFile);
        ArrayList<HybridBitmap> itemBitmaps = collaborativeFiltering.loadHybridBitmaps((itemBitmapFile));

        ArrayList<ArrayList<Integer>> similariyMatrix1 = collaborativeFiltering.buildSimilarityMatrix(userBitmaps); // we are only doing user based collaborative filtering hence passed userbitmaps
        collaborativeFiltering.storeSimilarityMatrix(userSimilarityFile,similariyMatrix1);
        String testUserFile = currentDirectory + "movie100kdataset/test_users.txt"; /// alternatively can be passed manually as array list below , file generated from notebook
        String testRecommendationsFile = currentDirectory + "movie100kdataset/test_recommendation_bsi.txt";
        String bsi_recommendations_for_all_users = currentDirectory + "movie100kdataset/bsi_recommendations_for_all_users.txt";
        ArrayList<ArrayList<Integer>> similariyMatrix = collaborativeFiltering.loadSimilarityMAtrix(userSimilarityFile);
        // code analysis
        System.out.println("Finished!!");


        // func to read from input test_users.txt(test_users_list generated from notebook) , can be passed manually. and storing them as arraylist
        ArrayList<Integer> testUsers = collaborativeFiltering.getTestUsers(testUserFile);
        System.out.println("testUsers: "+
                testUsers.toArray().toString());
        // ##  printing test_users_list from the notebook  ( unique users from the test dataset)
        for (int i=0; i< testUsers.size(); i++){
            System.out.print(testUsers.get(i)+",");
        }

        int[] userKs = {5}; // final run ?{5, 10,20,25}
        int[] itemKs = {5, 10, 25, 50 ,100};

        // tracking total run time for all different combinations of userKs and itemKs
        collaborativeFiltering.recommendMoviesForAllTestUsers(testUsers,testRecommendationsFile,similariyMatrix,itemBitmaps,userBitmaps,userKs,itemKs);



        File file = new File(bsi_recommendations_for_all_users);
//        file.createNewFile();
        FileWriter writer = new FileWriter(file);


        ArrayList<Integer> itemIndices;
        Map<Integer, List<Integer>> userMap = new HashMap<>();
        for (int i=1; i<6040; i++){ //adjust range to user set max value in test_users.txt, 944 ,6040
                Map<Integer,Double> movieMap = new HashMap<>();
                itemIndices = collaborativeFiltering.recommendItemsToUser(similariyMatrix,itemBitmaps,userBitmaps,i,20,50);
                if(itemIndices == null){ // if they are no top 50(itemK) items for a user
                    System.out.println("Missing id: "+i);
                    continue;
                }
                writer.write(i+ ", ");
//                writer.write(userks[u_k] + ", ");
//                writer.write(itemKs[it_k]+", ");

                for (int re=0; re < itemIndices.size(); re++){
                    writer.write(itemIndices.get(re).toString()+", ");
                }
                writer.write("\n");
        }
        writer.close();
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Tme taken: " + timeElapsed + " ms");
        System.out.println("Finished storing bsi_recommendations_for_all_users");

        // below lines are not used, just for understanding filter bitmap index function
        // bsi ~=cbv
        //
//        ArrayList<Integer> tempArray = similariyMatrix.get(1);
//        ArrayList<Integer> indexList = collaborativeFiltering.findKNearest(tempArray,10);
//        for (int i=0; i<indexList.size(); i++){
//            System.out.print(indexList.get(i)+",");
//        }
//        System.out.println();
//
//        System.out.println("filter bitmap index: ");
//        ArrayList<Integer> indexList1 = new ArrayList<Integer>(Arrays.asList(
//                5,10,30,55,56,89,90,99,140
//        ));
//        HybridBitmap filterBitmap = collaborativeFiltering.createHybridBitmapWithIndex(indexList1,610);
//        int[] indices = filterBitmap.getPositions();
//        for (int i=0; i< indices.length; i++){
//            System.out.print(indices[i]+",");
//        }
//        System.out.println();
        // above lines are not used anywhere ..jus for understanding filter bitmap index function

//        ArrayList<Integer> itemIndices = collaborativeFiltering.recommendItemsToUser(similariyMatrix,itemBitmaps,userBitmaps,442,10,40); //for single user ,ran this function below all users in for loop



    }
}
