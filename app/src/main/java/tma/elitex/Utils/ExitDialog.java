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
    private boolean mShowChangeUser;

    public ExitDialog(Context context, ExitListener listener, boolean showChangeUser) {
        super(context);
        this.mListener = listener;
        mShowChangeUser = showChangeUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_exit);

        findViewById(R.id.exit_yes).setOnClickListener(this); // Button yes
        findViewById(R.id.exit_no).setOnClickListener(this); // Button no

        if (mShowChangeUser) { // Logout button
            View logout = findViewById(R.id.exit_logout);
            logout.setVisibility(View.VISIBLE);
            logout.setOnClickListener(this);
        }
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
