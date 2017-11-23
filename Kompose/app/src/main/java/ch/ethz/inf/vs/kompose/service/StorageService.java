package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles loading/storing data to storage.
 */
public class StorageService {

    private static final String LOG_TAG = "## StorageService";
    private Context context;

    public StorageService(Context context) {
        this.context = context;
    }

    /**
     * Persists the file to internal storage
     *
     * @param fileName  file will be overwritten if it already exists
     * @param content   file content
     * @param directory directory will be created if it doesn't exist
     * @return boolean indication whether the operation has succeeded or not
     */
    public boolean persist(String directory, String fileName, String content) {
        try {
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

            // write to storage
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        return false;
    }

    private String readFile(File file) {
        try {
            FileInputStream input = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            input.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
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
