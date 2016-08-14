package tma.elitex.reference.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Krum Iliev
 */
public class ReferenceOrder {

    public String model;
    public String name;
    public int total;
    public ArrayList<ReferenceOperation> operations;

    public ReferenceOrder(String model, String name, int total, ArrayList<ReferenceOperation> operations) {
        this.model = model;
        this.name = name;
        this.total = total;
        this.operations = operations;
    }

    private static final String JSON_MODEL = "model";
    private static final String JSON_NAME = "name";
    private static final String JSON_TOTAL = "pieces";
    private static final String JSON_OPERATIONS = "processes";

    public static ReferenceOrder parse(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);

        String model = json.getString(JSON_MODEL);
        String name = json.getString(JSON_NAME);
        int total = json.getInt(JSON_TOTAL);

        JSONArray operArr = json.getJSONArray(JSON_OPERATIONS);
        ArrayList<ReferenceOperation> operations = new ArrayList<>();
        for (int i = 0; i < operArr.length(); i++) {
            operations.add(ReferenceOperation.parse(operArr.get(i).toString()));
        }

        return new ReferenceOrder(model, name, total, operations);
    }
}
