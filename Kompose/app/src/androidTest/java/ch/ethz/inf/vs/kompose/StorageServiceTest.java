package ch.ethz.inf.vs.kompose;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * This fails because of "permission denied" in the test environment.
 *
 * Setting permissions for tests does not work like described on the web or
 * in the documentation, at this point I assume the whole androidTest permission
 * is completley fucked. No matter where or how you add the permissions, they don't get
 * merged in the final Manifest file.
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
//        Context context = InstrumentationRegistry.getContext();
//        StorageHandler storageService = new StorageHandler(context);
//        storageService.persist(null, "testfile", testFile);
//        String readBack = storageService.retrieveFile(null, "testfile");
        //Assert.assertEquals(testFile, readBack);
        Assert.assertTrue(false);
    }

    @Test
    public void retrieveAllFileTest() {
        // TODO
        Assert.assertTrue(false);
    }
}
