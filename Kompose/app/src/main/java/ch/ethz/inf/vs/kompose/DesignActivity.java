package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.view.adapter.DesignClientAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.DesignClientViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.DesignViewModel;

public class DesignActivity extends AppCompatActivity implements DesignClientViewHolder.ClickListener {

    private static final String LOG_TAG = "## Design Acitivty";

    private SampleService sampleService = new SampleService();
    private final DesignViewModel viewModel = new DesignViewModel(sampleService.getClients());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDesignBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_design);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new DesignClientAdapter(viewModel.getClients(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
    }

    public void onClickFriend(View view) {
        sampleService.addMoreClients();
    }

    @Override
    public void buttonClicked(View v, int position) {
        Log.d(LOG_TAG, "pressed button of item number " + position);
    }

    @Override
    public void itemClicked(View v, int position) {
        Log.d(LOG_TAG, "pressed item number " + position);
    }
}
