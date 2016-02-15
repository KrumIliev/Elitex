package tma.elitex.reference;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.expirationpicker.ExpirationPickerBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import tma.elitex.LoadActivity;
import tma.elitex.R;
import tma.elitex.server.ServerResultListener;

/**
 * Created by Krum Iliev.
 */
public class ReferenceActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener, DateListener {

    private ListView mList;
    private ArrayList<ReferenceData> testData; // TODO Remove after testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);

        mList = (ListView) findViewById(R.id.ref_list);

        findViewById(R.id.ref_date).setOnClickListener(this); // Select date button
        findViewById(R.id.ref_back).setOnClickListener(this); // Back button returns to load activity

        // TODO remove after testing -------------------------------------------------------------->
        testData = new ArrayList<>();
        testData.add(ReferenceData.getTitles());
        testData.add(new ReferenceData("1279 - панталон Енцо с 3 филетки", "шие гайки на пекир х 5", "5 - Раз.:38 / Цв.:беж - 8 бр.", "8"));
        testData.add(new ReferenceData("1279 - панталон Енцо с 3 филетки", "шие гайки на пекир х 5", "5 - Раз.:38 / Цв.:беж - 8 бр.", "8"));
        testData.add(new ReferenceData("1279 - панталон Енцо с 3 филетки", "шие гайки на пекир х 5", "5 - Раз.:38 / Цв.:беж - 8 бр.", "8"));
        testData.add(new ReferenceData("1279 - панталон Енцо с 3 филетки", "шие гайки на пекир х 5", "5 - Раз.:38 / Цв.:беж - 8 бр.", "8"));

        int finalCount = 0;
        for (int i = 1; i < testData.size(); i++) {
            finalCount += Integer.valueOf(testData.get(i).mPieces);
        }

        testData.add(new ReferenceData("", "", "Общо: ", String.valueOf(finalCount)));
        // <----------------------------------------------------------------------------------------

        ReferenceListAdapter adapter = new ReferenceListAdapter(this, testData);
        mList.setAdapter(adapter);
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
        Toast.makeText(this, new Date(year, month, day).toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void requestReady(String result) {

    }

    @Override
    public void requestFailed(String error) {

    }
}
