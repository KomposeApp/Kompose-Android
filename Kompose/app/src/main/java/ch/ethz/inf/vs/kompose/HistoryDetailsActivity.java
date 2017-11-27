package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.databinding.ActivityHistoryDetailsBinding;
import ch.ethz.inf.vs.kompose.view.adapter.DesignClientAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.PlayedSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.PlayedSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.ConnectViewModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.HistoryDetailsViewModel;

public class HistoryDetailsActivity extends AppCompatActivity implements PlayedSongViewHolder.ClickListener {

    private static final String LOG_TAG = "## Details Activity";

    private final HistoryDetailsViewModel viewModel = new HistoryDetailsViewModel(null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);


        ActivityHistoryDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_design);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new PlayedSongAdapter(viewModel.getSessionModel().getSongs(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
    }
}
