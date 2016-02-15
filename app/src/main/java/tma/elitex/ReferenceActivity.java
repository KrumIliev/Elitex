package tma.elitex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

import tma.elitex.server.ServerResultListener;

/**
 * Created by Krum Iliev.
 */
public class ReferenceActivity extends AppCompatActivity implements View.OnClickListener, ServerResultListener {

    private DatePicker mDayPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);

        findViewById(R.id.ref_date).setOnClickListener(this);
        findViewById(R.id.ref_back).setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        mDayPicker = (DatePicker) findViewById(R.id.ref_date_picker);
        mDayPicker.updateDate(year, month, day);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ref_date:
                break;
            case R.id.ref_back:
                break;
        }
    }

    @Override
    public void requestReady(String result) {

    }

    @Override
    public void requestFailed(String error) {

    }
}
