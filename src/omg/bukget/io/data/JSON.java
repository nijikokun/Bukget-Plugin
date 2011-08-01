package omg.bukget.io.data;

import java.util.ArrayList;
import java.util.Arrays;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSON {
    private String source;
    private JSONArray data;
    private JSONParser parser;

    public JSON() {
        this.parser = new JSONParser();
    }
    
    public JSON setSource(String source) {
        this.source = source;
        return this;
    }
    
    public Object parse() throws ParseException {
        return parse(this.source);
    }

    final public Object parse(String source) throws ParseException {
        return this.parser.parse(source);
    }

    final public JSONArray getArray(Object value) {
        return (JSONArray)value;
    }

    final public JSONArray getArray(JSONArray array, int value) {
        return getArray(array.get(value));
    }

    final public String[] getStringArray(JSONArray obj, int value) {
        ArrayList<Object> fin = new ArrayList<Object>();
        Object[] array = (getArray(obj.get(value))).toArray();

        for(Object a: array)
            if(a.getClass().isArray()) {
                fin.addAll(Arrays.asList(parseArray((Object[])a)));
            } else {
                JSONArray b = getArray(JSONValue.parse(a.toString()));

                if(b != null)
                    fin.addAll(Arrays.asList(parseArray(b.toArray())));
                else
                    fin.add(a.toString());
            }

        array = fin.toArray();
        return Arrays.copyOf(array, array.length, String[].class);
    }

    private Object[] parseArray(Object[] array) {
        ArrayList<Object> fin = new ArrayList<Object>();

        for(Object a: array)
            if(a.getClass().isArray())
                fin.addAll(Arrays.asList(parseArray((Object[])a)));
            else {
                JSONArray b = getArray(JSONValue.parse(a.toString()));

                if(b != null)
                    fin.addAll(Arrays.asList(parseArray(b.toArray())));
                else
                    fin.add(a.toString());
            }

        return fin.toArray();
    }

    final public JSONArray getArray(JSONObject array, String value) {
        return getArray(array.get(value));
    }

    final public int getInteger(JSONObject obj, String value) {
        return Integer.valueOf(String.valueOf(obj.get(value)));
    }

    final public String getString(JSONObject obj, String value) {
        return String.valueOf(obj.get(value));
    }

    final public String[] getStringArray(JSONObject obj, String value) {
        ArrayList<Object> fin = new ArrayList<Object>();
        Object[] array = (getArray(obj.get(value))).toArray();

        for(Object a: array)
            if(a.getClass().isArray()) {
                fin.addAll(Arrays.asList(parseArray((Object[])a)));
            } else {
                JSONArray b = getArray(JSONValue.parse(a.toString()));

                if(b != null)
                    fin.addAll(Arrays.asList(parseArray(b.toArray())));
                else
                    fin.add(a.toString());
            }

        array = fin.toArray();
        return Arrays.copyOf(array, array.length, String[].class);
    }

    final public JSONObject getObject(Object value) {
        return (JSONObject)value;
    }

    final public JSONObject getObject(JSONArray array, int value) {
        return getObject(array.get(value));
    }

    final public JSONObject getObject(JSONObject array, String value) {
        return getObject(array.get(value));
    }
}
