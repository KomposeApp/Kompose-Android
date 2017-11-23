package ch.ethz.inf.vs.kompose;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.inf.vs.kompose.databinding.ActivityDesignBinding;
import ch.ethz.inf.vs.kompose.databinding.ClientViewBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.BindableAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.BindableViewHolder;
import ch.ethz.inf.vs.kompose.view.adapter.ClientAdapter;
import ch.ethz.inf.vs.kompose.view.viewmodel.DesignViewModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.ClientViewHolder;

public class DesignActivity extends BaseServiceActivity {

    private static final String LOG_TAG = "## Design Acitivty";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SampleService sampleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_design);
        mRecyclerView = findViewById(R.id.my_recycler);
        sampleService = getSampleService();

        final DesignViewModel designViewModel = new DesignViewModel(sampleService.getClients());

        binding.myRecycler.setLayoutManager(new LinearLayoutManager(this));
        BindableAdapter<ClientModel> adapter = new BindableAdapter<>(designViewModel.getClients(), new BindableAdapter.ViewHolderFactory<ClientModel>() {
            @Override
            public BindableViewHolder<ClientModel> create(ViewGroup viewGroup) {
                return new ClientViewHolder(ClientViewBinding.inflate(getLayoutInflater(), viewGroup, false), designViewModel);
            }
        });

        Log.d(LOG_TAG, "setting adapter");
        mRecyclerView.setAdapter(adapter);
        Log.d(LOG_TAG, "set adapter");


        Log.d(LOG_TAG, "setting data");
        binding.setDesignViewModel(designViewModel);
        Log.d(LOG_TAG, "set data");


        sampleService.getClients().get(0).setName("my new bound name");
    }

    public void onClickFriend(View view) {
        sampleService.addMoreClients();
    }

    private Thread thread;

    private ActivityDesignBinding binding;


}
