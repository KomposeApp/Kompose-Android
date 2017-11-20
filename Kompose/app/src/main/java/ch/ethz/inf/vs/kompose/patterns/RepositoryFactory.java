package ch.ethz.inf.vs.kompose.patterns;

import android.content.Context;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.repository.ClientRepository;
import ch.ethz.inf.vs.kompose.repository.SessionRepository;
import ch.ethz.inf.vs.kompose.repository.SongRepository;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;
import ch.ethz.inf.vs.kompose.service.StorageService;

public class RepositoryFactory {
    private Context context;

    public RepositoryFactory(Context context) {

    }

    private NetworkService getNetworkService() {
        return new NetworkService(getStateService());
    }

    private StateService getStateService() {
        return new StateService();
    }

    private StorageService getStorageService() {
        return new StorageService();
    }

    public ClientRepository getClientRepository() {
        return new ClientRepository();
    }

    public SessionRepository getSessionRepository() {
        return new SessionRepository(getNetworkService(), getStorageService(), getStateService());
    }

    public SongRepository getSongRepository() {
        return new SongRepository(getNetworkService(), getStateService());
    }
}
