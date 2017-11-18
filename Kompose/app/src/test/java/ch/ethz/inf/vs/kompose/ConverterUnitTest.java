package ch.ethz.inf.vs.kompose;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.ClientConverter;
import ch.ethz.inf.vs.kompose.converter.DownVoteConverter;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.Client;
import ch.ethz.inf.vs.kompose.data.DownVote;
import ch.ethz.inf.vs.kompose.data.Song;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

import static org.junit.Assert.assertEquals;

/**
 * Created by git@famoser.ch on 18/11/2017.
 */

public class ConverterUnitTest {
    private Map<Type, Object> typeToValueDictionary = new HashMap<>();

    public ConverterUnitTest() {
        typeToValueDictionary.put(String.class, "hi mom");
        typeToValueDictionary.put(Integer.class, 1);
        typeToValueDictionary.put(int.class, 1);
        typeToValueDictionary.put(Boolean.class, true);
        typeToValueDictionary.put(boolean.class, true);
        typeToValueDictionary.put(DateTime.class, "2004-02-12T15:19:21+00:00");
        typeToValueDictionary.put(UUID.class, "fe567f0c-7f27-4b36-965d-a24071fd346e");
    }

    private void fillObject(Object obj) {
        try {
            Class cls = obj.getClass();
            Method[] methods = cls.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    Type parameterType = method.getParameterTypes()[0];
                    if (parameterType.equals(String.class)) {
                        if (method.getName().endsWith("DateTime")) {
                            method.invoke(obj, typeToValueDictionary.get(DateTime.class));
                        } else if (method.getName().endsWith("Uuid")) {
                            method.invoke(obj, typeToValueDictionary.get(UUID.class));
                        } else {
                            method.invoke(obj, typeToValueDictionary.get(parameterType));
                        }
                    } else {

                        method.invoke(obj, typeToValueDictionary.get(parameterType));
                    }

                }
            }
        } catch (Exception ex) {
            Assert.fail(ex.toString());
        }
    }

    private <T> void verifyObject(T oldInstance, T newInstance) {
        try {
            Class cls = oldInstance.getClass();
            Method[] methods = cls.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().startsWith("get")) {
                    Assert.assertEquals(method.invoke(oldInstance), method.invoke(newInstance));
                }
            }
        } catch (Exception ex) {
            Assert.fail(ex.toString());
        }
    }

    @Test
    public void clientConverterTest() throws Exception {
        Client client = new Client();

        fillObject(client);

        ClientModel model = ClientConverter.convert(client);
        Client newClient = ClientConverter.convert(model);

        verifyObject(client, newClient);
    }

    @Test
    public void downVoteConverterTest() throws Exception {
        DownVote downVote = new DownVote();

        fillObject(downVote);

        DownVoteModel model = DownVoteConverter.convert(downVote, sampleClients());
        DownVote newDownVote = DownVoteConverter.convert(model);

        verifyObject(downVote, newDownVote);
    }

    @Test
    public void songConverterTest() throws Exception {
        Song song = new Song();

        fillObject(song);

        SongModel model = SongConverter.convert(song, sampleClients());
        Song newSong = SongConverter.convert(model);

        verifyObject(song, newSong);
    }

    private ClientModel[] sampleClients() {
        return new ClientModel[]{new ClientModel(UUID.fromString((String) typeToValueDictionary.get(UUID.class)))};
    }
}
