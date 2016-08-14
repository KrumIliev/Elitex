package tma.elitex.reference.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;

import tma.elitex.R;

/**
 * Dialog for selecting month / dat / year
 *
 * Created by Krum Iliev.
 */
public class SelectDateDialog extends Dialog implements View.OnClickListener{

    private DatePicker mDatePicker;
    private DateListener mDateListener;

    public SelectDateDialog(Context context, DateListener listener) {
        super(context);
        this.mDateListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_date_select);

        mDatePicker = (DatePicker) findViewById(R.id.ref_date_picker);
        findViewById(R.id.ref_date_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // There is only one button on this view so no need for check.
        // Return selected date and close dialog
        mDateListener.onDateSet(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        dismiss();
    }
}
