package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.BindableAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.ClientBindableAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.RecyclerViewClickListener;
import ch.ethz.inf.vs.kompose.view.viewmodel.DesignViewModel;

public class DesignActivity extends BaseServiceActivity {

    private static final String LOG_TAG = "## Design Acitivty";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SampleService sampleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDesignBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_design);
        mRecyclerView = findViewById(R.id.my_recycler);
        sampleService = getSampleService();

        final DesignViewModel designViewModel = new DesignViewModel(sampleService.getClients());

        binding.myRecycler.setLayoutManager(new LinearLayoutManager(this));
        BindableAdapter adapter = new ClientBindableAdapter(designViewModel.getClients(), getLayoutInflater(), listener);
        binding.myRecycler.setAdapter(adapter);

        sampleService.getClients().get(0).setName("my new bound name");
    }

    RecyclerViewClickListener listener = new RecyclerViewClickListener() {
        @Override
        public void recyclerViewListClicked(View v, int position) {
            Log.d(LOG_TAG, "pressed item number " + position);
        }
    };

    public void onClickFriend(View view) {
        sampleService.addMoreClients();
    }
}
