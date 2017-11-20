package ch.ethz.inf.vs.kompose;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.ReflectionUnitTest;
import ch.ethz.inf.vs.kompose.converter.ClientConverter;
import ch.ethz.inf.vs.kompose.data.Client;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.data.Session;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.ClientModel;

/**
 * Created by git@famoser.ch on 19/11/2017.
 */

public class JsonUnitTest extends ReflectionUnitTest {

    @Test
    public void messageTest() throws Exception {
        Message message = new Message();
        message.setType(MessageType.REGISTER_CLIENT.toString());
        message.setSenderUuid(UUID.randomUUID().toString());
        message.setSenderUsername("Mom");

        String json = JsonConverter.toJsonString(message);

        Message newMessage = JsonConverter.fromMessageJsonString(json);

        verifyObject(message, newMessage);
    }

    @Test
    public void sessionTest() throws Exception {
        Session session = new Session();
        session.setHostUuid(UUID.randomUUID().toString());
        session.setSessionName("session name");
        session.setUuid(UUID.randomUUID().toString());

        String json = JsonConverter.toJsonString(session);

        Session newSession = JsonConverter.fromSessionJsonString(json);

        verifyObject(session, newSession);
    }

    @Test
    public void directJsonTest() throws Exception {
        String challenge = "{\n" +
                "    \"type\": \"REGISTER_CLIENT\",\n" +
                "    \"sender_username\": \"Mario Huana\",\n" +
                "    \"sender_uuid\": \"c4d435c6-c92b-11e7-9e80-d1034c1b7b33\"\n" +
                "}";
        Message expected = new Message();
        expected.setSenderUsername("Mario Huana");
        expected.setSenderUuid("c4d435c6-c92b-11e7-9e80-d1034c1b7b33");
        expected.setType("REGISTER_CLIENT");

        Message actual = JsonConverter.fromMessageJsonString(challenge);

        verifyObject(expected, actual);
    }
}
