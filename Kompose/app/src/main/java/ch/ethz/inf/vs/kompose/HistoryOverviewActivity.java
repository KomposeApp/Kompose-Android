package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityHistoryOverviewBinding;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.view.adapter.PastSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.PastSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.HistoryOverviewViewModel;

public class HistoryOverviewActivity extends AppCompatActivity implements PastSessionViewHolder.ClickListener {

    private static final String LOG_TAG = "## History Activity";
    private final HistoryOverviewViewModel viewModel = new HistoryOverviewViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_overview);


        ActivityHistoryOverviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_history_overview);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new PastSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);


        if (MainActivity.DESIGN_MODE) {
            SampleService sampleService = new SampleService();
            viewModel.getSessionModels().add(sampleService.getSampleSession("session 1"));
            viewModel.getSessionModels().add(sampleService.getSampleSession("session 2"));
            viewModel.getSessionModels().add(sampleService.getSampleSession("session 3"));
        }
    }

    @Override
    public void onClick(View v, int position) {
        Log.d(LOG_TAG, "model selected at position " + position);

        StateSingleton.getInstance().activeHistorySession = viewModel.getSessionModels().get(position);

        Intent playlistIntent = new Intent(this, HistoryDetailsActivity.class);
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
