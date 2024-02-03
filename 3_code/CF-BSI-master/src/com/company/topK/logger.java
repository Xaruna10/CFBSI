package com.company.topK;
import java.io.BufferedWriter;
import java.io.FileWriter;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gcanahuate
 */
public class logger {
    String outfile;
    boolean toFile = true;
    FileWriter logfile;
    BufferedWriter blogfile;
    boolean verbose = false;
    
    public logger () {        
    }
    public logger (String outF) {
        outfile = outF;
        this.initializeLogging(outF);
    }
    public void initializeLogging (String outF) {
        if (this.toFile)
        try {
            outfile = outF;
            logfile = new FileWriter(outF);
            blogfile = new BufferedWriter(logfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void closeFiles () {
        if (this.toFile) try {
            blogfile.close();
            logfile.close();                 
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void log(String str) {
        if (this.toFile)
        try {
            blogfile.write(str);        
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (verbose) {
            System.out.print(str);
        }
    }
}
