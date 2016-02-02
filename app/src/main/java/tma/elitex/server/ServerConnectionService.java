package tma.elitex.server;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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

    private ServerListener mServerListener;

    public ServerConnectionService() {
        super(ServerConnectionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Initialize the server listener that communicates with the colling activity
        mServerListener = (ServerListener) intent.getSerializableExtra(getString(R.string.key_listener));

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
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            // Add request headers
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty(getString(R.string.server_content_type), getString(R.string.server_content_type_value));
            urlConnection.setRequestProperty(getString(R.string.server_accept), getString(R.string.server_accept_value));
            if (token != null && !token.isEmpty()) {
                urlConnection.setRequestProperty(getString(R.string.server_authorization), getString(R.string.key_token) + "=" + token);
            }

            // Add request body
            outputStream = urlConnection.getOutputStream();
            outputStream.write(body.getBytes("UTF-8"));
            outputStream.flush();

            // Read response
            inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) throw new RuntimeException("Input Stream null");
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) buffer.append(line);
            String result = buffer.toString();
            Log.d(LOG_TAG, result);
            mServerListener.requestReady(result);

        } catch (Exception e) {
            mServerListener.requestFailed(e);
            Log.d(LOG_TAG, e.toString());
        } finally {
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
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            // Add request headers
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty(getString(R.string.server_content_type), getString(R.string.server_content_type_value));
            urlConnection.setRequestProperty(getString(R.string.server_accept), getString(R.string.server_accept_value));
            urlConnection.setRequestProperty(getString(R.string.server_authorization), getString(R.string.key_token) + "=" + token);

            // Read response
            inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) throw new RuntimeException("Input Stream null");
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) buffer.append(line);
            String result = buffer.toString();
            Log.d(LOG_TAG, result);
            mServerListener.requestReady(result);

        } catch (Exception e) {
            mServerListener.requestFailed(e);
            Log.d(LOG_TAG, e.toString());
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
