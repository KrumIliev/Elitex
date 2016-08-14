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

import org.json.JSONArray;

import java.util.concurrent.TimeUnit;

import tma.elitex.load.LoadWorkActivity;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.LoadingDialog;
import tma.elitex.utils.MassageDialog;
import tma.elitex.utils.Utils;
import tma.elitex.utils.WorkData;

/**
 * Created by Krum Iliev.
 */
public class WorkActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener {

    private final String LOG_TAG = WorkActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private WorkData mWorkData;

    private TextView mTimerText;
    private Handler mTimerHandler;
    private Runnable mTimerRunnable;
    private long mStartTime = 0;
    private long mTimeElapsed = 0;

    private Button mPause;
    private Button mFinnish;
    private Button mBack;
    private Button mContinue;
    private Button mStop;
    private Button mConfirm;
    private EditText mWorkCount;

    private LoadingDialog mLoading;
    private MassageDialog mMassageDialog;

    private boolean mPausing = false;
    private boolean mResuming = false;
    private boolean mCompletingWork = false;
    private boolean mCompleteAfterResume = false;

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
        Utils.setupActionBar(this, mElitexData.getActionBarTitle(), getSupportActionBar());

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

                if (second == 30) {
                    mElitexData.saveWorkTime(millis); // Save the time every 30th second
                }

                mTimerText.setText(String.format("%02d : %02d : %02d", hour, minute, second));

