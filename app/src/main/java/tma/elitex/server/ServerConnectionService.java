package tma.elitex.server;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import tma.elitex.R;

/**
 * This class is used to communicate with the server api.
 * requestWithBody and requestNoBody are the main communication methods.
 * createBody... methods are for creating request bodies.
 *
 * Created by Krum Iliev.
 */
public class ServerConnectionService extends IntentService {

    private String LOG_TAG = ServerConnectionService.class.getSimpleName();

    private ResultReceiver mServerListener;

    public ServerConnectionService() {
        super(ServerConnectionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Initialize the server listener that communicates with the colling activity
        mServerListener = intent.getParcelableExtra(getString(R.string.key_listener));

        String body, method, serverPath, token = null;

        switch ((ServerRequests) intent.getSerializableExtra(getString(R.string.key_request))) {
            case LOGIN_USER:
                // Initiates user login via username and password
                body = createBodyUserLogin(intent);
                method = getString(R.string.server_method_post);
                serverPath = getString(R.string.server_url_login);
                requestWithBody(serverPath, method, token, body);
                break;
            case LOGIN_TOKEN:
                // Initiates user login via access token
                method = getString(R.string.server_method_get);
                serverPath = getString(R.string.server_url_token);
                token = intent.getStringExtra(getString(R.string.key_token));
                requestNoBody(serverPath, method, token);
                break;
            case LOAD_OPERATION:
                method = getString(R.string.server_method_get);
                serverPath = getString(R.string.server_url_operation) + intent.getStringExtra(getString(R.string.key_operation_id));
                token = intent.getStringExtra(getString(R.string.key_token));
                requestNoBody(serverPath, method, token);
                break;
        }
    }

    /**
     * Makes request to the server with body. If access token is null or empty it will
     * not be added to the request.
     *
     * @param serverPath Request path after the server URL (example: api/tma/user)
     * @param method Server request method
     * @param token Access token
     * @param body Request body
     */
    private void requestWithBody (String serverPath, String method, String token, String body) {
        String serverURL = getString(R.string.server_url) + serverPath;

        HttpURLConnection urlConnection = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            // Create the connection
            URL url = new URL(serverURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            // Add request headers
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty(getString(R.string.server_content_type), getString(R.string.server_content_type_value));
            urlConnection.setRequestProperty(getString(R.string.server_accept), getString(R.string.server_accept_value));
            if (token != null && !token.isEmpty()) {
                urlConnection.setRequestProperty(getString(R.string.server_authorization), getString(R.string.key_token) + "=" + token);
            }

            // Add request body
            outputStream = urlConnection.getOutputStream();
            byte[] outputStringBytes = body.getBytes();
            outputStream.write(outputStringBytes);
            outputStream.flush();
            outputStream.close();

            Log.d(LOG_TAG, "Request with no body");
            Log.d(LOG_TAG, "Token: " + token);
            Log.d(LOG_TAG, "Response code: " + urlConnection.getResponseCode());

            // Read response
            if (urlConnection.getResponseCode() == 400) {
                inputStream = urlConnection.getErrorStream();
            } if (urlConnection.getResponseCode() == 404) {
                throw new RuntimeException("Възникна проблем със заявката");
            } else {
                inputStream = urlConnection.getInputStream();
            }

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.d(LOG_TAG, "Input Stream null");
                throw new RuntimeException(getString(R.string.massage_server_failed));
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while ((line = reader.readLine()) != null) buffer.append(line);
            } catch (EOFException e) {
            }

            inputStream.close();
            String result = buffer.toString();
            Log.d(LOG_TAG, result);

            // Return results
            Bundle bundle = new Bundle();
            bundle.putString(ServerResultReceiver.KEY_RESULT, result);
            mServerListener.send(ServerResultReceiver.RESULT_OK, bundle);

        } catch (Exception e) {
            Bundle bundle = new Bundle();
            bundle.putString(ServerResultReceiver.KEY_ERROR, e.toString());
            mServerListener.send(ServerResultReceiver.RESULT_FAIL, bundle);
        } finally {
            // Check if anything is still open and close it.
            try {
                if (urlConnection != null) urlConnection.disconnect();
                if (reader != null) reader.close();
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
            }
        }
    }

    /**
     * Makes request to the server without body.
     *
     * @param serverPath Request path after the server URL (example: api/tma/user)
     * @param method Server request method
     * @param token Access token
     */
    private void requestNoBody (String serverPath, String method, String token) {
        String serverURL = getString(R.string.server_url) + serverPath;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            // Create the connection
            URL url = new URL(serverURL);
            urlConnection = (HttpURLConnection) url.openConnection();

            // Add request headers
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty(getString(R.string.server_content_type), getString(R.string.server_content_type_value));
            urlConnection.setRequestProperty(getString(R.string.server_accept), getString(R.string.server_accept_value));
            urlConnection.setRequestProperty(getString(R.string.server_authorization), getString(R.string.key_token) + "=" + token);

            Log.d(LOG_TAG, "Request with no body");
            Log.d(LOG_TAG, "Token: " + token);
            Log.d(LOG_TAG, "Response code: " + urlConnection.getResponseCode());

            // Read response
            if (urlConnection.getResponseCode() == 400) {
                inputStream = urlConnection.getErrorStream();
            } if (urlConnection.getResponseCode() == 404) {
                throw new RuntimeException("Възникна проблем със заявката");
            } else {
                inputStream = urlConnection.getInputStream();
            }

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.d(LOG_TAG, "Input Stream null");
                throw new RuntimeException(getString(R.string.massage_server_failed));
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            try {
                while ((line = reader.readLine()) != null) buffer.append(line);
            } catch (EOFException e) {
                // Stream has ended unexpectedly do nothing and proceed to reading result
            }

            inputStream.close();
            String result = buffer.toString();
            Log.d(LOG_TAG, result);

            // Return results
            Bundle bundle = new Bundle();
            bundle.putString(ServerResultReceiver.KEY_RESULT, result);
            mServerListener.send(ServerResultReceiver.RESULT_OK, bundle);

        } catch (Exception e) {
            Bundle bundle = new Bundle();
            bundle.putString(ServerResultReceiver.KEY_ERROR, e.toString());
            mServerListener.send(ServerResultReceiver.RESULT_FAIL, bundle);
        } finally {
            try {

                if (urlConnection != null) urlConnection.disconnect();
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
            }
        }
    }

    /**
     * Creates the user login body with user name and password
     *
     * @param intent The service intent
     */
    private String createBodyUserLogin(Intent intent) {
        String user = intent.getStringExtra(getString(R.string.key_user_name));
        String password = intent.getStringExtra(getString(R.string.key_password));

        try {
            JSONObject body = new JSONObject();
            body.put(getString(R.string.key_user_name), user);
            body.put(getString(R.string.key_password), password);

            String result = body.toString();
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
