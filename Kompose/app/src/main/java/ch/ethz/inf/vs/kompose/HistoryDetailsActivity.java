package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.ethz.inf.vs.kompose.databinding.ActivityHistoryDetailsBinding;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.StorageHandler;
import ch.ethz.inf.vs.kompose.view.adapter.PlayedSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.PlayedSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.HistoryDetailsViewModel;

public class HistoryDetailsActivity extends AppCompatActivity implements PlayedSongViewHolder.ClickListener {

    private final String LOG_TAG = "##Details Activity";

    private final HistoryDetailsViewModel viewModel = new HistoryDetailsViewModel(
            StateSingleton.getInstance().getActiveHistorySession()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Viewing details for Playlist: " + viewModel.getSessionModel().getName());

        ActivityHistoryDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_history_details);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new PlayedSongAdapter(viewModel.getSessionModel().getAllSongs(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.history_details_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_historydetails, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Delete all history contents
        switch (item.getItemId()) {
            case R.id.history_toolbar_deletethis:
                new StorageHandler(this).deleteSelected(
                        StateSingleton.getInstance().getActiveHistorySession().getCreationDateTime().toString());
                StateSingleton.getInstance().setActiveHistorySession(null);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
