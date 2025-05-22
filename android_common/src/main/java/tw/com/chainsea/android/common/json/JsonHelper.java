package tw.com.chainsea.android.common.json;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.log.CELog;

public class JsonHelper {
    //    private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Gson SOCKET_SPECIAL_GSON = null;
    private static JsonHelper INSTANCE;

    private static final IntTypeAdapter intTypeAdapter = new IntTypeAdapter();
    private static final StringTypeAdapter stringTypeAdapter = new StringTypeAdapter();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(int.class, intTypeAdapter)
            .registerTypeAdapter(Integer.class, intTypeAdapter)
            .registerTypeAdapter(String.class, stringTypeAdapter).create();


//    public static Gson getGson() {
//        if (gson == null) {
//            gson = new GsonBuilder()
//                    .registerTypeAdapter(int.class, intTypeAdapter)
//                    .registerTypeAdapter(Integer.class, intTypeAdapter)
//                    .registerTypeAdapter(String.class, stringTypeAdapter).create();
//        }
//        return gson;
//    }



    private static synchronized Gson getSocketSpecialInstance() {
        if (SOCKET_SPECIAL_GSON == null) {
            SOCKET_SPECIAL_GSON = new GsonBuilder()
                    .setDateFormat(DateFormat.DEFAULT, DateFormat.DEFAULT)
                    .registerTypeAdapter(JSONObject.class, (JsonDeserializer<JSONObject>) (element, type, context) -> {
                        try {
                            return new JSONObject(element.toString());
                        } catch (JSONException e) {
                            return null;
                        }
                    })
                    .registerTypeAdapter(String.class, (JsonDeserializer<String>) (element, type, context) -> {
                        try {
                            return element.toString();
                        } catch (Exception e) {
                            return "";
                        }
                    })
                    .setPrettyPrinting()
                    .create();
        }
        return SOCKET_SPECIAL_GSON;
    }

    public static synchronized JsonHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (JsonHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JsonHelper();
                }
            }
        }
        return INSTANCE;
    }

