package tma.elitex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerListener;
import tma.elitex.server.ServerRequests;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, ServerListener {

    private final String LOG_TAG = SignInActivity.class.getSimpleName();

    private EditText mUserEditText; // User name input field
    private EditText mPasswordEditText; // Password input field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initializing input
        mUserEditText = (EditText) findViewById(R.id.sign_in_user);
        mPasswordEditText = (EditText) findViewById(R.id.sign_in_password);

        // Initializing buttons
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_in_button_qr).setOnClickListener(this);
    }

    /**
     * Handles on button click events
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                loginUser();
                break;
            case R.id.sign_in_button_qr:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt(getString(R.string.massage_barcode));
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
                break;
        }
    }

    /**
     *  Initializes the server user login
     */
    private void loginUser () {
        if (checkUserInput()) {
            String userName = mUserEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            ServerListener listener = this;
            Intent intent = new Intent(this, ServerConnectionService.class);
            intent.putExtra(getString(R.string.key_listener), listener);
            intent.putExtra(getString(R.string.key_request), ServerRequests.LOGIN_USER);
            intent.putExtra(getString(R.string.key_user_name), userName);
            intent.putExtra(getString(R.string.key_password), password);
            startService(intent);
        }
    }
    /**
     *  Initializes the server token login
     */
    private void loginToken (String token) {
        ServerListener listener = this;
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), listener);
        intent.putExtra(getString(R.string.key_token), token);
        startService(intent);
    }

    /**
     * Checks if the user name and password input is not empty
     */
    private boolean checkUserInput () {
        String userName = mUserEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        // Remove all whitespaces
        userName = userName.replaceAll("//s+", "");
        password = password.replaceAll("//s+", "");

        if (userName.isEmpty()) {
            Toast.makeText(this, getString(R.string.massage_user), Toast.LENGTH_LONG).show();
            mUserEditText.setText("");
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, getString(R.string.massage_password), Toast.LENGTH_LONG).show();
            mPasswordEditText.setText("");
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle results form QR/Barcode scanning
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(LOG_TAG, "QR scan failed");
            } else {
                loginToken(result.getContents());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void requestReady(String result) {
        try {
            JSONObject json = new JSONObject(result);
            if (json.has(getString(R.string.key_token))) {
                // Retrieving token and initializing token login
                String token = json.getString(getString(R.string.key_token));
                loginToken(token);

            } else if (json.has(getString(R.string.key_massage))) {
                // There was a problem with the request get and show massage
                String massage = json.getString(getString(R.string.key_massage));
                Toast.makeText(this, massage, Toast.LENGTH_LONG).show();

            } else {
                // The server has returned unknown result log it
                Log.d(LOG_TAG, "Unknown server result");
            }
        } catch (JSONException e) {
            // The server has returned unknown result log it
            Log.d(LOG_TAG, e.toString());
        }
    }

    @Override
    public void requestFailed(Exception e) {
        Log.d(LOG_TAG, e.toString());
    }
}
