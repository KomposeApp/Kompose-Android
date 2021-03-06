package ch.ethz.inf.vs.kompose;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.joda.time.DateTime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.databinding.ActivityMainBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.client.ClientRegistrationTask;
import ch.ethz.inf.vs.kompose.service.client.NSDListenerService;
import ch.ethz.inf.vs.kompose.view.mainactivity.CustomViewPager;
import ch.ethz.inf.vs.kompose.view.mainactivity.MainActivityPagerAdapter;
import ch.ethz.inf.vs.kompose.view.viewmodel.MainViewModel;

public class MainActivity extends BaseActivity implements MainViewModel.ClickListener {

    private final String LOG_TAG = "##MainActivity";

    private final MainViewModel viewModel = new MainViewModel(this);
    private TabLayout tabLayout;

    private NSDListenerService NSDListenerService;
    private boolean nsdListenerServiceBound = false;
    private ProgressDialog connectionProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable the ViewModel inputs
        viewModel.setEnabled(true);

        // Initialize the preference utility, and sets a flag to prevent ShareActivity from killing Kompose
        StateSingleton.getInstance().setStartedFromMainActivity();
        StateSingleton.getInstance().setPreferenceUtility(this);

        // Initialize Content View
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(viewModel);

        // setup toolbar
        Toolbar mainToolbar = findViewById(R.id.kompose_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Insert default names
        viewModel.setFromPreferences(StateSingleton.getInstance().getPreferenceUtility());

        tabLayout = findViewById(R.id.tabLayout);
        final CustomViewPager viewPager = findViewById(R.id.viewPager);
        final PagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager(), viewModel);

        viewPager.initializeViewModel(viewModel);

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
        Intent cnsIntent = new Intent(this.getBaseContext(), NSDListenerService.class);
        bindService(cnsIntent, nsdListenerConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        // Reinsert default names if settings have changed
        if (StateSingleton.getInstance().getPreferenceUtility().hasChanged()) {
            viewModel.setClientName(StateSingleton.getInstance().getPreferenceUtility().getUsername());
            viewModel.setSessionName(StateSingleton.getInstance().getPreferenceUtility().getSessionName());
        }

        //rebind client network service if it's gone when returning
        if (!nsdListenerServiceBound) {
            Intent intent = new Intent(this.getBaseContext(), NSDListenerService.class);
            bindService(intent, nsdListenerConnection, BIND_AUTO_CREATE);
        }

        if (!viewModel.isEnabled()){
            enableViews();
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
        if (nsdListenerServiceBound) {
            unbindService(nsdListenerConnection);
            nsdListenerServiceBound = false;
        }
    }


    /**
     * Join tab -- join existing rooms by clicking on their fragment.
     * @param sessionModel Session to join
     */
    @Override
    public void joinSessionClicked(SessionModel sessionModel) {
        //disable inputs
        if (viewModel.isEnabled()) {
            disableViews();
        }

        String clientName = viewModel.getClientName();
        // Client's name must not be empty
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.view_error_clientname));
            enableViews();
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

        RegistrationListener listener = new RegistrationListener(this);
        Thread registrationTask;
        try {
            registrationTask = new Thread(new ClientRegistrationTask(this,clientName, listener));
        } catch (IOException e) {
            e.printStackTrace();
            showError(getString(R.string.view_error_connection_failed));
            enableViews();
            return;
        }

