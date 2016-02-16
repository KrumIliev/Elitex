package tma.elitex.reference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tma.elitex.LoadActivity;
import tma.elitex.R;
import tma.elitex.server.ServerConnectionService;
import tma.elitex.server.ServerRequests;
import tma.elitex.server.ServerResultListener;
import tma.elitex.server.ServerResultReceiver;
import tma.elitex.utils.ElitexData;
import tma.elitex.utils.MassageDialog;
import tma.elitex.utils.LoadingDialog;

/**
 * Created by Krum Iliev.
 */
public class ReferenceActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener, DateListener {

    private final String LOG_TAG = ReferenceActivity.class.getSimpleName();

    private ServerResultReceiver mResultReceiver; // Server service communication
    private ElitexData mElitexData; // Stored data access

    private ReferenceListAdapter mAdapter;
    private ArrayList<ReferenceData> mListData;

    private LoadingDialog mLoading;
    private MassageDialog mMassageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);

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

        // Show date select on initial start
        new SelectDateDialog(this, this).show();
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
        // Creates server url date string in format YYYY-MM-DD
        String date = year + "-" + month + "-" + day;

        Intent intent = new Intent(this, ServerConnectionService.class);
        intent.putExtra(getString(R.string.key_listener), mResultReceiver);
        intent.putExtra(getString(R.string.key_request), ServerRequests.REPORTS);
        intent.putExtra(getString(R.string.key_token), mElitexData.getAccessToken());
        intent.putExtra(getString(R.string.key_report_date), date);
        startService(intent);
        mLoading.show();
    }

    @Override
    public void requestReady(String result) {
        Log.d(LOG_TAG, result);
        mLoading.dismiss();

        try {
            JSONArray json = new JSONObject(result).getJSONArray(getString(R.string.key_earnings));

            if (json.length() > 0) {

                mListData.clear(); // Remove all previous information
                mListData.add(ReferenceData.getTitles(this)); // Add titles this needs to go first before adding data

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

                int finalCount = 0;
                for (int i = 1; i < mListData.size(); i++) {
                    finalCount += Integer.valueOf(mListData.get(i).mPieces);
                }

                mListData.add(new ReferenceData("", "", getString(R.string.total), String.valueOf(finalCount)));
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

    @Override
    public void requestFailed(String error) {
        Log.d(LOG_TAG, error);
        mLoading.dismiss();
        mMassageDialog.setMassageText(getString(R.string.massage_server_failed));
        mMassageDialog.show();
    }
}
