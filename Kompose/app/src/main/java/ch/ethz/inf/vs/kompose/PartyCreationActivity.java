package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityPartyCreationBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.view.viewmodel.PartyCreationViewModel;

public class PartyCreationActivity extends BaseActivity {

    private static final String LOG_TAG = "## Party Activity";
    private final PartyCreationViewModel viewModel = new PartyCreationViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creation);

        //get binding & bind viewmodel to view
        ActivityPartyCreationBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_party_creation);
        binding.setViewModel(viewModel);
    }

    public void confirmParty(View v) {

        Log.d(LOG_TAG, "Confirmation button pressed");

        String clientName = viewModel.getClientName();
        //Check whether the client's name is empty or null
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.choose_client_name));
            return;
        }

        String sessionName = viewModel.getSessionName();
        if (sessionName == null || sessionName.trim().isEmpty()) {
            showError(getString(R.string.choose_session_name));
            return;
        }

        // Remove trailing whitespace from username and set it in the singleton
        StateSingleton.getInstance().username = clientName.trim();
        UUID deviceUUID = StateSingleton.getInstance().deviceUUID;

        // create a new session
        SessionModel newSession = new SessionModel(UUID.randomUUID(), deviceUUID);
        newSession.setName(sessionName.trim());

        // Add host as client to the session
        ClientModel clientModel = new ClientModel(deviceUUID, newSession);
        clientModel.setName(StateSingleton.getInstance().username);
        clientModel.setIsActive(true);
        newSession.getClients().add(clientModel);

        StateSingleton.getInstance().activeSession = newSession;
        StateSingleton.getInstance().deviceIsHost = true;

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
