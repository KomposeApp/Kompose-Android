package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.joda.time.DateTime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.databinding.ActivityMainBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.client.ClientNetworkService;
import ch.ethz.inf.vs.kompose.view.mainactivity.MainActivityPagerAdapter;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

public class MainActivity extends BaseActivity implements MainViewModel.ClickListener {

    private final String LOG_TAG = "## Main Activity";

    public static final String KEY_NETWORKSERVICE = "ClientNetworkService";
    public static final String KEY_SERVERSERVICE = "HostServerService";
    public static final String SERVICE_NAME = "Kompose";
    public static final String SERVICE_TYPE = "_kompose._tcp";
    public static final String SERVICE_TYPE_NSD = "_kompose._tcp.";

    private final MainViewModel viewModel = new MainViewModel(this);

    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the preference utility, and sets a flag to prevent ShareActivity from killing Kompose
        StateSingleton.getInstance().setStartedFromMainActivity();
        StateSingleton.getInstance().setPreferenceUtility(this);

        // Initialize the song cache
        int currentPreload = StateSingleton.getInstance().getPreferenceUtility().getPreload();
        int currentCacheSize = StateSingleton.getInstance().getPreferenceUtility().getCurrentCacheSize();
        StateSingleton.getInstance().initializeSongCache(currentPreload, currentCacheSize);

        // Initialize Content View
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(viewModel);

        // setup toolbar
        Toolbar mainToolbar = findViewById(R.id.kompose_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Insert default names
        viewModel.setFromPreferences(StateSingleton.getInstance().getPreferenceUtility());

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewPager viewPager = findViewById(R.id.viewPager);
        final PagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager(), viewModel);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                viewModel.getSessionModels().clear();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //bind client network service
        Intent cnsIntent = new Intent(this.getBaseContext(), ClientNetworkService.class);
        bindService(cnsIntent, cNetServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        // Reinsert default names if settings have changed
        if (StateSingleton.getInstance().getPreferenceUtility().hasChanged()) {
            viewModel.setClientName(StateSingleton.getInstance().getPreferenceUtility().getUsername());
            viewModel.setSessionName(StateSingleton.getInstance().getPreferenceUtility().getSessionName());
        }

        //rebind client network service if it's gone when returning
        if (!clientNetworkServiceBound) {
            Intent intent = new Intent(this.getBaseContext(), ClientNetworkService.class);
            bindService(intent, cNetServiceConnection, BIND_AUTO_CREATE);
        }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_toolbar_settings:
                viewModel.openSettingsClicked(null);
                return true;

            case R.id.main_toolbar_history:
                viewModel.openHistoryClicked(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientNetworkServiceBound) {
            unbindService(cNetServiceConnection);
            clientNetworkServiceBound = false;
        }
        //Clear song cache
        StateSingleton.getInstance().clearCache();
    }

    /**
     * Join tab -- join existing rooms by clicking on their fragment.
     * @param sessionModel Session to join
     */
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

        //Set active session and our own active client
        StateSingleton.getInstance().setActiveSession(sessionModel);
        StateSingleton.getInstance().setActiveClient(clientModel);

        try {
            if (!clientNetworkServiceBound || clientNetworkService == null)
                throw new IllegalStateException("Failed to properly set up Client Network Service");

            RegistrationListener listener = new RegistrationListener(this);
            clientNetworkService.initSocketListener();
            clientNetworkService.registerClientOnHost(listener, clientName);
        } catch (SocketException s) {
            s.printStackTrace();
            try {
                clientNetworkService.closeClientSocket();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Cleaning up the Client Socket failed.");
                e.printStackTrace();
            }
            showError("Failed to set up connection.");
        } catch (IllegalStateException | IOException io) {
            io.printStackTrace();
            showError("Failed to set up connection.");
        }
        viewModel.saveToPreferences(StateSingleton.getInstance().getPreferenceUtility());
    }

    @Override
    public void joinManualClicked() {

        UUID deviceUUID = StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID();

        // Create a stub session
        // NOTE: Don't care for now what we put as Session or Host UUID.
        SessionModel sessionModel = new SessionModel(deviceUUID, null, false);

        // Get IP / port
        InetAddress inetAddress = getInetAddressByNameAsync(viewModel.getIpAddress());
        if (inetAddress == null) {
            showError("Unknown host");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(viewModel.getPort());
        } catch(NumberFormatException e) {
            showError("Invalid port");
            return;
        }
        ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails(
                inetAddress, port);
        sessionModel.setConnectionDetails(serverConnectionDetails);

        joinSessionClicked(sessionModel);
    }

    @Override
    public void openSettingsClicked() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public void openHistoryClicked() {
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
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

        //Unbind the service discovery as we don't need it anymore
        unbindService(cNetServiceConnection);
        clientNetworkServiceBound = false;

        // Start the playlist activity
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);

        viewModel.saveToPreferences(StateSingleton.getInstance().getPreferenceUtility());
    }


    private final ServiceConnection cNetServiceConnection = new ServiceConnection() {
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
            breakdown();
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "Binding with ClientNetworkService died");
            breakdown();
        }

        private void breakdown() {
            showError("Discovery of Kompose Sessions has stopped unexpectedly.");
            clientNetworkService = null;
        }
    };

    /**
     * This listener is intended to be a callback for when we successfully join a session
     * Needs to close the ServerSocket in the client in case the connection fails
     */
    private class RegistrationListener implements SimpleListener<Boolean, Void> {

        private MainActivity mainActivity;

        private RegistrationListener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onEvent(Boolean success, Void v) {
            try {
                if (!success) {
                    Log.e(LOG_TAG, "Failed to establish connection with host");
                    mainActivity.showError(getString(R.string.view_error_connection_failed));
                    clientNetworkService.closeClientSocket();
                    return;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Closing the Client Server Socket failed");
            }

            // start the client service through startService -- THIS IS INTENTIONAL
            // it will keep the service alive across different activities.
            Intent serverIntent = new Intent(mainActivity, ClientNetworkService.class);
            mainActivity.startService(serverIntent);

            //Unbind the service discovery as we don't need it anymore
            unbindService(cNetServiceConnection);
            clientNetworkServiceBound = false;

            // Initialize Playlist
            Intent playlistIntent = new Intent(mainActivity, PlaylistActivity.class);
            playlistIntent.putExtra(ch.ethz.inf.vs.kompose.MainActivity.KEY_NETWORKSERVICE, serverIntent);
            mainActivity.startActivity(playlistIntent);
        }
    }

    // getByName can cause NetworkOnMainThreadException, so we have to wrap it in an AsyncTask
    private InetAddress getInetAddressByNameAsync(String ip) {
        try {
            GetByNameTask getByNameTask = new GetByNameTask();
            return getByNameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip).get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    private static class GetByNameTask extends AsyncTask<String, Void, InetAddress> {

        @Override
        protected InetAddress doInBackground(String... strings) {
            try {
                return InetAddress.getByName(strings[0]);
            } catch (UnknownHostException e) {
                return null;
            }
        }
    }
}
