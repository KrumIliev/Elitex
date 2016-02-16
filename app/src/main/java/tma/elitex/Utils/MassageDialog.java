package tma.elitex.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import tma.elitex.R;

/**
 * Dialog for displaying massages
 *
 * Created by Krum Iliev.
 */
public class MassageDialog extends Dialog implements View.OnClickListener{

    private TextView mMassage;
    private String mMassageText;

    public MassageDialog(Context context) {
        super(context);
    }

    public MassageDialog(Context context, String massage) {
        super(context);
        mMassageText = massage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_massage);

        mMassage = (TextView) findViewById(R.id.dialog_massage_text);
        findViewById(R.id.dialog_massage_button).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMassage.setText(mMassageText);
    }

    /**
     * Sets the dialog massage
     */
    public void setMassageText (String massage){
        mMassageText = massage;
    }

    @Override
    public void onClick(View v) {
       dismiss();
    }
}
