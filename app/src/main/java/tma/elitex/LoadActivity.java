package tma.elitex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import tma.elitex.reference.ReferenceActivity;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.ErrorDialog;
import tma.elitex.utils.FeaturesDialog;
import tma.elitex.utils.LoadingDialog;
import tma.elitex.utils.OperationAndBatch;

/**
 * Created by Krum Iliev.
 */
public class LoadActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener {

    private final String LOG_TAG = LoadActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    // Variables for controlling activity loading flow
    private boolean mOperationLoaded = false;
    private boolean mBatchLoaded = false;
    private boolean mLoadingOperation = false;
    private boolean mLoadingBatch = false;

    // Views for holding operation and batch information
    private LinearLayout mOperationContainer;
    private LinearLayout mBatchContainer;
    private TextView mOperationName;
    private TextView mOperationModel;
    private TextView mOperationMachine;
    private TextView mBatchNumber;
    private TextView mBatchCount;
    private TextView mBatchSize;
    private TextView mBatchColour;
    private TextView mBatchFeatures;
    private TextView mMassage;

    // View buttons
    private Button mLoad;
    private Button mCancel;

    private OperationAndBatch mOperationAndBatch; // Object for storing the result data from server requests

    private LoadingDialog mLoading;
    private ErrorDialog mErrorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        // Initializing server communication
        mResultReceiver = new ServerResultReceiver(new Handler());
        mResultReceiver.serListener(this);

        // Initializing Elitex data and setting action bar title
        mElitexData = new ElitexData(this);
        getSupportActionBar().setTitle(mElitexData.getActionBarTitle());

