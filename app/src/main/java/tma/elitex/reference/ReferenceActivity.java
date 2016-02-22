package tma.elitex.reference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tma.elitex.LoadActivity;
import tma.elitex.R;
import tma.elitex.SignInActivity;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.ExitDialog;
import tma.elitex.utils.ExitListener;
import tma.elitex.utils.MassageDialog;
import tma.elitex.utils.LoadingDialog;
import tma.elitex.utils.User;

/**
 * Created by Krum Iliev.
 */
public class ReferenceActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener, DateListener, ExitListener {

    private final String LOG_TAG = ReferenceActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private ReferenceListAdapter mAdapter;
    private ArrayList<ReferenceData> mListData;
    private TextView mTotalTitle;
    private TextView mTotalValue;
    private String mDateSelected;

    private LoadingDialog mLoading;
    private MassageDialog mMassageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);

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

        findViewById(R.id.ref_date).setOnClickListener(this); // Select date button
        findViewById(R.id.ref_back).setOnClickListener(this); // Back button returns to load activity

        mLoading = new LoadingDialog(this);
        mMassageDialog = new MassageDialog(this);

        mListData = new ArrayList<>();
        ListView list = (ListView) findViewById(R.id.ref_list);
        mAdapter = new ReferenceListAdapter(this, mListData);
        list.setAdapter(mAdapter);

        mTotalTitle = (TextView) findViewById(R.id.ref_total_title);
        mTotalValue = (TextView) findViewById(R.id.ref_total_value);

        // Show date select on initial start
        new SelectDateDialog(this, this).show();
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
                new ExitDialog(this, this, true).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ref_date:
                new SelectDateDialog(this, this).show();
                break;
            case R.id.ref_back:
                startActivity(new Intent(this, LoadActivity.class));
                break;
        }
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        mDateSelected = formatDate(year, month, day);
        Log.d(LOG_TAG, mDateSelected);

        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.REPORTS);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_report_date), mDateSelected);
        startService(intent);
        mLoading.show();
    }

    /**
     * Creates server url date string in format YYYY-MM-DD
     */
    private String formatDate(int year, int month, int day) {
        String strMonth, strDay;
        month++; // The date picker returns months starting from 0 for some reason. So add 1 to get the right value
        if (month < 10) strMonth = "0" + month;
        else strMonth = String.valueOf(month);
        if (day < 10) strDay = "0" + day;
        else strDay = String.valueOf(day);
        return year + "-" + strMonth + "-" + strDay;
    }

    @Override
    public void requestReady(String result) {
        Log.d(LOG_TAG, result);
        mLoading.dismiss();

        try {
            JSONArray json = new JSONObject(result).getJSONArray(getString(R.string.key_earnings));

            if (json.length() > 0) {

                mListData.clear(); // Remove all previous information

                // Add the data returned from the server
                for (int i = 0; i < json.length(); i++) {
                    JSONObject object = (JSONObject) json.get(i);
                    mListData.add(new ReferenceData(
                            object.getString(getString(R.string.key_model)),
                            object.getString(getString(R.string.key_process)),
                            object.getString(getString(R.string.key_batch)),
                            object.getString(getString(R.string.key_pieces))
                    ));
                }

                setTotal();
                mAdapter.notifyDataSetChanged();

            } else {
                // There are no earnings for this date
                mMassageDialog.setMassageText(getString(R.string.massage_no_reports));
                mMassageDialog.show();
            }


        } catch (JSONException e) {
            mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
            mMassageDialog.show();
            Log.d(LOG_TAG, e.toString());
        }
    }

    /**
     * Sets the total values
     */
    private void setTotal() {
        int finalCount = 0;
        for (int i = 0; i < mListData.size(); i++) {
            finalCount += Integer.valueOf(mListData.get(i).mPieces);
        }

        mDateSelected = mDateSelected.replaceAll("-", "/"); // Formats the date from YYYY-MM-DD to YYYY/MM/DD for consistency
        mTotalTitle.setText(getString(R.string.total) + mDateSelected + " :");
        mTotalValue.setText(String.valueOf(finalCount));
    }

    @Override
    public void requestFailed() {
        mLoading.dismiss();
        mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
        mMassageDialog.show();
    }

    @Override
    public void exitApp() {
        finishAffinity();
    }

    @Override
    public void logout() {
        mElitexData.setAccessToken(""); // Remove token

        // Change automatic login to false so the sign in activity behaves properly
        User user = mElitexData.getUserData();
        user.mKeepLogged = false;
        mElitexData.addUserData(user);

        // Start sign in activity
        startActivity(new Intent(this, SignInActivity.class));
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }
}
