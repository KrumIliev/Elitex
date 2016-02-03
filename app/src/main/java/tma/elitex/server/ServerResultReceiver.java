package tma.elitex.server;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

/**
 * Created by Krum Iliev on 2/3/2016.
 */
@SuppressLint("ParcelCreator")
public class ServerResultReceiver extends ResultReceiver {

    private final String LOG_TAG = ServerResultReceiver.class.getSimpleName();

    // Result codes
    public static final int RESULT_FAIL = 0;
    public static final int RESULT_OK = 1;

    // Result date keys
    public static final String KEY_RESULT = "result";
    public static final String KEY_ERROR = "error";

    // Listener for communicating with the calling activity / fragment
    private ServerResultListener mListener;

    public ServerResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     * Sets the listener for communicating with the calling activity / fragment
     *
     * @param listener ServerResultListener implemented in the caller code
     */
    public void serListener (ServerResultListener listener) {
        this.mListener = listener;
    }

    /**
     * Receives the result from IntentService and sends it to the calling activity / fragment
     *
     * @param resultCode see result codes in ServerResultReceiver.class
     * @param resultData see result data keys in ServerResultReceiver.class
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mListener != null) {
            if (resultCode == RESULT_OK) {
                // Pass the result to the calling activity / fragment
                mListener.requestReady(resultData.getString(KEY_RESULT));

            } else if (resultCode == RESULT_FAIL) {
                // There was a error log massage
                Log.d(LOG_TAG, resultData.getString(KEY_ERROR));
            } else {
                // There was a error log massage
                Log.d(LOG_TAG, "Unknown result code: " + resultCode);
            }
        }
    }
}
