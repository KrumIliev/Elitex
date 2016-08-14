package tma.elitex.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for holding application data in SharedPreferences.
 * It can store the data for one user and one operation.
 * <p/>
 * Created by Krum Iliev.
 */
public class ElitexData {

    private final String KEY_OPERATIONS = "operations";

    // Operation keys


    // User keys
    private final String KEY_USER_ID = "user_id";
    private final String KEY_USER_NAME = "user_name";
    private final String KEY_DEPARTMENT_ID = "department_id";
    private final String KEY_DEPARTMENT_NAME = "department_name";
    private final String KEY_DEPARTMENT_KIND = "department_kind";
    private final String KEY_ROLES = "user_roles";
    private final String KEY_ACTIONBAR_TITLE = "title";
    private final String KEY_TOKEN = "token";
    private final String KEY_KEEP_LOGGED = "keep_logged";

    // Batch keys
    private final String KEY_BATCH_ID = "batch_id";
    private final String KEY_BATCH_NUMBER = "batch_number";
    private final String KEY_FEATURES = "features";
    private final String KEY_SIZE = "size";
    private final String KEY_COLOUR = "colour";
    private final String KEY_BATCH_COUNT = "batch_count";
    private final String KEY_MADE = "made";
    private final String KEY_REMAINING = "remaining";

    // Work data keys
    private final String KEY_WORK_IDS_ARRAY = "work_ids_array";
    private final String KEY_WORK_TITLE = "work_title";
    private final String KEY_OPERATION_IDS_ARRAY = "operation_ids_array";
    private final String KEY_START_DATE = "start_date";
    private final String KEY_WORK_TIME = "time";
    private final String KEY_IS_SEPARATE = "separate";

    private final String PREFS_NAME = "elitexPrefsFile"; // Preference file name
    private SharedPreferences mData;

