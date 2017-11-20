package ch.ethz.inf.vs.kompose;

import android.databinding.BaseObservable;

import org.junit.Test;

import ch.ethz.inf.vs.kompose.converter.ClientConverter;
import ch.ethz.inf.vs.kompose.converter.DownVoteConverter;
import ch.ethz.inf.vs.kompose.converter.IBaseConverter;
import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.json.DownVote;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.base.ReflectionUnitTest;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

import static org.junit.Assert.assertEquals;

/**
 * Created by git@famoser.ch on 18/11/2017.
 */

public class ConverterUnitTest extends ReflectionUnitTest {
    private <TModel extends BaseObservable, TEntity> void TestSingleConverter(IBaseConverter<TModel, TEntity> converter, TEntity entity) {
        fillObject(entity);

        TModel model = converter.convert(entity);
        TEntity newEntity = converter.convert(model);

        verifyObject(entity, newEntity);
    }

    @Test
    public void testConverters() throws Exception {
        SessionModel sessionModel = new SessionModel(sampleUUID, sampleUUID);
        ClientModel clientModel = new ClientModel(sampleUUID, sessionModel);
        ClientModel[] clientModels = new ClientModel[]{clientModel};
        SongModel songModel = new SongModel(sampleUUID, clientModel, sessionModel);

        TestSingleConverter(new ClientConverter(sessionModel), new Client());
        TestSingleConverter(new DownVoteConverter(clientModels, songModel), new DownVote());
        TestSingleConverter(new SongConverter(clientModels), new Song());
        TestSingleConverter(new SessionConverter(), new Session());
    }
}
