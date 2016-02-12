package tma.elitex.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import tma.elitex.R;

/**
 * Created by Krum Iliev.
 */
public class ErrorDialog extends Dialog implements View.OnClickListener{

    private String mMassageText;

    public ErrorDialog(Context context, String massage) {
        super(context);
        this.mMassageText = massage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_error);


        TextView massage = (TextView) findViewById(R.id.dialog_error_text);
        massage.setText(mMassageText);
        findViewById(R.id.dialog_error_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       dismiss();
    }
}
