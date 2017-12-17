package ch.ethz.inf.vs.kompose.service.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.model.SessionModel;

/**
 * Service that handles loading/storing data to storage.
 */
public class StorageHandler {

    private final String LOG_TAG = "##StorageHandler";
    private final String SESSION_FOLDER = "sessions";
    private Context context;

    public StorageHandler(Context context) {
        this.context = context;
    }

    /**
     * Accessor method to persist the given session
     * @param session session to persist
     */
    public void persist(Session session) {
        try {
            String content = JsonConverter.toJsonString(session);
            boolean success = persist(SESSION_FOLDER, session.getCreationDateTime(), content);
            if (!success) Log.e(LOG_TAG, "Failed to persist session: " + session.getSessionName());
        } catch (JsonProcessingException e) {
            Log.e(LOG_TAG, "Failed to persist session: " + session.getSessionName());
            e.printStackTrace();
        }
    }

    /**
     * Load stored history sessions into the provided list object
     * @param sessionList List to load SessionModels into
     */
    public void load(final List<SessionModel> sessionList) {
        String[] files = retrieveAllFiles(SESSION_FOLDER);
        SessionConverter sessionConverter = new SessionConverter();
        if (files != null) {
            for (String file : files) {
                try {
                    Session content = JsonConverter.fromSessionJsonString(file);
                    final SessionModel model = sessionConverter.convert(content);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            sessionList.add(model);
                        }
                    });
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to convert stored sessions.");
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteSelected(String filename){
        deleteFile(SESSION_FOLDER, filename);
    }

    public void deleteAll(){
        clearDirectory(SESSION_FOLDER, false);
    }

    /**
     * Persists the file to internal storage
     *
     * @param fileName  name of the file (will be overwritten if already exists)
     * @param content   file content
     * @param directory where the file is stored relative to the app path (automatic creation)
     * @return boolean indication whether the operation has succeeded or not
     */
    private boolean persist(String directory, String fileName, String content) {
        String child = fileName;

        if (fileName.isEmpty()) {
            Log.e(LOG_TAG, "Filename was empty");
            return false;
        }

        // create directory
        if (directory != null && directory.length() > 0) {
            File dir = new File(context.getFilesDir(), directory);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    Log.e(LOG_TAG, "Creating directory failed");
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
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "Malformed file passed to method.", fnf);
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        //Try reading from InputStream
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            input.close();
        } catch (IOException io) {
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
    private String[] retrieveAllFiles(String directory) {
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
            fileContents[i] = readFile(regularFiles.get(i));
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
    private String retrieveFile(String directory, String fileName) {

        String child = fileName;
        if (directory != null && directory.length() > 0) {
                child = directory + "/" + fileName;
        }

        File file = new File(context.getFilesDir(), child);
        return readFile(file);
    }

    /**
     * Delete given filename in directory
     * @param directory directory to search in
     * @param fileName file to delete
     */
    private void deleteFile(String directory, String fileName){

        // Check if the directory exists
        File file = new File(context.getFilesDir(), directory);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        File[] children = file.listFiles();
        for (File f : children) {
            if (f.getName().equals(fileName)) {
                if (f.delete()) {
                    Log.d(LOG_TAG, "Successfully deleted file: " + fileName);
                    return;
                }
                else{
                    Log.e(LOG_TAG, "Failed to delete file: " + fileName);
                    return;
                }
            }
        }
        Log.e(LOG_TAG, "File not found: " + fileName);
    }


    /**
     * Delete contents of a directory.
     */
    private void clearDirectory(String directory, boolean recursive){

        // Check if the directory exists
        File file = new File(context.getFilesDir(), directory);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        File[] children = file.listFiles();
        for (File f : children) {
            String filename = f.getName();
            if (f.isFile()) {
                if (f.delete()) Log.d(LOG_TAG, "Successfully deleted file: " + filename);
                else Log.e(LOG_TAG, "Failed to delete file: " + filename);
            }
            else if (f.isDirectory() && recursive){
                clearDirectory(f.getPath(), true);
                if (f.delete()) Log.d(LOG_TAG, "Successfully deleted directory: " + filename);
                else Log.e(LOG_TAG, "Failed to delete directory: " + filename);
            }
            else{
                Log.e(LOG_TAG, "Failed to delete: " + f.getPath());
            }
        }
    }
}
