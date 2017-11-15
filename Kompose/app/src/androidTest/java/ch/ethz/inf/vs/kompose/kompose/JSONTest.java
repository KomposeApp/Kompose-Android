package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class JSONTest {

    private static final String LOG_TAG = "### JSONTest";

    @Test
    public void jsonTest() throws Exception {
        Context instrumentationCtx = InstrumentationRegistry.getContext();

        Message msg1 = new Message(Message.MessageType.REGISTER_CLIENT,
                "Big Shaq",
                UUID.randomUUID(),
                "",
                null,
                null
        );

        Session session2 = new Session(instrumentationCtx,
                "Big Shaqs CONSPICC party",
                "Big Shaq, the host",
                UUID.randomUUID());

        session2.addItem(new PlaylistItem(instrumentationCtx,
                0,
                0,
                "Shooting Stars",
                "UNKNOWN",
                "https://www.youtube.com/watch?v=feA64wXhbjo"
                ));

        session2.addItem(new PlaylistItem(instrumentationCtx,
                1,
                7,
                "Ghostbusters",
                "UNKNOWN",
                "https://www.youtube.com/watch?v=m9We2XsVZfc"));

        Message msg2 = new Message(Message.MessageType.SESSION_UPDATE,
                "Big Shaq",
                UUID.randomUUID(),
                "",
                session2,
                null);

        String msg1Str = msg1.toJSON().toString();
        String msg2Str = msg2.toJSON().toString();

        Message msg3 = new Message(instrumentationCtx, msg1.toJSON());
        Message msg4 = new Message(instrumentationCtx, msg2.toJSON());
        String msg3Str = msg3.toJSON().toString();
        String msg4Str = msg4.toJSON().toString();

        Log.d(LOG_TAG, "msg1 = " + msg1Str);
        Log.d(LOG_TAG, "msg3 = " + msg3Str);
        Log.d(LOG_TAG, "msg2 = " + msg2Str);
        Log.d(LOG_TAG, "msg4 = " + msg4Str);

        Assert.assertEquals(msg1Str, msg3Str);
        Assert.assertEquals(msg2Str, msg4Str);
    }
}
