package com.archapp.coresmash.utlis;

import java.io.BufferedReader;
import java.io.IOException;

public class FileUtils {

    private FileUtils() {
    }


    public static String fileToString(BufferedReader reader) {
        StringBuilder buffer = new StringBuilder();

        String ls = System.getProperty("line.separator");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(ls);
            }
            return buffer.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "error.. :C";
        }
    }
}