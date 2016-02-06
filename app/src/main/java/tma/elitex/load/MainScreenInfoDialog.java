package tma.elitex.load;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import tma.elitex.R;
import tma.elitex.utils.OperationAndBatch;

/**
 * Created by Krum Iliev.
 */
public class MainScreenInfoDialog extends Dialog implements View.OnClickListener{

    private MainScreenListener mListener;

    private String mMassageText;
    private String mButtonText;

    public MainScreenInfoDialog(Context context, MainScreenListener listener, String massage, String buttonText) {
        super(context);
        this.mListener = listener;
        this.mMassageText = massage;
        this.mButtonText = buttonText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main_screen_info);
        setCanceledOnTouchOutside(false); // Prevents dialog closing if the user touches outside of it


        TextView massage = (TextView) findViewById(R.id.dialog_load_text);
        massage.setText(mMassageText);
        Button button = (Button) findViewById(R.id.dialog_load_button);
        button.setText(mButtonText);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       mListener.startLoading();
    }

    @Override
    public void onBackPressed() {
        // Do nothing, prevents dialog closing if the user presses the back button
    }
}
