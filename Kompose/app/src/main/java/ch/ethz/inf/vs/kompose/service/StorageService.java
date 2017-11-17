package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Service that handles loading/storing data to storage.
 */
public class StorageService {
    private static String LOG_TAG = "StorageService";

    private Context context;

    public StorageService(Context context) {
        this.context = context;
    }

    /**
     * persists the file to storage
     *
     * @param fileName   the filename. file will be overwritten if found
     * @param content    the content of the file
     * @param folderName the folder. folder will be created if not found
     * @return boolean indication whether the operation has succeeded or not
     */
    public boolean persist(String fileName, String content, String folderName) {
        try {
            //create folder
            File folder = new File(context.getFilesDir(), folderName);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    return false;
                }
            }

            //create file
            File file = new File(context.getFilesDir(), folderName + fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }

            //write to disk
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        return false;
    }

    /**
     * returns an array with the content of all files in the folder
     *
     * @param folderName the folder to be traversed
     * @return an array of strings with the file contents
     */
    public String[] retrieveAllFiles(String folderName) {
        //TODO
        return new String[0];
    }

    /**
     * retrieves the file with the specified filename in the corresponding folder
     *
     * @param fileName   the name of the requested file
     * @param folderName the folder in which the requested file resides
     * @return a string with the file content
     */
    public String retrieveFile(String fileName, String folderName) {
        //TODO
        return "";
    }
}
