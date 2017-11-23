package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.model.SessionModel;

public class HistoryOverviewActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## History Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_overview_placeholder);
    }

    public void examineHistoryItem() {
        //TODO: Select the correct item to examine out of the listview
        Log.d(LOG_TAG, "Item selected");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
    }


    /**
     * gets all sessions which are persisted on storage
     *
     * @return collection of all saves sessions
     */
//    public ObservableList<SessionModel> getPastSessions() {
//        String[] pastSessionStrings = getStorageService().retrieveAllFiles(DIRECTORY_ARCHIVE);
//        for (String pastSession : pastSessionStrings) {
//            try {
//                SessionConverter sessionConverter = new SessionConverter();
//                SessionModel sessionModel = sessionConverter.convert(
//                        JsonConverter.fromSessionJsonString(pastSession)
//                );
//                pastSessions.add(sessionModel);
//            } catch (IOException e) {
//            }
//        }
//        return pastSessions;
//    }
}
