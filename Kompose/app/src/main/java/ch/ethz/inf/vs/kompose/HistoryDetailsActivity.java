package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import ch.ethz.inf.vs.kompose.databinding.ActivityHistoryDetailsBinding;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.view.adapter.PlayedSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.PlayedSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.HistoryDetailsViewModel;

public class HistoryDetailsActivity extends AppCompatActivity implements PlayedSongViewHolder.ClickListener {

    //TODO: If there's time, maybe allow expanding the song to see more details

    private final String LOG_TAG = "## Details Activity";

    private final HistoryDetailsViewModel viewModel = new HistoryDetailsViewModel(
            StateSingleton.getInstance().getActiveHistorySession()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Viewing details for Playlist: " + viewModel.getSessionModel().getName());

        setTitle("Playlist: " + viewModel.getSessionModel().getName());
        ActivityHistoryDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_history_details);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new PlayedSongAdapter(viewModel.getSessionModel().getAllSongs(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
    }
}
