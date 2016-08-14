package tma.elitex.load;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import tma.elitex.R;
import tma.elitex.SignInActivity;
import tma.elitex.WorkActivity;
import tma.elitex.reference.ReferenceActivity;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.Batch;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.ExitDialog;
import tma.elitex.utils.ExitListener;
import tma.elitex.utils.FeaturesDialog;
import tma.elitex.utils.LoadingDialog;
import tma.elitex.utils.MassageDialog;
import tma.elitex.utils.Operation;
import tma.elitex.utils.User;
import tma.elitex.utils.Utils;
import tma.elitex.utils.WorkData;

public class LoadWorkActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener, ExitListener {

    private final String LOG_TAG = LoadWorkActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private TextView mMassage; // Initial massage

    // CONTAINERS
    private LinearLayout mOperationsConteiner;
    private LinearLayout mBatchContainer;

    // BUTTONS
    private Button mLoadOperation;
    private Button mLoadMoreOperations;
    private Button mLoadBatch;
    private Button mBack;
    private Button mStartWork;

    // OPERATIONS
    private ArrayList<Operation> mOperations; // Object for storing the result data from server requests
    private OperationsListAdapter mOperationsAdapter;
    private TextView mOperationsTile;

    // BATCH
    private Batch mBatch; // Object for storing the result data from server requests
    private TextView mBatchNumber;
    private TextView mBatchCount;
    private TextView mBatchSize;
    private TextView mBatchColour;
    private TextView mBatchFeatures;

    // DIALOGS
    private LoadingDialog mLoading;
    private MassageDialog mMassageDialog;

