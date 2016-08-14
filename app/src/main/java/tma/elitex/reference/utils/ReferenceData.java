package tma.elitex.reference.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Krum Iliev
 */
public class ReferenceData {

    public String date;
    public int userID;
    public int dateTotal;
    public ArrayList<ReferenceOrder> orders;

    public ReferenceData(String date, int userID, int dateTotal, ArrayList<ReferenceOrder> orders) {
        this.date = date;
        this.userID = userID;
        this.dateTotal = dateTotal;
        this.orders = orders;
    }

    private static final String JSON_DATE = "date";
    private static final String JSON_USER = "user_id";
    private static final String JSON_TOTAL = "pieces";
    private static final String JSON_ORDERS = "orders";

    public static ReferenceData parse(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);

        String date = json.getString(JSON_DATE);
        int user = json.getInt(JSON_USER);
        int total = json.getInt(JSON_TOTAL);

        JSONArray ordersArr = json.getJSONArray(JSON_ORDERS);
        ArrayList<ReferenceOrder> orders = new ArrayList<>();
        for (int i = 0; i < ordersArr.length(); i++) {
            orders.add(ReferenceOrder.parse(ordersArr.get(i).toString()));
        }

        return new ReferenceData(date, user, total, orders);
    }
}
