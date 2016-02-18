package tma.elitex.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import tma.elitex.R;

/**
 * Created by Krum Iliev.
 */
public class ExitDialog extends Dialog implements View.OnClickListener {

    private ExitListener mListener;

    public ExitDialog(Context context, ExitListener listener) {
        super(context);
        this.mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_exit);

        findViewById(R.id.exit_yes).setOnClickListener(this); // Button yes
        findViewById(R.id.exit_no).setOnClickListener(this); // Button no
        findViewById(R.id.exit_logout).setOnClickListener(this); // Logout button
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit_yes:
                mListener.exitApp();
                dismiss();
                break;
            case R.id.exit_no:
                dismiss();
                break;
            case R.id.exit_logout:
                mListener.logout();
                dismiss();
                break;
        }
    }
}
