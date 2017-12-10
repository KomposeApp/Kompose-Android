package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityHistoryOverviewBinding;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.StorageHandler;
import ch.ethz.inf.vs.kompose.view.adapter.PastSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.PastSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.HistoryOverviewViewModel;

public class HistoryOverviewActivity extends AppCompatActivity implements PastSessionViewHolder.ClickListener {

    private final String LOG_TAG = "##HistoryActivity";
    private final HistoryOverviewViewModel viewModel = new HistoryOverviewViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHistoryOverviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_history_overview);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new PastSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);

        StorageHandler storageHandler = new StorageHandler(this);
        storageHandler.load(viewModel.getSessionModels());

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.history_overview_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v, int position) {
        Log.d(LOG_TAG, "model selected at position " + position);

        StateSingleton.getInstance().setActiveHistorySession(viewModel.getSessionModels().get(position));

        Intent playlistIntent = new Intent(this, HistoryDetailsActivity.class);
        startActivity(playlistIntent);
    }
}