    public ElitexData(Context context) {
        mData = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves user data in Elitex SharedPreferences and creates the action bar title
     *
     * @param user user data object
     */
    public void addUserData(User user) {
        //Save user data
        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putInt(KEY_USER_ID, user.mUserId);
        dataEditor.putString(KEY_USER_NAME, user.mUserName);
        dataEditor.putInt(KEY_DEPARTMENT_ID, user.mDepartmentId);
        dataEditor.putString(KEY_DEPARTMENT_NAME, user.mDepartmentName);
        dataEditor.putString(KEY_DEPARTMENT_KIND, user.mDepartmentKind);
        dataEditor.putStringSet(KEY_ROLES, user.mRoles);
        dataEditor.putBoolean(KEY_KEEP_LOGGED, user.mKeepLogged);

        // Save all changes
        dataEditor.apply();
    }

    public void setActionBarTitle(User user, String serverTime) {
        SharedPreferences.Editor dataEditor = mData.edit();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        Date date = null;
        try {
            date = dateFormat.parse(serverTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String dateStr = dateFormat.format(date);
        String title = user.mUserName + ", " + user.mDepartmentName + " | " + "Дата: " + dateStr;
        dataEditor.putString(KEY_ACTIONBAR_TITLE, title);

        // Save all changes
        dataEditor.apply();
    }

    /**
     * Retrieves user data from Elitex SharedPreferences
     *
     * @return user data object
     */
    public User getUserData() {
        return new User(
                mData.getInt(KEY_USER_ID, 0),
                mData.getString(KEY_USER_NAME, null),
                mData.getInt(KEY_DEPARTMENT_ID, 0),
                mData.getString(KEY_DEPARTMENT_NAME, null),
                mData.getString(KEY_DEPARTMENT_KIND, null),
                mData.getStringSet(KEY_ROLES, null),
                mData.getBoolean(KEY_KEEP_LOGGED, false)
        );
    }

    /**
     * Retrieves custom user action bar title
     *
     * @return action bar title string
     */
    public String getActionBarTitle() {
        return mData.getString(KEY_ACTIONBAR_TITLE, "");
    }

    public void setWorkData(WorkData data) {
        SharedPreferences.Editor dataEditor = mData.edit();

        if (data.batch != null) {
            // Add batch
            dataEditor.putInt(KEY_BATCH_ID, data.batch.mBatchId);
            dataEditor.putInt(KEY_BATCH_NUMBER, data.batch.mBatchNumber);
            dataEditor.putString(KEY_FEATURES, data.batch.mFeatures);
            dataEditor.putString(KEY_COLOUR, data.batch.mColour);
            dataEditor.putInt(KEY_BATCH_COUNT, data.batch.mTotalPieces);
            dataEditor.putInt(KEY_MADE, data.batch.mMade);
            dataEditor.putInt(KEY_REMAINING, data.batch.mRemaining);
            dataEditor.putString(KEY_SIZE, data.batch.mSize);
        } else {
            dataEditor.putInt(KEY_BATCH_ID, -1);
            dataEditor.putInt(KEY_BATCH_NUMBER, 0);
            dataEditor.putString(KEY_FEATURES, "");
            dataEditor.putString(KEY_COLOUR, "");
            dataEditor.putInt(KEY_BATCH_COUNT, 0);
            dataEditor.putInt(KEY_MADE, 0);
            dataEditor.putInt(KEY_REMAINING, 0);
            dataEditor.putString(KEY_SIZE, "");
        }

        // Add operation ids
        dataEditor.putString(KEY_OPERATION_IDS_ARRAY, data.operationIDs.toString());

        // Add work ids
        dataEditor.putString(KEY_WORK_IDS_ARRAY, data.workIDs.toString());

        dataEditor.putString(KEY_WORK_TITLE, data.workTitle);

        dataEditor.putString(KEY_START_DATE, data.startDate);

        dataEditor.putBoolean(KEY_IS_SEPARATE, data.isSeparate);

        // Save all changes
        dataEditor.apply();
    }

    public WorkData getWorkData() {
        Batch batch = new Batch(
                mData.getInt(KEY_BATCH_ID, 0),
                mData.getInt(KEY_BATCH_NUMBER, 0),
                mData.getString(KEY_FEATURES, null),
                mData.getString(KEY_COLOUR, null),
                mData.getInt(KEY_BATCH_COUNT, 0),
                mData.getInt(KEY_MADE, 0),
                mData.getInt(KEY_REMAINING, 0),
                mData.getString(KEY_SIZE, null)
        );

        JSONArray operationIDs = null;
        JSONArray workIDs = null;
        try {
            operationIDs = new JSONArray(mData.getString(KEY_OPERATION_IDS_ARRAY, ""));
            workIDs = new JSONArray(mData.getString(KEY_WORK_IDS_ARRAY, ""));
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }

        String workTitle = mData.getString(KEY_WORK_TITLE, "");
        String date = mData.getString(KEY_START_DATE, Utils.getCurrentDate());
        boolean isSeprate = mData.getBoolean(KEY_IS_SEPARATE, false);

        return new WorkData(batch, operationIDs, workIDs, workTitle, date, isSeprate);
    }

    /**
     * Saves the access token returned from the server
     *
     * @param accessToken server access token
     */
    public void setAccessToken(String accessToken) {
        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putString(KEY_TOKEN, accessToken);
        dataEditor.apply();
    }

    /**
     * @return server access token
     */
    public String getAccessToken() {
        return mData.getString(KEY_TOKEN, null);
    }

    /**
     * Sets the time passed while working
     *
     * @param time the time passed in milliseconds
     */
    public void saveWorkTime(long time) {
        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putLong(KEY_WORK_TIME, time);
        dataEditor.apply();
        Log.d("Data", "Work time saved: " + time);
    }

    /**
     * @return the time spend on the before been stopped/paused in milliseconds
     */
    public long getTimePassed() {
        return mData.getLong(KEY_WORK_TIME, 0);
    }

    public void addOperation(String operation) {
        Set<String> operations = getOperations();
        operations.add(operation);

        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putStringSet(KEY_OPERATIONS, operations);
        dataEditor.apply();
    }

    public Set<String> getOperations() {
        return mData.getStringSet(KEY_OPERATIONS, new HashSet<String>());
    }

    public void resetOperations() {
        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putStringSet(KEY_OPERATIONS, new HashSet<String>());
        dataEditor.apply();
    }
}
