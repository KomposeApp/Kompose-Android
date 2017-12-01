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
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
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
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_party_creation);


        //get binding & bind viewmodel to view
        ActivityPartyCreationBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_party_creation);
        binding.setViewModel(viewModel);

    }

    public void confirmParty(View v) {
        Log.d(LOG_TAG, "Confirmation button pressed");

        String clientName = viewModel.getClientName();
        if (clientName == null) {
            showError(getString(R.string.view_error_clientname));
            return;
        }
        String sessionName = viewModel.getSessionName();
        if (sessionName == null) {
            showError(getString(R.string.view_error_sessionname));
            return;
        }

        UUID deviceUUID = StateSingleton.getInstance().deviceUUID;


        // create a new session
        SessionModel newSession = new SessionModel(UUID.randomUUID(), deviceUUID);
        newSession.setName(sessionName);

        // initialize session as paused
        newSession.setSessionStatus(SessionStatus.WAITING);

        // creation timestamp
        newSession.setCreationDateTime(DateTime.now());

        // make this device the host
        StateSingleton.getInstance().deviceIsHost = true;

        //todo technical: am I doing this right?
        ClientModel clientModel = new ClientModel(deviceUUID, newSession);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);
        newSession.getClients().add(clientModel);

        StateSingleton.getInstance().activeSession = newSession;

        // start the server service
        Intent serverIntent = new Intent(this, AndroidServerService.class);
        startService(serverIntent);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