    //
    private boolean mLoadingOperation = false;
    private boolean mLoadingBatch = false;
    private boolean mStartingWork = false;
    private boolean isSeparate = false; // checks if the operations are separate
    private boolean mOperationTitleSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_work);

        // Dims the navigation buttons
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Keeps the screen on while the app is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initializing server communication
        mResultReceiver = new ServerResultReceiver(new Handler());
        mResultReceiver.serListener(this);

        // Initializing Elitex data and setting action bar title
        mElitexData = new ElitexData(this);
        Utils.setupActionBar(this, mElitexData.getActionBarTitle(), getSupportActionBar());

        mOperations = new ArrayList<>(); // Initialize operations array

        initViews();
        initOperations();
    }

    private void initViews() {
        // INITIAL MASSAGE
        mMassage = (TextView) findViewById(R.id.load_info_massage);

        // OPERATIONS TITLE
        mOperationsTile = (TextView) findViewById(R.id.load_operations_title);

        // CONTAINERS
        mOperationsConteiner = (LinearLayout) findViewById(R.id.load_info_operation_container);
        mBatchContainer = (LinearLayout) findViewById(R.id.info_load_batch_container);

        // BATCH
        mBatchNumber = (TextView) findViewById(R.id.load_info_batch_number);
        mBatchCount = (TextView) findViewById(R.id.load_info_batch_count);
        mBatchSize = (TextView) findViewById(R.id.load_info_size);
        mBatchColour = (TextView) findViewById(R.id.load_info_colour);
        mBatchFeatures = (TextView) findViewById(R.id.load_info_features);

        //BUTTONS
        mLoadOperation = (Button) findViewById(R.id.load_info_load_button);
        mLoadBatch = (Button) findViewById(R.id.load_info_load_batch_button);
        mBack = (Button) findViewById(R.id.load_info_cancel_button);
        mLoadMoreOperations = (Button) findViewById(R.id.info_load_continue_load);
        mStartWork = (Button) findViewById(R.id.load_info_start_work_button);

        mBatchFeatures.setOnClickListener(this);
        mLoadOperation.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mLoadMoreOperations.setOnClickListener(this);
        mLoadBatch.setOnClickListener(this);
        mStartWork.setOnClickListener(this);

        mLoading = new LoadingDialog(this);
        mMassageDialog = new MassageDialog(this);

        mOperationsAdapter = new OperationsListAdapter(this, mOperations);
        ListView list = (ListView) findViewById(R.id.load_info_operations_list);
        list.setAdapter(mOperationsAdapter);
    }

    private void initOperations() {
        try {
            Set<String> operations = mElitexData.getOperations();
            for (String operationStr : operations) {
                readOperationFromJson(operationStr);
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.load, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reference:
                startActivity(new Intent(this, ReferenceActivity.class));
                return true;
            case R.id.action_exit:
                new ExitDialog(this, this, true).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_info_features:
                new FeaturesDialog(this, mBatch.mFeatures).show();
                break;
            case R.id.load_info_load_button:
                readOperationBarcode();
                break;
            case R.id.info_load_continue_load:
                readOperationBarcode();
                break;
            case R.id.load_info_load_batch_button:
                readBatchQRCode();
                break;
            case R.id.load_info_start_work_button:
                startWork();
                break;
            case R.id.load_info_cancel_button:
                handleBackButton();
                break;
        }
    }

    /**
     * Start barcode reader   for operation
     */
    private void readOperationBarcode() {
        Log.d(LOG_TAG, "readOperationBarcode");
        mLoadingOperation = true;
        mLoadingBatch = false;
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getString(R.string.massage_barcode));
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    /**
     * Start QR reader activity for batch
     */
    private void readBatchQRCode() {
        Log.d(LOG_TAG, "readBatchQRCode");
        mLoadingBatch = true;
        mLoadingOperation = false;
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getString(R.string.massage_barcode));
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    private void handleBackButton() {
        if (mBatch != null) {
            resetBatchLoading();
        } else {
            resetOperationLoading();
        }
    }

    /**
     * Handles results from barcode/QR code read activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle results form QR/Barcode scanning
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                if (mLoadingOperation) {
                    if (mOperations.isEmpty()) {
                        resetOperationLoading();
                    }
                    mMassageDialog.setMassageText(getString(R.string.massage_barcode_failed));
                    mMassageDialog.show();
                }

                if (mLoadingBatch) {
                    resetBatchLoading();
                    mMassageDialog.setMassageText(getString(R.string.massage_qr_code_failed));
                    mMassageDialog.show();
                }
            } else {
                if (mLoadingOperation) {
                    loadOperation(result.getContents());
                }
                if (mLoadingBatch) {
                    loadBatch(result.getContents());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initiates loading operation in ServerConnectionService
     *
     * @param operationID ID of the operation that needs to be loaded
     */
    private void loadOperation(String operationID) {
        Log.d(LOG_TAG, operationID);
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.LOAD_OPERATION);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_operation_id), trimOperationId(operationID));
        startService(intent);
        mLoading.show();
    }

    /**
     * Initiates loading batch in ServerConnectionService
     *
     * @param batchString Json string of the batch that needs to be loaded
     */
    private void loadBatch(String batchString) {

        try {
            JSONObject json = new JSONObject(batchString);

            // Check if QR code is valid
            if (!json.has(getString(R.string.key_id))) {
                throw new JSONException("No ID key");
            }

            int batchID = json.getInt(getString(R.string.key_id));

            Intent intent = new Intent(this, ServerConnectionService.class);
            intent.putExtra(getString(R.string.key_listener), mResultReceiver);
            intent.putExtra(getString(R.string.key_request), ServerRequests.LOAD_BATCH);
            intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
            intent.putExtra(getString(R.string.key_batch_id), String.valueOf(batchID));
            intent.putExtra(getString(R.string.key_operation_id), mOperations.get(0).id);
            startService(intent);
            mLoadingBatch = true;
            mLoading.show();

        } catch (JSONException e) {
            if (mLoading.isShowing()) mLoading.dismiss();
            mMassageDialog.setMassageText(getString(R.string.massage_batch_failed));
            mMassageDialog.show();
            Log.d(LOG_TAG, e.toString());
        }
    }

    private void startWork() {
        JSONArray operations = new JSONArray();
        for (Operation operation : mOperations) {
            operations.put(Integer.valueOf(operation.id));
        }
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.START_WORK);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_order_id), mOperations.get(0).orderId);
        intent.putExtra(getString(R.string.key_process_id), operations.toString());
        if (!isSeparate) intent.putExtra(getString(R.string.key_batch_id), mBatch.mBatchId);
        startService(intent);
        mStartingWork = true;
        mLoading.show();
    }

    @Override
    public void requestReady(String result, String serverTime) {
        Log.d(LOG_TAG, result);
        mLoading.dismiss(); // Remove loading dialog
        JSONObject json;
        // Parse resulting string in json format
        try {
            json = new JSONObject(result);
            if (mLoadingOperation) {
                mElitexData.addOperation(result);
                readOperationFromJson(result);
            }
            if (mLoadingBatch) {
                readBatchFromJson(json);
            }
            if (mStartingWork) {
                readStartWorkFromJson(json);
            }
        } catch (JSONException e) {
            // If there is a error during parsing, log the error, show error massage dialog.
            // If the error happened during loading operation reset the hole view.
            // If the error happened during loading batch reset only the batch data.
            Log.d(LOG_TAG, e.toString());
            Crashlytics.logException(e);

            if (mLoadingOperation) {
                mMassageDialog.setMassageText(getString(R.string.massage_operation_failed));
                mMassageDialog.show();
                if (mOperations.isEmpty()) {
                    resetOperationLoading();
                }
            }
            if (mLoadingBatch) {
                resetBatchLoading();
                mMassageDialog.setMassageText(getString(R.string.massage_batch_failed));
                mMassageDialog.show();
            }
            if (mStartingWork) {
                mStartingWork = false;
                mMassageDialog.setMassageText(getString(R.string.massage_start_work_failed));
                mMassageDialog.show();
            }
        }
    }

    @Override
    public void requestFailed() {
        mLoading.dismiss();
        mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
        mMassageDialog.show();

        if (mLoadingOperation) {
            mLoadingOperation = false;
            if (mOperations.isEmpty()) {
                resetOperationLoading();
            }
        }

        if (mLoadingBatch) {
            resetBatchLoading();
        }

        if (mStartingWork) {
            mStartingWork = false;
        }
    }

    /**
     * Reads the operation data from the server result json and stores it in the
     * mOperationAndBatch object
     *
     * @throws JSONException If there is a problem the method delegates the exception to the calling method
     */
    private void readOperationFromJson(String json) throws JSONException {
        Operation operation = getOperation(json);

        // If this is the first operation loaded set isSeparate for additional operations check
        if (mOperations.isEmpty()) {
            isSeparate = operation.isSeparate;
        }

        if (isSeparate != operation.isSeparate) {
            // The operation type is different
            int typeRes = isSeparate ? R.string.operation_type_separate : R.string.operation_type_normal;
            String operationType = getString(typeRes);
            mMassageDialog.setMassageText(getString(R.string.massage_is_separate, operationType));
            mMassageDialog.show();
            return;

        } else if (checkOperationLoaded(operation.id)) {
            // The operation already exists
            mMassageDialog.setMassageText(getString(R.string.massage_operation_exists));
            mMassageDialog.show();
            return;

        } else {
            // Everything is ok add the operation
            mOperations.add(operation);
            mOperationsAdapter.notifyDataSetChanged();
            if (!mOperationTitleSet) {
                mOperationsTile.setText(String.format("%s - %s", operation.orderModel, operation.orderName));
                mOperationTitleSet = true;
            }
        }

        mOperationsConteiner.setVisibility(View.VISIBLE);
        mLoadMoreOperations.setVisibility(View.VISIBLE);
        mLoadBatch.setVisibility(View.VISIBLE);
        mLoadOperation.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mMassage.setVisibility(View.GONE);
        mLoadingOperation = false;

        if (isSeparate) {
            mLoadBatch.setVisibility(View.GONE);
            mStartWork.setVisibility(View.VISIBLE);
        }
    }

    private Operation getOperation(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);

        JSONObject order = json.getJSONObject(getString(R.string.key_order));
        JSONObject machine = json.getJSONObject(getString(R.string.key_machine_type));

        return new Operation(
                String.valueOf(json.getInt(getString(R.string.key_id))),
                json.getString(getString(R.string.key_name)),
                String.valueOf(json.getInt(getString(R.string.key_serial_number))),
                json.getString(getString(R.string.key_aligned_time)),
                json.getBoolean(getString(R.string.key_separate)),
                String.valueOf(order.getInt(getString(R.string.key_id))),
                order.getString(getString(R.string.key_name)),
                order.getString(getString(R.string.key_model)),
                order.getString(getString(R.string.key_identification_number)),
                order.getString(getString(R.string.key_client_name)),
                order.getString(getString(R.string.key_client_model)),
                order.getString(getString(R.string.key_client_identification_number)),
                String.valueOf(machine.getInt(getString(R.string.key_id))),
                machine.getString(getString(R.string.key_name))
        );
    }

    /**
     * Reads the batch data from the server result json and stores it in the
     * mOperationAndBatch object
     *
     * @throws JSONException If there is a problem the method delegates the exception to the calling method
     */
    private void readBatchFromJson(JSONObject json) throws JSONException {
        JSONObject dist = json.getJSONObject(getString(R.string.key_distribution));

        mBatch = new Batch(
                json.getInt(getString(R.string.key_id)),
                json.getInt(getString(R.string.key_position)),
                json.has(getString(R.string.key_features)) ? json.getString(getString(R.string.key_features)) : "",
                dist.getString(getString(R.string.key_color)),
                json.getInt(getString(R.string.key_number)),
                json.getInt(getString(R.string.key_made_pieces)),
                json.getInt(getString(R.string.key_remaining_pieces)),
                dist.getString(getString(R.string.key_size))
        );

        mLoadingBatch = false; // The batch is loaded

        // Check if the pieces needed are 0 if yes the batch is completed, show massage
        // and reset loading batch
        if (mBatch.mRemaining <= 0) {
            resetBatchLoading();
            mMassageDialog.setMassageText(getString(R.string.massage_batch_ready));
            mMassageDialog.show();
            return;
        }

        // Set values to view
        mBatchContainer.setVisibility(View.VISIBLE);
        mBatchCount.setText(getString(R.string.title_batch_count, String.valueOf(mBatch.mRemaining)));
        mBatchNumber.setText(getString(R.string.title_batch_number, String.valueOf(mBatch.mBatchNumber)));
        mBatchSize.setText(getString(R.string.title_batch_size, mBatch.mSize));
        mBatchColour.setText(getString(R.string.title_batch_colour, mBatch.mColour));

        // Set features visibility
        if (mBatch.mFeatures == null || mBatch.mFeatures.isEmpty()) {
            mBatchFeatures.setVisibility(View.GONE);
        } else {
            mBatchFeatures.setVisibility(View.VISIBLE);
            mBatchFeatures.setText(mBatch.mFeatures);
        }

        mLoadBatch.setVisibility(View.GONE); // Hide load batch button
        mStartWork.setVisibility(View.VISIBLE); // Show start work button
        mLoadMoreOperations.setVisibility(View.GONE); // Hide load more operations button
    }

    private void readStartWorkFromJson(JSONObject json) throws JSONException {

        JSONObject mainObj = json.getJSONObject(getString(R.string.key_earnings));
        JSONArray workIDs = mainObj.getJSONArray(getString(R.string.key_ids));
        JSONArray operationIDs = new JSONArray();
        for (Operation operation : mOperations) {
            operationIDs.put(operation.serialNumber);
        }
        Operation operation = mOperations.get(0);
        String workTitle = String.format("%1$s - %2$s / %3$s - %4$s",
                operation.orderName,
                operation.orderModel,
                operation.clientName,
                operation.clientIdentificationNumber);

        Log.d(LOG_TAG, workTitle);
        Log.d(LOG_TAG, workIDs.toString());
        Log.d(LOG_TAG, operationIDs.toString());

        mElitexData.setWorkData(new WorkData(mBatch, operationIDs, workIDs, workTitle, Utils.getCurrentDate(), isSeparate));

        // Start work activity
        Intent intent = new Intent(this, WorkActivity.class);
        startActivity(intent);
    }

    private void resetOperationLoading() {
        mLoadingOperation = false;
        mLoadingBatch = false;
        mStartingWork = false;
        mOperationTitleSet = false;

        mOperationsConteiner.setVisibility(View.GONE); // Hide operations container
        mBatchContainer.setVisibility(View.GONE); // Hide batch container
        mLoadMoreOperations.setVisibility(View.GONE); // Hide load more operations button
        mLoadBatch.setVisibility(View.GONE); // Hide load batch button
        mStartWork.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE); // Hide back button

        mOperations.clear(); // Clear all loaded operations
        mOperationsAdapter.notifyDataSetChanged(); // Clear the list

        mLoadOperation.setVisibility(View.VISIBLE); // Show load operation button
        mMassage.setVisibility(View.VISIBLE); // Show initial massage

        mElitexData.resetOperations(); // Remove stored operations
    }

    private void resetBatchLoading() {
        mLoadingBatch = false; // Waiting to start loading batch
        mBatch = null; // Remove batch data

        mMassage.setVisibility(View.GONE); // Remove massage if visible /just in case/
        mOperationsConteiner.setVisibility(View.VISIBLE); // Operation is loaded so the data container must be visible
        mBatchContainer.setVisibility(View.GONE); // Batch is still not loaded, hide batch container
        mLoadBatch.setVisibility(View.VISIBLE); // Show load batch button
        mStartWork.setVisibility(View.GONE); // Hide start work button
    }

    /**
     * Checks if the operation is already loaded
     *
     * @param operationID The id string to check
     * @return true if the operation exists
     */
    private boolean checkOperationLoaded(String operationID) {
        for (Operation operation : mOperations) {
            if (operation.id.equalsIgnoreCase(operationID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes additional information from operation id
     * Additional data can be "P:" or "Id:" in the start of the string
     *
     * @param operationID the full id returned from the server
     * @return the id with no additional data
     */
    private String trimOperationId(String operationID) {
        char first = operationID.charAt(0);
        if (first == 'P') {
            return operationID.substring(2);
        } else if (first == 'I') {
            return operationID.substring(3);
        }
        return operationID;
    }

    @Override
    public void logout() {
        mElitexData.setAccessToken(""); // Remove token

        // Change automatic login to false so the sign in activity behaves properly
        User user = mElitexData.getUserData();
        user.mKeepLogged = false;
        mElitexData.addUserData(user);

        // Start sign in activity
        startActivity(new Intent(this, SignInActivity.class));
    }

    @Override
    public void exitApp() {
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }
}
