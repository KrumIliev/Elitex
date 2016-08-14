package tma.elitex.reference.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Krum Iliev
 */
public class ReferenceOperation {

    public String name;
    public int total;
    public ArrayList<ReferenceBatch> batches;

    public ReferenceOperation(String name, int total, ArrayList<ReferenceBatch> batches) {
        this.name = name;
        this.total = total;
        this.batches = batches;
    }

    private static final String JSON_NAME = "name";
    private static final String JSON_TOTAL = "pieces";
    private static final String JSON_BATCHES = "earnings";

    public static ReferenceOperation parse(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);

        String name = json.getString(JSON_NAME);
        int total = json.getInt(JSON_TOTAL);

        JSONArray batchesArr = json.getJSONArray(JSON_BATCHES);
        ArrayList<ReferenceBatch> batches = new ArrayList<>();
        for (int i = 0; i < batchesArr.length(); i++) {
            batches.add(ReferenceBatch.parse(batchesArr.get(i).toString()));
        }

        return new ReferenceOperation(name, total, batches);
    }
}
