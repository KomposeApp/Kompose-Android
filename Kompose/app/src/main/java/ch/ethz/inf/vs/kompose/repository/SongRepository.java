package ch.ethz.inf.vs.kompose.repository;

import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;

public class SongRepository {

    private NetworkService networkService;
    private StateService stateService;

    public SongRepository(NetworkService networkService, StateService stateService) {
        this.networkService = networkService;
        this.stateService = stateService;
    }

}
