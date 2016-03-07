package tma.elitex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import tma.elitex.reference.ReferenceActivity;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.ExitDialog;
import tma.elitex.utils.ExitListener;
import tma.elitex.utils.LoadingDialog;
import tma.elitex.utils.MassageDialog;
import tma.elitex.utils.User;

/**
 * Created by Krum Iliev.
 */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener, ExitListener {

    private final String LOG_TAG = SignInActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private EditText mUserEditText; // User name input field
    private EditText mPasswordEditText; // Password input field
    private CheckBox mKeepLogged; // Checkbox if the user wants to automatically log in on the next app start

    private LoadingDialog mLoading;
    private MassageDialog mMassageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_sign_in);

        // Dims the navigation buttons
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Keeps the screen on while the app is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mElitexData = new ElitexData(this);
        checkIfWorkWasComplected();

        // Checks if the user has checked keep logged on his previous login
        if (mElitexData.getUserData().mKeepLogged) {
            Intent intent = new Intent(this, LoadActivity.class);
            startActivity(intent);
        }

        // Initializing server communication
        mResultReceiver = new ServerResultReceiver(new Handler());
        mResultReceiver.serListener(this);

        // Initializing input
        mUserEditText = (EditText) findViewById(R.id.sign_in_user);
        mPasswordEditText = (EditText) findViewById(R.id.sign_in_password);
        mKeepLogged = (CheckBox) findViewById(R.id.sign_in_keep_logged);

        // Initializing buttons
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_in_button_qr).setOnClickListener(this);

        mLoading = new LoadingDialog(this);
        mMassageDialog = new MassageDialog(this);

        // Hides soft keyboard on initial start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Checks if the user closed the application from home button or the application crashed
     * and if yes it restores it to its previous state
     */
    private void checkIfWorkWasComplected() {
        try {
            // User access token if its null or empty return to login
            String token = mElitexData.getAccessToken();
            if (token == null || token.isEmpty()) {
                return;
            }

            // Check if the app was closed from work activity
            if (mElitexData.getTimePassed() != 0) {

                // Check how many days have passed since the task was started
                // this is necessary because the server automatically closes all work tasks in the evening
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
                Date previousDate = df.parse(mElitexData.getOperationAndBatch().mStartDate);
                Calendar cal = Calendar.getInstance();
                Date currentDate = df.parse(df.format(cal.getTime()));
                long diff = previousDate.getTime() - currentDate.getTime();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                if (days < 1) {
                    Intent intent = new Intent(this, WorkActivity.class);
                    intent.putExtra(getString(R.string.key_time), mElitexData.getTimePassed());
                    startActivity(intent);
                }
            }
        } catch (ParseException e) {
            Log.d(LOG_TAG, e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                new ExitDialog(this, this, false).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Initializes the server user login
     */
    private void loginUser() {
        if (checkUserInput()) {
            String userName = mUserEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            Intent intent = new Intent(this, ServerConnectionService.class);
            intent.putExtra(getString(R.string.key_listener), mResultReceiver);
            intent.putExtra(getString(R.string.key_request), ServerRequests.LOGIN_USER);
            intent.putExtra(getString(R.string.key_user_name), userName);
            intent.putExtra(getString(R.string.key_password), password);
            startService(intent);
            mLoading.show();
        }
    }

    /**
     * Initializes the server token login
     */
    private void loginToken(String token) {
        new ElitexData(this).setAccessToken(token);
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_request), ServerRequests.LOGIN_TOKEN);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_token), token);
        startService(intent);
        if (!mLoading.isShowing()) mLoading.show();
    }

    /**
     * Checks if the user name and password input is not empty
     */
    private boolean checkUserInput() {
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
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(LOG_TAG, "QR scan failed");
            } else {
                loginToken(result.getContents());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void requestReady(String result) {
        mLoading.dismiss(); // Remove loading dialog
        try {
            JSONObject json = new JSONObject(result);
            if (json.has(getString(R.string.key_token))) {
                // Retrieving token and initializing token login
                String token = json.getString(getString(R.string.key_token));
                loginToken(token);

            } else if (json.has(getString(R.string.key_name))) {
                saveUserData(json); // Retrieving user information

                // Start proceed to next activity
                Intent intent = new Intent(this, LoadActivity.class);
                startActivity(intent);

            } else if (json.has(getString(R.string.key_error))) {
                // There was a problem with the request get and show massage
                JSONArray errorMassages = json.getJSONArray(getString(R.string.key_error));
                mMassageDialog.setMassageText(errorMassages.get(0).toString());
                mMassageDialog.show();

            } else {
                // The server has returned unknown
                throw new JSONException("Unknown server result");
            }
        } catch (JSONException e) {
            mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
            mMassageDialog.show();
            // The server has returned unknown result log it
            Log.d(LOG_TAG, e.toString());
        }
    }

    @Override
    public void requestFailed() {
        mLoading.dismiss();
        mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
        mMassageDialog.show();
    }

    /**
     * Retrieves user data from result json and save it in Elitex SharedPreferences
     *
     * @param json Result json from request
     * @throws JSONException If there is a problem with the json file it is delegated to calling method
     */
    private void saveUserData(JSONObject json) throws JSONException {
        JSONArray roles = json.getJSONArray(getString(R.string.key_roles));
        Set<String> userRoles = new HashSet<>();
        for (int i = 0; i < roles.length(); i++) {
            userRoles.add(roles.getString(i));
        }

        JSONObject department = json.getJSONObject(getString(R.string.key_department));

        User user = new User(
                json.getInt(getString(R.string.key_id)),
                json.getString(getString(R.string.key_name)),
                department.getInt(getString(R.string.key_department_id)),
                department.getString(getString(R.string.key_department_name)),
                department.getString(getString(R.string.key_department_kind)),
                userRoles,
                mKeepLogged.isChecked()
        );

        new ElitexData(this).addUserData(user);
    }

    @Override
    public void exitApp() {
        finishAffinity();
    }

    @Override
    public void logout() {
        // DO NOTHING this is the login screen
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }
}