        connectionProgress = ProgressDialog.show(this,getString(R.string.progress_connect_title),
                getString(R.string.progress_be_patient), true, false);
        viewModel.saveToPreferences(StateSingleton.getInstance().getPreferenceUtility());
        registrationTask.start();
    }

    @Override
    public void joinManualClicked() {
        //disable inputs
        if (viewModel.isEnabled()) {
            disableViews();
        }

        String addressText = viewModel.getIpAddress();
        String portText = viewModel.getPort();

        if (!checkIPandPort(addressText, portText)){
            showError(getString(R.string.view_error_address_port));
            enableViews();
            return;
        }

        // Create a stub session (attributes will later be filled in)
        SessionModel sessionModel = new SessionModel(null, null, false);

        // Get IP / port
        try {
            InetAddress inetAddress = InetAddress.getByName(addressText);
            int port = Integer.parseInt(portText);
            ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails(
                    inetAddress, port);
            sessionModel.setConnectionDetails(serverConnectionDetails);
        } catch (UnknownHostException e) {
            showError(getString(R.string.view_error_unknown_host));
            enableViews();
            return;
        }

        joinSessionClicked(sessionModel);
    }

    @Override
    public void refreshNSDListener() {
        //Unbind the service discovery
        if (nsdListenerServiceBound) {
            unbindService(nsdListenerConnection);
            nsdListenerServiceBound = false;
        }
        viewModel.getSessionModels().clear();

        Intent intent = new Intent(this.getBaseContext(), NSDListenerService.class);
        bindService(intent, nsdListenerConnection, BIND_AUTO_CREATE);
    }


    private boolean checkIPandPort(String ipText, String portText){
        //Check if both Strings exist
        if (ipText == null || portText == null){
            return false;
        }

        if (ipText.isEmpty() || portText.isEmpty()){
            return false;
        }

        //Check size of port, and last character of ip
        int port = Integer.valueOf(portText);
        if ((port <= 0) || (port > 65535) || ipText.endsWith(".")){
            return false;
        }

        //Check length of the IP (note: must ensure only numbers and periods are permitted inputs)
        String[] addressSegments = ipText.split("\\.");
        if (addressSegments.length!=4){
            return false;
        }

        //Check the size of each segment
        for (String addressSegment : addressSegments) {
            if (Integer.valueOf(addressSegment) > 255) {
                return false;
            }
        }

        //If all checks passed, return true
        return true;
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
        if (viewModel.isEnabled()) {
            disableViews();
        }

        String clientName = viewModel.getClientName();
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.view_error_clientname));
            enableViews();
            return;
        }
        String sessionName = viewModel.getSessionName();
        if (sessionName == null || sessionName.trim().isEmpty()) {
            showError(getString(R.string.view_error_sessionname));
            enableViews();
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
        unbindService(nsdListenerConnection);
        nsdListenerServiceBound = false;

        // Start the playlist activity
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);

        viewModel.saveToPreferences(StateSingleton.getInstance().getPreferenceUtility());
    }


    private final ServiceConnection nsdListenerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "NSDListenerService bound");
            NSDListenerService.LocalBinder binder = (NSDListenerService.LocalBinder) service;
            NSDListenerService = binder.getService();
            nsdListenerServiceBound = true;
            NSDListenerService.findNetworkServices(viewModel.getSessionModels());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG_TAG, "NSDListenerService died");
            breakdown();
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "Binding with NSDListenerService died");
            breakdown();
        }

        private void breakdown() {
            showError(getString(R.string.view_error_nsd_crash));
            NSDListenerService = null;
        }
    };

    private void disableViews(){
        viewModel.setEnabled(false);
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        tabStrip.setEnabled(false);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(false);
        }
    }

    private void enableViews(){
        viewModel.setEnabled(true);
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        tabStrip.setEnabled(true);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(true);
        }
    }



    /**
     * This listener is intended to be a callback for when we successfully join a session
     * Needs to close the ServerSocket in the client in case the connection fails
     */
    private class RegistrationListener implements SimpleListener<Boolean, Integer> {

        private MainActivity mainActivity;

        private RegistrationListener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onEvent(Boolean success, Integer clientPort) {
            try {
                if (!success) {
                    Log.w(LOG_TAG, "Failed to establish connection with host");
                    mainActivity.showError(getString(R.string.view_error_connection_failed));
                    return;
                }
                //Unbind the service discovery as we don't need it anymore
                unbindService(nsdListenerConnection);
                nsdListenerServiceBound = false;

                // Initialize Playlist
                Intent playlistIntent = new Intent(mainActivity, PlaylistActivity.class);
                playlistIntent.putExtra(PlaylistActivity.KEY_PORT, clientPort);
                mainActivity.startActivity(playlistIntent);
            }finally {
                enableViews();
                connectionProgress.cancel();
            }
        }
    }
}
