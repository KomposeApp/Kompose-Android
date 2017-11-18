package ch.ethz.inf.vs.kompose;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import ch.ethz.inf.vs.kompose.service.StorageService;

/*
 * This fails because of "permission denied" in the test environment.
 * However it seems to work on the real app.
 */

@RunWith(AndroidJUnit4.class)
public class StorageServiceTest {

    private static final String testFile =
        "Lorem ipsizzle dolor sit amizzle, fizzle daahng dawg elit.\n"
        + "Nullizzle sapizzle velizzle, aliquet shizzlin dizzle, suscipizzle quis, gravida vizzle, shit.\n"
        + "Yippiyo eget tortor.\n"
        + "Sed erizzle.\n"
        + "Shiznit izzle shizzle my nizzle crocodizzle dapibus dizzle tempizzle pimpin'.\n"
        + "Maurizzle rizzle nibh izzle turpizzle.\n"
        + "Vestibulum izzle tortor.\n";

    @Test
    public void retrieveFileTest() {
        Context context = InstrumentationRegistry.getContext();
        StorageService storageService = new StorageService(context);
        storageService.persist(null, "testfile", testFile);
        String readBack = storageService.retrieveFile(null, "testfile");
        Assert.assertEquals(testFile, readBack);
    }

    @Test
    public void retrieveAllFileTest() {
        // TODO
        Assert.assertTrue(false);
    }
}
