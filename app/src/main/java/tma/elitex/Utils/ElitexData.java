package tma.elitex.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for holding application data in SharedPreferences.
 * It can store the data for one user and one operation.
 * <p/>
 * Created by Krum Iliev.
 */
public class ElitexData {

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

    // Operation keys
    private final String KEY_OPERATION_ID = "operation_id";
    private final String KEY_OPERATION_NAME = "operation_name";
    private final String KEY_SERIAL_NUMBER = "serial_number";
    private final String KEY_ALIGNED_TIME = "aligned_time";
    private final String KEY_ORDER_ID = "order_id";
    private final String KEY_ORDER_NAME = "order_name";
    private final String KEY_MACHINE_ID = "machine_id";
    private final String KEY_MACHINE_NAME = "machine_name";

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
    private final String KEY_WORK_ID = "work_id";
    private final String KEY_PIECES = "pieces";
    private final String KEY_START_DATE = "start_date";
    private final String KEY_TASK_RUNNING = "task_running"; // This is used if the application closes without completing the running task
    private final String KEY_WORK_TIME = "time";

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

        //Create action bar title
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = dateFormat.format(new Date());
        String title = user.mUserName + ", " + user.mDepartmentName + " | " + "Дата: " + date;
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

    /**
     * Saves operation and batch data in Elitex SharedPreferences
     *
     * @param operationAndBatch operation and batch data object
     */
    public void addOperationAndBatch(OperationAndBatch operationAndBatch) {
        SharedPreferences.Editor dataEditor = mData.edit();

        // Add operation
        dataEditor.putString(KEY_OPERATION_ID, operationAndBatch.mOperationId);
        dataEditor.putString(KEY_OPERATION_NAME, operationAndBatch.mOperationName);
        dataEditor.putInt(KEY_SERIAL_NUMBER, operationAndBatch.mSerialNumber);
        dataEditor.putFloat(KEY_ALIGNED_TIME, operationAndBatch.mAlignedTime);
        dataEditor.putInt(KEY_ORDER_ID, operationAndBatch.mOrderId);
        dataEditor.putString(KEY_ORDER_NAME, operationAndBatch.mModelName);
        dataEditor.putInt(KEY_MACHINE_ID, operationAndBatch.mMachineId);
        dataEditor.putString(KEY_MACHINE_NAME, operationAndBatch.mMachineName);

        // Add batch
        dataEditor.putInt(KEY_BATCH_ID, operationAndBatch.mBatchId);
        dataEditor.putInt(KEY_BATCH_NUMBER, operationAndBatch.mBatchNumber);
        dataEditor.putString(KEY_FEATURES, operationAndBatch.mFeatures);
        dataEditor.putString(KEY_COLOUR, operationAndBatch.mColour);
        dataEditor.putInt(KEY_BATCH_COUNT, operationAndBatch.mBatchCount);
        dataEditor.putInt(KEY_MADE, operationAndBatch.mMade);
        dataEditor.putInt(KEY_REMAINING, operationAndBatch.mRemaining);
        dataEditor.putString(KEY_SIZE, operationAndBatch.mSize);

        // Add work data
        dataEditor.putInt(KEY_WORK_ID, operationAndBatch.mWorkId);
        dataEditor.putInt(KEY_PIECES, operationAndBatch.mNeededPieces);
        dataEditor.putString(KEY_START_DATE, operationAndBatch.mStartDate);

        // Save all changes
        dataEditor.apply();
    }

    /**
     * Retrieves operation and batch data from Elitex SharedPreferences
     *
     * @return operation and batch data object
     */
    public OperationAndBatch getOperationAndBatch() {
        OperationAndBatch operationAndBatch = new OperationAndBatch(
                mData.getString(KEY_OPERATION_ID, null),
                mData.getString(KEY_OPERATION_NAME, null),
                mData.getInt(KEY_SERIAL_NUMBER, 0),
                mData.getFloat(KEY_ALIGNED_TIME, 0),
                mData.getInt(KEY_ORDER_ID, 0),
                mData.getString(KEY_ORDER_NAME, null),
                mData.getInt(KEY_MACHINE_ID, 0),
                mData.getString(KEY_MACHINE_NAME, null)
        );

        operationAndBatch.setBatch(
                mData.getInt(KEY_BATCH_ID, 0),
                mData.getInt(KEY_BATCH_NUMBER, 0),
                mData.getString(KEY_FEATURES, null),
                mData.getString(KEY_COLOUR, null),
                mData.getInt(KEY_BATCH_COUNT, 0),
                mData.getInt(KEY_MADE, 0),
                mData.getInt(KEY_REMAINING, 0),
                mData.getString(KEY_SIZE, null)
        );

        operationAndBatch.setWorkData(
                mData.getInt(KEY_WORK_ID, 0),
                mData.getInt(KEY_PIECES, 0),
                mData.getString(KEY_START_DATE, null)
        );

        return operationAndBatch;
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
    * Sets the indicator is there currently running work task
     * this is necessary if the application is terminated without completing the work task
     *
     * @param state is the task starting (true) or finishing (false)
    */
    public void setTaskRunning(boolean state) {
        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putBoolean(KEY_TASK_RUNNING, state);
        dataEditor.apply();
    }

    /**
     * @return true if the application was terminated without closing the work process
     * correctly
     */
    public boolean isTaskRunning() {
        return mData.getBoolean(KEY_TASK_RUNNING, false);
    }

    /**
     * Sets the time passed while working
     *
     * @param time the time passed in milliseconds
     */
    public void setWorkTime (long time) {
        SharedPreferences.Editor dataEditor = mData.edit();
        dataEditor.putLong(KEY_WORK_TIME, time);
        dataEditor.commit();
    }

    /**
     * @return the time spend on the before been stopped/paused in milliseconds
     */
    public long getTimePassed () {
        return mData.getLong(KEY_WORK_TIME, 0);
    }
}