//    public static synchronized void registerTypeAdapter(Type type, Object typeAdapter) {
//        GSON = GSON.newBuilder().registerTypeAdapter(type, typeAdapter).create();
//    }

    public static <T> T socketSpecialFrom(String json, Class<T> classOfT) {
        Object object = getSocketSpecialInstance().fromJson(json, classOfT);
        if (object == null) {
            try {
                String name = classOfT.getName();
                return (T) Class.forName(name).newInstance();
            } catch (Exception e) {
                return null;
            }
        }
        return (T) object;
    }

    /**
     * Convert the object into a json string
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> String toJson(T obj) {
        if (obj instanceof String) {
            return String.valueOf(obj);
        } else if (obj instanceof JSONObject){
            return obj.toString();
        } else {
            return GSON.toJson(obj);
        }
    }

    public <T> String toJson(T obj, Type typeOfSrc) {
        return GSON.toJson(obj, typeOfSrc);
    }

    public <T> JSONObject toJsonObject(T obj) throws JSONException {
        if (obj instanceof String) {
            return new JSONObject(String.valueOf(obj));
        } else {
            String json = GSON.toJson(obj);
            return new JSONObject(json);
        }
    }

    public <T> JsonElement toJsonTree(T obj) {
        return GSON.toJsonTree(obj);
    }

    /**
     * json Convert string to object
     * @author Evan Wang
     * version 0.0.1
     */
    @SuppressWarnings("unchecked")
    public <T> T from(String json, Class<T> classOfT) {
        try {
            Object object = GSON.fromJson(json, classOfT);
            if (object == null) {
                try {
                    String name = classOfT.getName();
                    return (T) Class.forName(name).newInstance();
                } catch (Exception e) {
                    return null;
                }
            }
            return (T) object;
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T from(Object obj, Class<T> classOfT) {
        String json = toJson(obj);
        return from(json, classOfT);
    }

    /**
     * json Convert string to object
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T from(String json, Type type) {
        try {
            Object object = GSON.fromJson(json, type);
            if (object == null) {
                try {
                    return (T) type.getClass().newInstance();
                } catch (Exception e) {
                    return null;
                }
            }
            return (T) object;
        } catch (Exception e) {
            CELog.e("JsonHelper from error = ", e.getMessage() + "\n ,json=" + json);
            return null;
        }
    }

    /**
     * jsonArray Convert a string to a collection object (List)
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> List<T> fromToList(String json, Class<T[]> types) {
        if (Strings.isNullOrEmpty(json)) {
            return Lists.<T>newArrayList();
        }
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return Lists.<T>newArrayList();
        }
        final T[] jsonToObject = GSON.fromJson(json, types);
        return Lists.<T>newArrayList(jsonToObject);
    }

    public <T> T[] froms(String json, Class<T[]> types) {
        if (json == null || json.isEmpty()) {
            throw new RuntimeException();
        }
        return from(json, types);
    }

    /**
     * jsonArray Convert string to set object (Set)
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> Set<T> fromToSet(String json, Class<T[]> types) {
        if (Strings.isNullOrEmpty(json)) {
            return Sets.<T>newHashSet();
        }
        final T[] jsonToObject =  GSON.fromJson(json, types);
        return Sets.<T>newHashSet(jsonToObject);
    }

    /**
     * jsonArray The string is converted to Array(), and the empty string is automatically judged. Null must be avoided
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T[] fromToArray(String json, Class<T[]> types) {
        if (Strings.isNullOrEmpty(json)) {
            json = "[]";
        }
        return GSON.fromJson(json, types);
    }

    /**
     * json Convert string to Table object (Map)
     * @author Evan Wang
     * version 0.0.1
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> fromToMap(String json) {
        if (Strings.isNullOrEmpty(json)) {
            return Maps.<K, V>newHashMap();
        }
        return GSON.fromJson(json, HashMap.class);
    }

    /**
     * Incoming URL, reading file, and returning Json format string
     * @author Evan Wang
     * version 0.0.1
     */
    public String reader(URL url) {
        return reader(new File(url.getPath()));
    }

    /**
     * Incoming file location Reading file and returning Json format string
     * @author Evan Wang
     * version 0.0.1
     */
    public String reader(String filePath) {
        return reader(new File(filePath));
    }

    /**
     * Incoming file Reading file and returning Json format string
     * @author Evan Wang
     * version 0.0.1
     */
    public String reader(File file) throws JsonReaderException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new JsonReaderException(e);
        }
        return sb.toString();
    }

    /**
     * Incoming URL, reading the file, returning the Json format string, and converting it into an object
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T readerFrom(URL url, Class<T> classOfT) {
        String json = reader(url);
        return from(json, classOfT);
    }

    /**
     * Incoming file location Read the file and return a Json format string and convert it into an object
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T readerFrom(String filePath, Class<T> classOfT) {
        String json = reader(filePath);
        return from(json, classOfT);
    }

    /**
     * Incoming file Read the file and return the Json format string and convert it into an object
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T readerFrom(File file, Class<T> classOfT) {
        String json = reader(file);
        return from(json, classOfT);
    }

    /**
     * Incoming URL, read file, return Json format string, and convert it into array
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T[] readerFromToArray(URL url, Class<T[]> classOfT) {
        String json = reader(url);
        return fromToArray(json, classOfT);
    }

    /**
     * Incoming file location Read the file and return the Json format string and convert it into an array
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T[] readerFromToArray(String filePath, Class<T[]> classOfT) {
        String json = reader(filePath);
        return fromToArray(json, classOfT);
    }

    /**
     * Incoming file Read the file and return the Json format string and convert it into an object
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> T[] readerFromToArray(File file, Class<T[]> classOfT) {
        String json = reader(file);
        return fromToArray(json, classOfT);
    }

    /**
     * Incoming URL, reading the file, returning the Json format string, and converting it into a collection
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> List<T> readerFromToList(URL url, Class<T[]> classOfT) {
        String json = reader(url);
        return fromToList(json, classOfT);
    }

    /**
     * Incoming file location Read the file and return the Json format string and convert it into a collection
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> List<T> readerFromToList(String filePath, Class<T[]> classOfT) {
        String json = reader(filePath);
        return fromToList(json, classOfT);
    }

    /**
     * Incoming file Read the file and return the Json format string and convert it into a collection
     * @author Evan Wang
     * version 0.0.1
     */
    public <T> List<T> readerFromToList(File file, Class<T[]> classOfT) {
        String json = reader(file);
        return fromToList(json, classOfT);
    }

    /**
     * Check if there is a value
     */
    public boolean has(JSONObject jsonObject, String... keys) {
        for (String key : keys) {
            if (!jsonObject.has(key)) {
                return false;
            }
        }
        return true;
    }

    static class IntTypeAdapter extends TypeAdapter<Number> {

        @Override
        public void write(JsonWriter out, Number value)
                throws IOException {
            out.value(value);
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return -1;
            }
            try {
                String result = in.nextString();
                if ("".equals(result)) {
                    return -1;
                }
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }

    static class StringTypeAdapter extends TypeAdapter<String> {

        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return "";
            }
            try {
                String result = in.nextString();
                if ("".equals(result)) {
                    return "";
                }
                return result;
            } catch (NumberFormatException e) {
                return "";
            }
        }
    }

}
