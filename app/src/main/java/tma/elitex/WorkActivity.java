package tma.elitex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.MassageDialog;
import tma.elitex.utils.FeaturesDialog;
import tma.elitex.utils.OperationAndBatch;

/**
 * Created by Krum Iliev.
 */
public class WorkActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener {

    private final String LOG_TAG = WorkActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private TextView mTimerText;
    private Handler mTimerHandler;
    private Runnable mTimerRunnable;
    private long mStartTime = 0;
    private long mTimeElapsed = 0;
    private boolean mTimerIsRunning = false;

    private Button mPause;
    private Button mFinnish;
    private EditText mWorkCount;
    private LinearLayout mConfirmContainer;
    private FeaturesDialog mFeaturesDialog;

    // This is used to ensure correct back button functionality if the user wants to continue working
    // after he has pressed the finnish button
    private boolean mCanConfirm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

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
        getSupportActionBar().setTitle(mElitexData.getActionBarTitle());

        initViews();

        // Checks if there is elapsed time passed via intent. This only happens if the application was
        // closed without stopping the work process and the login screen is trying to restart the task
        long time = getIntent().getLongExtra(getString(R.string.key_time), 0);
        if (time > 0) {
            mTimeElapsed = time;
        }

        mTimerHandler = new Handler();
        mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - mStartTime;
                millis += mTimeElapsed;

                long second = (millis / 1000) % 60;
                long minute = (millis / (1000 * 60)) % 60;
                long hour = (millis / (1000 * 60 * 60)) % 24;

                mTimerText.setText(String.format("%02d : %02d : %02d", hour, minute, second));

                mTimerHandler.postDelayed(this, 500);
            }
        };
    }

    private void initViews() {
        OperationAndBatch operationAndBatch = mElitexData.getOperationAndBatch();

        ((TextView) findViewById(R.id.work_operation)).setText(operationAndBatch.mOperationName);
        ((TextView) findViewById(R.id.work_model)).setText(operationAndBatch.mModelName);
        ((TextView) findViewById(R.id.work_machine)).setText(getString(R.string.title_machine) + " " + operationAndBatch.mMachineName);
        ((TextView) findViewById(R.id.work_batch_number)).setText(getString(R.string.title_batch_number) + " " + operationAndBatch.mBatchNumber);
        ((TextView) findViewById(R.id.work_count)).setText(getString(R.string.title_batch_count) + " " + operationAndBatch.mBatchCount);
        ((TextView) findViewById(R.id.work_size)).setText(getString(R.string.title_batch_size) + " " + operationAndBatch.mSize);
        ((TextView) findViewById(R.id.work_colour)).setText(getString(R.string.title_batch_colour) + " " + operationAndBatch.mColour);
        ((TextView) findViewById(R.id.work_features)).setText(operationAndBatch.mFeatures);

        mTimerText = (TextView) findViewById(R.id.work_timer);
        mPause = (Button) findViewById(R.id.work_button_pause);
        mFinnish = (Button) findViewById(R.id.work_button_finnish);
        mWorkCount = (EditText) findViewById(R.id.work_confirm);
        mConfirmContainer = (LinearLayout) findViewById(R.id.work_confirm_container);

        findViewById(R.id.work_features).setOnClickListener(this);
        mPause.setOnClickListener(this);
        mFinnish.setOnClickListener(this);

        mFeaturesDialog = new FeaturesDialog(this, operationAndBatch.mFeatures);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setTimerState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTimerState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.work_features:
                mFeaturesDialog.show();
                break;
            case R.id.work_button_pause:
                setTimerState();
                break;
            case R.id.work_button_finnish:
                finishWork();
                break;
        }
    }

    private void setTimerState () {
        if (mTimerIsRunning) {
            mTimerIsRunning = false;
            mTimerHandler.removeCallbacks(mTimerRunnable);
            mPause.setText(getString(R.string.button_continue));
            mFinnish.setVisibility(View.GONE);
            mTimeElapsed = mTimeElapsed + (System.currentTimeMillis() - mStartTime);
        } else {
            mTimerIsRunning = true;
            mCanConfirm = false;
            mConfirmContainer.setVisibility(View.GONE);
            mTimerText.setVisibility(View.VISIBLE);
            mStartTime = System.currentTimeMillis();
            mTimerHandler.postDelayed(mTimerRunnable, 0);
            mPause.setText(getString(R.string.button_pause));
            mFinnish.setVisibility(View.VISIBLE);
            mFinnish.setText(getString(R.string.button_finnish));
        }
    }

    private void finishWork () {
        if (mCanConfirm) {
            sendWorkDataAndFinnish();
        } else {
            mTimerIsRunning = false;
            mTimerHandler.removeCallbacks(mTimerRunnable);
            mTimeElapsed = mTimeElapsed + (System.currentTimeMillis() - mStartTime);
            mPause.setText(getString(R.string.button_back));
            mFinnish.setText(getString(R.string.button_confirm));
            mConfirmContainer.setVisibility(View.VISIBLE);
            mTimerText.setVisibility(View.GONE);
            mCanConfirm = true;
            mWorkCount.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mWorkCount, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void sendWorkDataAndFinnish () {
        String data = mWorkCount.getText().toString();
        if (data.isEmpty()) {
            new MassageDialog(this, getString(R.string.massage_count)).show();
            return;
        }

        int count = Integer.valueOf(data);
        Log.d(LOG_TAG, "Final count: " + count);

        Intent intent = new Intent(this, LoadActivity.class);
        startActivity(intent);
    }

    @Override
    public void requestReady(String result) {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }
}
