package tma.elitex.load;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import tma.elitex.R;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.OperationAndBatch;

/**
 * Created by Krum Iliev.
 */
public class MainScreenActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener {

    private final String LOG_TAG = MainScreenActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    // Variables for controlling activity loading flow
    private boolean mOperationLoaded = false;
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

    private OperationAndBatch mOerationAndBatch; // Object for storing the result data from server requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_load_button:
                startLoading();
                break;
            case R.id.info_cancel_button:
                resetView();
                break;
            case R.id.info_features:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Handle results form QR/Barcode scanning
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //TODO show error massage dialog

                if (mLoadingOperation) resetView();
                if (mLoadingBatch) resetViewWithOperation();

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
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.LOAD_OPERATION);
        intent.putExtra(getString(R.string.key_operation_id), operationID);
        startService(intent);
    }

    /**
     * Initiates loading batch in ServerConnectionService
     *
     * @param batchID ID of the batch that needs to be loaded
     */
    private void loadBatch(String batchID) {
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.LOAD_BATCH);
        intent.putExtra(getString(R.string.key_operation_id), batchID);
        startService(intent);
    }

    @Override
    public void requestReady(String result) {
        JSONObject json;

        // Parse resulting string in json format
        try {
            json = new JSONObject(result);

            if (mLoadingOperation) {
                //TODO show info dialog
            }

            if (mLoadingBatch) {
                //TODO show info dialog
            }

        } catch (JSONException e) {
            // If there is a error during parsing, log the error, show error massage dialog.
            // If the error happened during loading operation reset the hole view.
            // If the error happened during loading batch reset only the batch data.
            Log.d(LOG_TAG, e.toString());
            // TODO show error massage dialog
            if (mLoadingOperation) resetView();
            if (mLoadingBatch) resetViewWithOperation();
            return;
        }
    }

    /**
     * Resets the entire view to initial parameters
     */
    private void resetView() {
        mLoadingOperation = false;
        mLoadingBatch = false;
        mOperationLoaded = false;
        mMassage.setVisibility(View.VISIBLE); // Show massage
        mOperationContainer.setVisibility(View.GONE); // Remove operation data container from the view
        mBatchContainer.setVisibility(View.GONE); // Remove batch data container from the view
        mCancel.setVisibility(View.GONE); // Remove cancel button from view it is not needed when loading operation
        mLoad.setText(getString(R.string.button_load)); // Change load button text
        if (mOerationAndBatch != null) mOerationAndBatch.reset(); // Reset the data holding object /just in case/. Object can be null.
    }

    /**
     * Resets the view before the batch loading operation
     */
    private void resetViewWithOperation() {
        mLoadingOperation = false; // Operation is already loaded so this should be false
        mOperationLoaded = true;
        mLoadingBatch = false; // Waiting to start loading batch
        mMassage.setVisibility(View.GONE); // Remove massage if visible /just in case/
        mOperationContainer.setVisibility(View.VISIBLE); // Operation is loaded so the data container must be visible
        mBatchContainer.setVisibility(View.GONE); // Batch is still not loaded, hide batch container
        mCancel.setVisibility(View.VISIBLE); // Show cancel button to cancel the current operation
        mLoad.setText(getString(R.string.button_load_qr)); // Change load button text
        mOerationAndBatch.resetBatch(); // Reset the batch data /just in case/
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
            integrator.setDesiredBarcodeFormats(IntentIntegrator.DATA_MATRIX_TYPES);
            mLoadingOperation = true;
        }

        integrator.setPrompt(getString(R.string.massage_barcode));
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }
}
