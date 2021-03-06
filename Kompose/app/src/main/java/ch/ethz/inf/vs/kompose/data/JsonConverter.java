package ch.ethz.inf.vs.kompose.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;

public class JsonConverter {

    /** JSON --> Message **/
    public static Message fromMessageJsonString(String json) throws IOException {
        return getObjectReader(Message.class).readValue(json);
    }

    /** JSON --> Session **/
    public static Session fromSessionJsonString(String json) throws IOException {
        return getObjectReader(Session.class).readValue(json);
    }

    /** Message --> JSON String **/
    public static String toJsonString(Message obj) throws JsonProcessingException {
        return getObjectWriter(Message.class).writeValueAsString(obj);
    }

    /** Session --> JSON String **/
    public static String toJsonString(Session obj) throws JsonProcessingException {
        return getObjectWriter(Session.class).writeValueAsString(obj);
    }

    private static Map<Type, ObjectReader> _readers = new HashMap<>();
    private static Map<Type, ObjectWriter> _writers = new HashMap<>();

    private static void instantiateMapper(Class type) {
        if (!_readers.containsKey(type)) {
            ObjectMapper mapper = new ObjectMapper();
            _readers.put(type, mapper.readerFor(type));
            _writers.put(type, mapper.writerFor(type));
        }
    }

    private static ObjectReader getObjectReader(Class type) {
        instantiateMapper(type);
        return _readers.get(type);
    }

    private static ObjectWriter getObjectWriter(Class type) {
        instantiateMapper(type);
        return _writers.get(type);
    }
}
