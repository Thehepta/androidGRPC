package org.example;



import org.jf.baksmali.fix.FixDumpClassCodeItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public class baksmali {

    public static void main(String[] args) {
        String dexPath = "src/test/resources/FixTest/c078d7b9.dex";
        String fixPath = "src/test/resources/FixTest/c078d7b9.data";
        String out = Integer.toHexString(dexPath.hashCode());

        int jobs = 1;

        Map<String, FixDumpClassCodeItem> fixDumpClassCodeItem = null;
        try {
            FileInputStream fileIn = new FileInputStream(fixPath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            fixDumpClassCodeItem = (Map<String, FixDumpClassCodeItem>) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("Map deserialized successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
//        FixMain fixMain = new FixMain();
//        fixMain(dexPath,out,jobs,null,fixDumpClassCodeItem);

    }
}
