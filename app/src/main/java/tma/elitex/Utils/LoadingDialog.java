package tma.elitex.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import tma.elitex.R;

/**
 * Created by T1000 on 2/4/2016.
 */
public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        setCanceledOnTouchOutside(false); // Prevents dialog closing if the user touches outside of it
    }

    @Override
    public void onBackPressed() {
        // Do nothing, prevents dialog closing if the user presses the back button
    }
}