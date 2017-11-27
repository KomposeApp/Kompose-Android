package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityConnectBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.ClientNetworkService;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.view.adapter.JoinSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.JoinSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.ConnectViewModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class ConnectActivity extends BaseActivity implements JoinSessionViewHolder.ClickListener {

    private static final String LOG_TAG = "## Connect Activity";
    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;

    private final ConnectViewModel viewModel = new ConnectViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //get binding & bind viewmodel to view
        ActivityConnectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new JoinSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);

        //bind client network service
        Intent intent = new Intent(this, ClientNetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        if (MainActivity.DESIGN_MODE) {
            SampleService sampleService = new SampleService();
            viewModel.getSessionModels().add(sampleService.getSampleSession("design session"));
            viewModel.getSessionModels().add(sampleService.getSampleSession("design session 1"));
            viewModel.getSessionModels().add(sampleService.getSampleSession("design session 2"));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ClientNetworkService.LocalBinder binder = (ClientNetworkService.LocalBinder) service;
            clientNetworkService = binder.getService();
            clientNetworkServiceBound = true;
            clientNetworkService.findNetworkServices(viewModel.getSessionModels());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            clientNetworkServiceBound = false;
        }
    };

    @Override
    public void joinButtonClicked(View v, int position) {
        Log.d(LOG_TAG, "pressed join button of item number " + position);
        SessionModel pressedSession = viewModel.getSessionModels().get(position);
        String clientName = viewModel.getClientName();

        if (clientName == null) {
            showError(getString(R.string.choose_client_name));
            return;
        }

        //todo technical: am I doing this right?
        ClientModel clientModel = new ClientModel(StateSingleton.getInstance().deviceUUID, pressedSession);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);
        pressedSession.getClients().add(clientModel);

        StateSingleton.getInstance().activeSession = pressedSession;


        // start the client service
        Intent serverIntent = new Intent(this, ClientNetworkService.class);
        startService(serverIntent);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
