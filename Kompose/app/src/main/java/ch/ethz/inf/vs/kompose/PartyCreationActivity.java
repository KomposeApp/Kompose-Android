package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import org.joda.time.DateTime;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityPartyCreationBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.host.HostServerService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.view.viewmodel.PartyCreationViewModel;

public class PartyCreationActivity extends BaseActivity {

    private final String LOG_TAG = "## Party Activity";
    private final PartyCreationViewModel viewModel = new PartyCreationViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_party_creation);

        viewModel.setClientName(StateSingleton.getInstance().getPreferenceUtility().getCurrentUsername());
        viewModel.setSessionName(StateSingleton.getInstance().getPreferenceUtility().getCurrentSessionName());

        //get binding & bind viewmodel to view
        ActivityPartyCreationBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_party_creation);
        binding.setViewModel(viewModel);
    }

    public void confirmParty(View v) {
        Log.d(LOG_TAG, "Confirmation button pressed");

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
        this.finish();
    }

}