        initViews();
    }

    /**
     * Initializes all activity related views
     */
    private void initViews() {
        mOperationContainer = (LinearLayout) findViewById(R.id.info_operation_container);
        mBatchContainer = (LinearLayout) findViewById(R.id.info_batch_container);
        mOperationName = (TextView) findViewById(R.id.info_operation);
        mOperationModel = (TextView) findViewById(R.id.info_model);
        mOperationMachine = (TextView) findViewById(R.id.info_machine);
        mBatchNumber = (TextView) findViewById(R.id.info_batch_number);
        mBatchCount = (TextView) findViewById(R.id.info_batch_count);
        mBatchSize = (TextView) findViewById(R.id.info_size);
        mBatchColour = (TextView) findViewById(R.id.info_colour);
        mBatchFeatures = (TextView) findViewById(R.id.info_features);
        mLoad = (Button) findViewById(R.id.info_load_button);
        mCancel = (Button) findViewById(R.id.info_cancel_button);
        mMassage = (TextView) findViewById(R.id.info_massage);

        mBatchFeatures.setOnClickListener(this);
        mLoad.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        mLoading = new LoadingDialog(this);
        mErrorDialog = new ErrorDialog(this);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_load_button:
                if (mBatchLoaded) {
                    mElitexData.addOperationAndBatch(mOperationAndBatch);
                    Intent intent = new Intent(this, WorkActivity.class);
                    startActivity(intent);
                    // TODO start working process on server
                    // TODO start working activity
                } else {
                    startLoading();
                }
                break;
            case R.id.info_cancel_button:
                if (mBatchLoaded) {
                    resetViewBatch();
                } else {
                    resetView();
                }
                break;
            case R.id.info_features:
                new FeaturesDialog(this, mOperationAndBatch.mFeatures).show();
                break;
        }
    }

    public void startLoading() {
        // Initiating barcode/QR code scanning.
        // If the operation is loaded the QR code scanner will be activated.
        // Else the barcode scanner will be activated.

        mMassage.setVisibility(View.GONE); // Remove massage from the view

        IntentIntegrator integrator = new IntentIntegrator(this);

        if (mOperationLoaded) {
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            mLoadingBatch = true;
        } else {
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            mLoadingOperation = true;
        }

        integrator.setPrompt(getString(R.string.massage_barcode));
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Handle results form QR/Barcode scanning
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                if (mLoadingOperation) {
                    resetView();
                    mErrorDialog.setMassageText(getString(R.string.massage_barcode_failed));
                    mErrorDialog.show();
                }

                if (mLoadingBatch) {
                    resetViewBatch();
                    mErrorDialog.setMassageText(getString(R.string.massage_qr_code_failed));
                    mErrorDialog.show();
                }

            } else {
                if (mLoadingOperation) loadOperation(result.getContents());
                if (mLoadingBatch) loadBatch(result.getContents());
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
            if (!json.has(getString(R.string.key_id))) throw new JSONException("No ID key");

            int batchID = json.getInt(getString(R.string.key_id));

            Intent intent = new Intent(this, ServerConnectionService.class);
            intent.putExtra(getString(R.string.key_listener), mResultReceiver);
            intent.putExtra(getString(R.string.key_request), ServerRequests.LOAD_BATCH);
            intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
            intent.putExtra(getString(R.string.key_batch_id), String.valueOf(batchID));
            startService(intent);
            mLoading.show();

        } catch (JSONException e) {
            //TODO hide loading dialog
            mErrorDialog.setMassageText(getString(R.string.massage_batch_failed));
            mErrorDialog.show();
            Log.d(LOG_TAG, e.toString());
        }
    }

    @Override
    public void requestReady(String result) {
        Log.d(LOG_TAG, result);
        mLoading.dismiss(); // Remove loading dialog
        JSONObject json;

        // Parse resulting string in json format
        try {
            json = new JSONObject(result);
            if (mLoadingOperation) readOperationFromJson(json);
            if (mLoadingBatch) readBatchFromJson(json);

        } catch (JSONException e) {
            // If there is a error during parsing, log the error, show error massage dialog.
            // If the error happened during loading operation reset the hole view.
            // If the error happened during loading batch reset only the batch data.
            Log.d(LOG_TAG, e.toString());

            if (mLoadingOperation) {
                resetView();
                mErrorDialog.setMassageText(getString(R.string.massage_operation_failed));
                mErrorDialog.show();
            }
            if (mLoadingBatch) {
                resetViewBatch();
                mErrorDialog.setMassageText(getString(R.string.massage_batch_failed));
                mErrorDialog.show();
            }
            return;
        }
    }

    @Override
    public void requestFailed(String error) {
        mLoading.dismiss();
        mErrorDialog.setMassageText(error);
        mErrorDialog.show();

        if (mLoadingOperation) resetView();
        if (mLoadingBatch) resetViewBatch();
    }

    /**
     * Reads the operation data form the server result json and stores it in the
     * mOperationAndBatch object
     *
     * @throws JSONException If there is a problem the method delegates the exception to the calling method
     */
    private void readOperationFromJson(JSONObject json) throws JSONException {
        // Create data holding object
        if (mOperationAndBatch != null) {
            // if the object exists already reuse it
            mOperationAndBatch.setOperation(
                    trimOperationId(json.getString(getString(R.string.key_id))),
                    json.getString(getString(R.string.key_name)),
                    json.getInt(getString(R.string.key_serial_number)),
                    json.getDouble(getString(R.string.key_aligned_time)),
                    json.getJSONObject(getString(R.string.key_order)).getInt(getString(R.string.key_id)),
                    json.getJSONObject(getString(R.string.key_order)).getString(getString(R.string.key_name)),
                    json.getJSONObject(getString(R.string.key_machine_type)).getInt(getString(R.string.key_id)),
                    json.getJSONObject(getString(R.string.key_machine_type)).getString(getString(R.string.key_name))
            );
        } else {
            // else create the object
            mOperationAndBatch = new OperationAndBatch(
                    trimOperationId(json.getString(getString(R.string.key_id))),
                    json.getString(getString(R.string.key_name)),
                    json.getInt(getString(R.string.key_serial_number)),
                    json.getDouble(getString(R.string.key_aligned_time)),
                    json.getJSONObject(getString(R.string.key_order)).getInt(getString(R.string.key_id)),
                    json.getJSONObject(getString(R.string.key_order)).getString(getString(R.string.key_name)),
                    json.getJSONObject(getString(R.string.key_machine_type)).getInt(getString(R.string.key_id)),
                    json.getJSONObject(getString(R.string.key_machine_type)).getString(getString(R.string.key_name))
            );
        }

        mOperationContainer.setVisibility(View.VISIBLE);
        mOperationName.setText(mOperationAndBatch.mOperationName);
        mOperationModel.setText(mOperationAndBatch.mModelName);
        mOperationMachine.setText(mOperationAndBatch.mMachineName);
        mLoad.setText(getString(R.string.button_load_qr));
        mCancel.setVisibility(View.VISIBLE);
        mOperationLoaded = true;
        mLoadingOperation = false;
    }

    /**
     * Reads the batch data form the server result json and stores it in the
     * mOperationAndBatch object
     *
     * @throws JSONException If there is a problem the method delegates the exception to the calling method
     */
    private void readBatchFromJson(JSONObject json) throws JSONException {
        JSONObject dist = json.getJSONObject(getString(R.string.key_distribution));
        mOperationAndBatch.setBatch(
                json.getInt(getString(R.string.key_id)),
                0, // TODO change after api implementation
                json.getString(getString(R.string.key_features)),
                dist.getString(getString(R.string.key_color)),
                json.getInt(getString(R.string.key_number)),
                json.getInt(getString(R.string.key_made_pieces)),
                json.getInt(getString(R.string.key_remaining_pieces)),
                dist.getString(getString(R.string.key_size))
        );

        mBatchContainer.setVisibility(View.VISIBLE);
        mBatchCount.setText(getString(R.string.title_batch_count) + " " + mOperationAndBatch.mBatchCount);
        mBatchNumber.setText(getString(R.string.title_batch_number) + " " + mOperationAndBatch.mBatchNumber);
        mBatchSize.setText(getString(R.string.title_batch_size) + " " + mOperationAndBatch.mSize);
        mBatchColour.setText(getString(R.string.title_batch_colour) + " " + mOperationAndBatch.mColour);

        if (mOperationAndBatch.mFeatures == null || mOperationAndBatch.mFeatures.isEmpty()) {
            mBatchFeatures.setVisibility(View.GONE);
        } else {
            mBatchFeatures.setVisibility(View.VISIBLE);
            mBatchFeatures.setText(mOperationAndBatch.mFeatures);
        }

        mLoad.setText(getString(R.string.button_load_start));
        mLoadingBatch = false;
        mBatchLoaded = true;
    }

    /**
     * Resets the entire view to initial parameters
     */
    private void resetView() {
        mLoadingOperation = false;
        mLoadingBatch = false;
        mOperationLoaded = false;
        mBatchLoaded = false;
        mMassage.setVisibility(View.VISIBLE); // Show massage
        mOperationContainer.setVisibility(View.GONE); // Remove operation data container from the view
        mBatchContainer.setVisibility(View.GONE); // Remove batch data container from the view
        mCancel.setVisibility(View.GONE); // Remove cancel button from view it is not needed when loading operation
        mLoad.setText(getString(R.string.button_load)); // Change load button text
        if (mOperationAndBatch != null)
            mOperationAndBatch.reset(); // Reset the data holding object /just in case/. Object can be null.
    }

    /**
     * Resets the view before the batch loading operation
     */
    private void resetViewBatch() {
        mLoadingOperation = false; // Operation is already loaded so this should be false
        mOperationLoaded = true;
        mLoadingBatch = false; // Waiting to start loading batch
        mBatchLoaded = false;
        mMassage.setVisibility(View.GONE); // Remove massage if visible /just in case/
        mOperationContainer.setVisibility(View.VISIBLE); // Operation is loaded so the data container must be visible
        mBatchContainer.setVisibility(View.GONE); // Batch is still not loaded, hide batch container
        mCancel.setVisibility(View.VISIBLE); // Show cancel button to cancel the current operation
        mLoad.setText(getString(R.string.button_load_qr)); // Change load button text
        mOperationAndBatch.resetBatch(); // Reset the batch data /just in case/
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
}
