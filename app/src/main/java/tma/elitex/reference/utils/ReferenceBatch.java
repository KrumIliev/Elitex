package tma.elitex.reference.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Krum Iliev
 */
public class ReferenceBatch {

    public String name;
    public int count;

    public ReferenceBatch(String name, int count) {
        this.name = name;
        this.count = count;
    }

    private static final String JSON_BATCH = "batch";
    private static final String JSON_COUNT = "pieces";

    public static ReferenceBatch parse(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);

        String name = json.getString(JSON_BATCH);
        int count = json.getInt(JSON_COUNT);

        return new ReferenceBatch(name, count);
    }
}