                mTimerHandler.postDelayed(this, 500);
            }
        };
        startWorkProcess();
    }

    private void initViews() {
        mWorkData = mElitexData.getWorkData();

        ((TextView) findViewById(R.id.work_operation)).setText(mWorkData.workTitle);
        ((TextView) findViewById(R.id.work_batch_number)).setText(String.valueOf(mWorkData.batch.mBatchNumber));
        ((TextView) findViewById(R.id.work_count)).setText(String.valueOf(mWorkData.batch.mRemaining));
        ((TextView) findViewById(R.id.work_size)).setText(mWorkData.batch.mSize);
        ((TextView) findViewById(R.id.work_colour)).setText(mWorkData.batch.mColour);

        String operations = mWorkData.operationIDs.toString();
        operations = operations.replace("[", "");
        operations = operations.replace("]", "");
        operations = operations.replace("\"", "");
        ((TextView) findViewById(R.id.work_operation_ids)).setText(operations);

        mTimerText = (TextView) findViewById(R.id.work_timer);
        mPause = (Button) findViewById(R.id.work_button_pause);
        mFinnish = (Button) findViewById(R.id.work_button_finnish);
        mBack = (Button) findViewById(R.id.work_button_back);
        mContinue = (Button) findViewById(R.id.work_button_resume);
        mStop = (Button) findViewById(R.id.work_button_stop);
        mConfirm = (Button) findViewById(R.id.work_button_confirm);
        mWorkCount = (EditText) findViewById(R.id.work_confirm);

        mPause.setOnClickListener(this);
        mFinnish.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mContinue.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mConfirm.setOnClickListener(this);

        mLoading = new LoadingDialog(this);
        mMassageDialog = new MassageDialog(this);

        if (mWorkData.isSeparate) {
            mFinnish.setEnabled(false);
            mFinnish.setBackgroundResource(R.drawable.button_gray);
            mFinnish.setPadding(20, 20, 20, 20);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.work_button_pause:
                sendPauseWorkRequest();
                break;
            case R.id.work_button_resume:
                sendResumeWorkRequest();
                break;
            case R.id.work_button_finnish:
                sendStopWorkRequest();
                break;
            case R.id.work_button_back:
                sendResumeWorkRequest();
                break;
            case R.id.work_button_stop:
                stopWorkProcess();
                break;
            case R.id.work_button_confirm:
                mCompleteAfterResume = true;
                sendResumeWorkRequest();
                break;
        }
    }

    private void sendPauseWorkRequest() {
        mLoading.show();
        mPausing = true;
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_work_ids), mWorkData.workIDs.toString());
        intent.putExtra(getString(R.string.key_request), ServerRequests.PAUSE_WORK);
        startService(intent);
    }

    private void sendResumeWorkRequest() {
        mLoading.show();
        if (!mCompleteAfterResume) mResuming = true;
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_work_ids), mWorkData.workIDs.toString());
        intent.putExtra(getString(R.string.key_request), ServerRequests.RESUME_WORK);
        startService(intent);
    }

    private void sendStopWorkRequest() {
        mLoading.show();
        mCompletingWork = true;
        String data = mWorkCount.getText().toString();
        int pieces = data.isEmpty() ? 0 : Integer.valueOf(data);
        long time = TimeUnit.MILLISECONDS.toMinutes(mElitexData.getTimePassed());
        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_time_worked), time);
        intent.putExtra(getString(R.string.key_pieces), pieces);
        intent.putExtra(getString(R.string.key_work_ids), mWorkData.workIDs.toString());
        intent.putExtra(getString(R.string.key_request), ServerRequests.COMPLETE_WORK);
        startService(intent);
    }

    @Override
    public void requestReady(String result, String serverTime) {
        Log.d(LOG_TAG, result);
        mLoading.dismiss(); // Remove loading dialog

        // !!! The work screen does not expect any response data from the API if the response is positive
        // !!! everything is OK

        if (mPausing) {
            pauseWorkProcess();
        }

        if (mResuming) {
            startWorkProcess();
        }

        if (mCompletingWork) {
            mCompletingWork = false;
            mTimerHandler.removeCallbacks(mTimerRunnable);
            mElitexData.saveWorkTime(0);
            Intent intent = new Intent(this, LoadWorkActivity.class);
            startActivity(intent);
        }

        if (mCompleteAfterResume) {
            mCompleteAfterResume = false;
            sendStopWorkRequest();
        }
    }


    public void pauseWorkProcess() {
        mPausing = false;

        mTimerHandler.removeCallbacks(mTimerRunnable);
        mTimeElapsed = mTimeElapsed + (System.currentTimeMillis() - mStartTime);

        mPause.setVisibility(View.GONE);
        mFinnish.setVisibility(View.GONE);
        mConfirm.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        mWorkCount.setVisibility(View.GONE);

        mContinue.setVisibility(View.VISIBLE);
        mStop.setVisibility(View.VISIBLE);
    }

    private void stopWorkProcess() {
        mTimerHandler.removeCallbacks(mTimerRunnable);
        mTimeElapsed = mTimeElapsed + (System.currentTimeMillis() - mStartTime);

        mPause.setVisibility(View.GONE);
        mFinnish.setVisibility(View.GONE);
        mContinue.setVisibility(View.GONE);
        mStop.setVisibility(View.GONE);

        mWorkCount.setVisibility(View.VISIBLE);
        mConfirm.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);

        mWorkCount.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mWorkCount, InputMethodManager.SHOW_IMPLICIT);
    }

    private void startWorkProcess() {
        mCompletingWork = false;
        mPausing = false;
        mResuming = false;
        mCompleteAfterResume = false;

        mWorkCount.setText("");
        mWorkCount.setVisibility(View.GONE);
        mStop.setVisibility(View.GONE);
        mContinue.setVisibility(View.GONE);
        mConfirm.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);

        mPause.setVisibility(View.VISIBLE);
        mFinnish.setVisibility(View.VISIBLE);

        if (mWorkData.isSeparate) {
            mFinnish.setEnabled(false);
            mFinnish.setBackgroundResource(R.drawable.button_gray);
            mFinnish.setPadding(20, 20, 20, 20);
        }

        mStartTime = System.currentTimeMillis();
        mTimerHandler.postDelayed(mTimerRunnable, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimerHandler.removeCallbacks(mTimerRunnable);
    }

    @Override
    public void requestFailed() {
        mLoading.dismiss();
        mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
        mMassageDialog.show();
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }
}
