package ch.ethz.inf.vs.kompose;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;

import org.joda.time.DateTime;

import java.io.IOException;
import java.net.SocketException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityMainBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.client.ClientNetworkService;
import ch.ethz.inf.vs.kompose.service.host.HostServerService;
import ch.ethz.inf.vs.kompose.view.mainactivity.MainActivityPagerAdapter;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

public class MainActivity extends BaseActivity implements MainViewModel.ClickListener {

    private final String LOG_TAG = "## Main Activity";

    public static final String KEY_NETWORKSERVICE = "ClientNetworkService";
    public static final String KEY_SERVERSERVICE = "HostServerService";
    public static final String SERVICE_NAME = "Kompose";
    public static final String SERVICE_TYPE = "_kompose._tcp";
    public static final String SERVICE_TYPE_NSD = "_kompose._tcp.";

    public static final boolean DESIGN_MODE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        //hide top bar
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Initialize the preference utility, and sets a flag to prevent ShareActivity from killing Kompose
        StateSingleton.getInstance().setStartedFromMainActivity();
        StateSingleton.getInstance().setPreferenceUtility(this);

        int currentPreload = StateSingleton.getInstance().getPreferenceUtility().getPreload();
        int currentCacheSize = StateSingleton.getInstance().getPreferenceUtility().getCurrentCacheSize();
        StateSingleton.getInstance().initializeSongCache(currentPreload, currentCacheSize);

        // Insert default username
        viewModel.setClientName(StateSingleton.getInstance().getPreferenceUtility().getUsername());
        viewModel.setSessionName(StateSingleton.getInstance().getPreferenceUtility().getSessionName());


        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(viewModel);

        if (MainActivity.DESIGN_MODE) {
            SampleService sampleService = new SampleService();
            for (int i = 0; i < 15; i++) {
                viewModel.getSessionModels().add(sampleService.getSampleSession("design session " + i));
            }
        }

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewPager viewPager = findViewById(R.id.viewPager);
        final PagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager(), viewModel);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //bind client network service
        Intent intent = new Intent(this.getBaseContext(), ClientNetworkService.class);
        bindService(intent, cNetServiceConnection, BIND_AUTO_CREATE);
    }


    private final MainViewModel viewModel = new MainViewModel(this);

    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;


    /*
      Note on unbinding the ClientNetworkService:
        * If it occurs before we join a room, it simply kills the service and the NSD Listener
        * If it occurs after we join a room, it will simply disconnect the service from this
          activity, and live on. This service will then be killed at some other point.
     */
    private ServiceConnection cNetServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "ClientNetworkService bound");
            ClientNetworkService.LocalBinder binder = (ClientNetworkService.LocalBinder) service;
            clientNetworkService = binder.getService();
            clientNetworkServiceBound = true;
            clientNetworkService.findNetworkServices(viewModel.getSessionModels());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG_TAG, "ClientNetworkService died");
            clientNetworkService = null;
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "Binding with ClientNetworkService died");
            clientNetworkService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientNetworkServiceBound) {
            unbindService(cNetServiceConnection);
            clientNetworkServiceBound = false;
        }
        StateSingleton.getInstance().clearCache();
    }


    @Override
    public void joinSessionClicked(SessionModel sessionModel) {
        String clientName = viewModel.getClientName();

        // Client's name must not be empty
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.view_error_clientname));
            return;
        }
        clientName = clientName.trim();


        // Add ourselves to the current session locally
        UUID deviceUUID = StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID();
        ClientModel clientModel = new ClientModel(deviceUUID, sessionModel);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);
        sessionModel.getClients().add(clientModel);

        StateSingleton.getInstance().setActiveSession(sessionModel);
        StateSingleton.getInstance().setActiveClient(clientModel);
        try {
            if (!clientNetworkServiceBound && clientNetworkService == null)
                throw new IllegalStateException("Failed to properly set up Client Network Service");

            RegistrationListener listener = new RegistrationListener(this);
            clientNetworkService.initSocketListener();
            clientNetworkService.registerClientOnHost(listener, clientName);
        } catch (SocketException s) {
            s.printStackTrace();
            try {
                clientNetworkService.closeClientSocket();
            } catch (IOException e) {
                Log.w(LOG_TAG, "Cleaning up the Client Socket failed.");
                e.printStackTrace();
            }
            showError("Failed to set up connection.");
        } catch (IllegalStateException | IOException io) {
            io.printStackTrace();
            showError("Failed to set up connection.");
        }
    }

    @Override
    public void joinManualClicked() {
        //todo: join manually

    }

    @Override
    public void openHelpClicked() {
        //todo: open help
    }

    @Override
    public void openHistoryClicked() {
        Intent playlistIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(playlistIntent);
    }

    @Override
    public void createSessionClicked() {
        String clientName = viewModel.getClientName();
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.view_error_clientname));
            return;
        }
        String sessionName = viewModel.getSessionName();
        if (sessionName == null || sessionName.trim().isEmpty()) {
            showError(getString(R.string.view_error_sessionname));
            return;
        }

        //Remove trailing whitespaces
        clientName = clientName.trim();
        sessionName = sessionName.trim();

        //Retrieve device UUID from preferences
        UUID deviceUUID = StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID();

        // create a new session
        SessionModel newSession = new SessionModel(UUID.randomUUID(), deviceUUID, true);
        newSession.setName(sessionName);
        newSession.setCreationDateTime(DateTime.now());

        ClientModel clientModel = new ClientModel(deviceUUID, newSession);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);
        newSession.getClients().add(clientModel);

        StateSingleton.getInstance().setActiveClient(clientModel);
        StateSingleton.getInstance().setActiveSession(newSession);

        // start the server service
        Intent serverIntent = new Intent(this, HostServerService.class);
        startService(serverIntent);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        playlistIntent.putExtra(MainActivity.KEY_SERVERSERVICE, serverIntent);
        startActivity(playlistIntent);
    }


    /**
     * Listener which allows us to start the next Activity.
     * Always make sure this is started on the main thread.
     * Needs to close the ServerSocket in the client in case the connection fails
     */
    private class RegistrationListener implements SimpleListener<Boolean, Void> {

        private MainActivity connectActivity;

        private RegistrationListener(MainActivity connectActivity) {
            this.connectActivity = connectActivity;
        }

        @Override
        public void onEvent(Boolean success, Void v) {
            try {
                if (!success) {
                    Log.e(LOG_TAG, "Failed to establish connection with host");
                    if (!connectActivity.isDestroyed()) {
                        connectActivity.showError(getString(R.string.view_error_connection_failed));
                    }
                    clientNetworkService.closeClientSocket();
                    return;
                }

                // In case we closed the activity while waiting for a connection, stop here.
                if (connectActivity.isDestroyed()) {
                    clientNetworkService.closeClientSocket();
                    return;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Closing the Client Server Socket failed");
            }
            // start the client service again -- THIS IS INTENTIONAL
            // it will keep the service alive across different activities.
            Intent serverIntent = new Intent(connectActivity, ClientNetworkService.class);
            connectActivity.startService(serverIntent);

            // Initialize Playlist
            Intent playlistIntent = new Intent(connectActivity, PlaylistActivity.class);
            playlistIntent.putExtra(ch.ethz.inf.vs.kompose.MainActivity.KEY_NETWORKSERVICE, serverIntent);
            connectActivity.startActivity(playlistIntent);
            connectActivity.finish();
        }
    }

    /**
     * Navigation to the History Activity
     */
    public void viewHistoryFromTitle(View view) {
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }
}
