package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private int lastIndex = -1;

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
    public void onResume() {
        if (lastIndex > -1 && StateSingleton.getInstance().getActiveHistorySession() == null){
            viewModel.getSessionModels().remove(lastIndex);
        }
        lastIndex = -1;
        super.onResume();
    }

    @Override
    public void onClick(View v, int position) {
        Log.d(LOG_TAG, "model selected at position " + position);
        lastIndex = position;

        StateSingleton.getInstance().setActiveHistorySession(viewModel.getSessionModels().get(position));

        Intent detailsIntent = new Intent(this, HistoryDetailsActivity.class);
        startActivity(detailsIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_historyoverview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Delete all history contents
        switch (item.getItemId()) {
            case R.id.history_toolbar_deleteall:
                new StorageHandler(this).deleteAll();
                viewModel.getSessionModels().clear();
                StateSingleton.getInstance().setActiveHistorySession(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
