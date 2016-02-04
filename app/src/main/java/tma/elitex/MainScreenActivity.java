package tma.elitex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import tma.elitex.Utils.ElitexData;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;

/**
 * Created by Krum.
 */
public class MainScreenActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener, MainScreenListener {

    private final String LOG_TAG = MainScreenActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private Button mButton;
    private TextView mText;

    private boolean mOperationLoaded = false;
    private boolean mLoadingOperation = false;
    private boolean mLoadingBatch = false;

    private MainScreenInfoDialog mInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing server communication
        mResultReceiver = new ServerResultReceiver(new Handler());
        mResultReceiver.serListener(this);

        // Initializing fields
        mButton = (Button) findViewById(R.id.main_load_button);
        mText = (TextView) findViewById(R.id.main_info_text);
        mButton.setOnClickListener(this);

        // Initializing Elitex data and setting action bar title
        mElitexData = new ElitexData(this);
        getSupportActionBar().setTitle(mElitexData.getActionBarTitle());

        mInfoDialog = new MainScreenInfoDialog(this, this);
    }

    @Override
    public void onClick(View v) {
        // Initiating barcode/QR code scanning.
        // If the operation is loaded the QR code scanner will be activated.
        // Else the barcode scanner will be activated.

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Handle results form QR/Barcode scanning
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                mText.setText(getString(R.string.massage_load_failed));
                mLoadingOperation = false;
                mLoadingBatch = false;

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
    private void loadOperation (String operationID) {
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
    private void loadBatch (String batchID) {
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.LOAD_BATCH);
        intent.putExtra(getString(R.string.key_operation_id), batchID);
        startService(intent);
    }

    @Override
    public void requestReady(String result) {
        JSONObject json;

        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.toString());
            Toast.makeText(this, getString(R.string.massage_server_failed), Toast.LENGTH_LONG).show();
            return;
        }

        if (mLoadingOperation) {
            //TODO show info dialog
        }

        if (mLoadingBatch) {
            //TODO show info dialog
        }
    }

    @Override
    public void continueLoad() {
        if (mLoadingOperation) {
            mOperationLoaded = true;
            mLoadingOperation = false;
            mText.setText(getString(R.string.massage_operation_qr_code));
            mButton.setText(getString(R.string.button_load_qr));

            //TODO continue with loading batch
        }

        if (mLoadingBatch) {
            mLoadingBatch = false;

            //TODO continue with operation
        }
    }

    @Override
    public void restartLoad() {
        mLoadingOperation = false;
        mLoadingBatch = false;
        mOperationLoaded = false;
        mText.setText(getString(R.string.massage_operation_barcode));
        mButton.setText(getString(R.string.button_load));
    }
}
