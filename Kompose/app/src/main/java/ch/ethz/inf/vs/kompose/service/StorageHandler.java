package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles loading/storing data to storage.
 */
public class StorageHandler {

    private static final String LOG_TAG = "## StorageHandler";
    private Context context;

    public StorageHandler(Context context) {
        this.context = context;
    }

    /**
     * Persists the file to internal storage
     *
     * @param fileName  name of the file (will be overwritten if already exists)
     * @param content   file content
     * @param directory where the file is stored relative to the app path (automatic creation)
     * @return boolean indication whether the operation has succeeded or not
     */
    public boolean persist(String directory, String fileName, String content) {
        String child = fileName;

        // create directory
        if (directory != null && directory.length() > 0) {
            File dir = new File(context.getFilesDir(), directory);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    return false;
                }
            }
            child = directory + "/" + fileName;
        }

        // create file
        Log.d(LOG_TAG, "writing file: " + child);
        File file = new File(context.getFilesDir(), child);

        try {
            // write to storage
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException io) {
            // failure case
            Log.e(LOG_TAG, io.getMessage());
            return false;
        }

        return true;
    }

    @Nullable
    private String readFile(File file) {

        FileInputStream input;
        // Load file into inputstream
        try{
            input = new FileInputStream(file);
        } catch(FileNotFoundException fnf){
            Log.e(LOG_TAG, "Malformed file passed to method.", fnf);
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        //Try reading from InputStream
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            input.close();
        }catch(IOException io){
            Log.e(LOG_TAG, "Reading from InputStream failed.", io);
            return null;
        }

        return stringBuilder.toString();
    }

    /**
     * returns an array with the content of all files in the directory
     *
     * @param directory the directory to be traversed
     * @return an array of strings with the file contents
     */
    public String[] retrieveAllFiles(String directory) {
        File file = new File(context.getFilesDir(), directory);
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        File[] children = file.listFiles();
        List<File> regularFiles = new ArrayList<>();
        int numFiles = 0;
        for (File f : children) {
            if (f.isFile()) {
                regularFiles.add(f);
                numFiles++;
            }
        }

        String[] fileContents = new String[numFiles];
        for (int i = 0; i < numFiles; i++) {
            try {
                fileContents[i] = readFile(regularFiles.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fileContents;
    }

    /**
     * retrieves the file with the specified filename in the corresponding folder
     *
     * @param fileName  the name of the requested file
     * @param directory the directory in which the requested file resides
     * @return a string with the file content
     */
    public String retrieveFile(String directory, String fileName) {
        try {
            String child = fileName;
            if (directory != null && directory.length() > 0) {
                child = directory + "/" + fileName;
            }

            File file = new File(context.getFilesDir(), child);
            return readFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
