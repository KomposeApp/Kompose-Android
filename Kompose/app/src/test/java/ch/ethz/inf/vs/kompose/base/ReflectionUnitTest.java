package ch.ethz.inf.vs.kompose.base;

import org.joda.time.DateTime;
import org.junit.Assert;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.enums.SongStatus;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public class ReflectionUnitTest {

    private Map<Type, Object> typeToValueDictionary = new HashMap<>();

    protected final static UUID sampleUUID = UUID.fromString("fe567f0c-7f27-4b36-965d-a24071fd346e");

    public ReflectionUnitTest() {
        typeToValueDictionary.put(String.class, "hi mom");
        typeToValueDictionary.put(Integer.class, 1);
        typeToValueDictionary.put(int.class, 1);
        typeToValueDictionary.put(Boolean.class, true);
        typeToValueDictionary.put(boolean.class, true);
        typeToValueDictionary.put(DateTime.class, "2004-02-12T16:19:21.000+01:00");
        typeToValueDictionary.put(UUID.class, sampleUUID.toString());
        typeToValueDictionary.put(URI.class, "http://youtube.com");
        typeToValueDictionary.put(SongStatus.class, SongStatus.EXCLUDED_BY_POPULAR_VOTE.toString());
    }

    protected void fillObject(Object obj) {
        Class cls = obj.getClass();
        Method[] methods = cls.getDeclaredMethods();

        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                if (method.getParameterTypes().length != 1) {
                    continue;
                }

                Type parameterType = method.getParameterTypes()[0];
                try {
                    if (!typeToValueDictionary.containsKey(parameterType)) {
                        continue;
                    }

                    if (parameterType.equals(String.class)) {
                        if (methodName.endsWith("DateTime")) {
                            method.invoke(obj, typeToValueDictionary.get(DateTime.class));
                        } else if (methodName.endsWith("Uuid")) {
                            method.invoke(obj, typeToValueDictionary.get(UUID.class));
                        } else if (methodName.endsWith("Url")) {
                            method.invoke(obj, typeToValueDictionary.get(URI.class));
                        } else if (methodName.endsWith("Status")) {
                            method.invoke(obj, typeToValueDictionary.get(SongStatus.class));
                        } else {
                            method.invoke(obj, typeToValueDictionary.get(parameterType));
                        }
                    } else {
                        method.invoke(obj, typeToValueDictionary.get(parameterType));
                    }

                } catch (Exception ex) {
                    Assert.fail(ex.toString());
                }
            }
        }
    }

    protected <T> void verifyObject(T oldInstance, T newInstance) {
        Class cls = oldInstance.getClass();
        Method[] methods = cls.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                try {
                    if (method.getReturnType().isArray()) {
                        //discard arrays for now
                    } else {
                        Assert.assertEquals("testing " + method.getName() + " in " + cls.getName(), method.invoke(oldInstance), method.invoke(newInstance));
                    }
                } catch (Exception ex) {
                    Assert.fail(ex.toString());
                }
            }
        }
    }

}