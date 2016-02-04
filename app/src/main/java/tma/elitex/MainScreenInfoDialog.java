package tma.elitex;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import tma.elitex.Utils.OperationAndBatch;

/**
 * Created by Krum Iliev.
 */
public class MainScreenInfoDialog extends Dialog implements View.OnClickListener{

    private OperationAndBatch mData;
    private MainScreenListener mListener;

    private TextView mModel;
    private TextView mOperation;
    private TextView mMachine;

    public MainScreenInfoDialog(Context context, MainScreenListener listener) {
        super(context);
        this.mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main_screen_info);
        setCanceledOnTouchOutside(false); // Prevents dialog closing if the user touches outside of it

        mModel = (TextView) findViewById(R.id.info_model);
        mOperation = (TextView) findViewById(R.id.info_operation);
        mMachine = (TextView) findViewById(R.id.info_machine);

        findViewById(R.id.info_batch_button).setOnClickListener(this);
        findViewById(R.id.info_cancel_button).setOnClickListener(this);

        initInfo();
    }

    public void setData (OperationAndBatch data) {
        this.mData = data;
        initInfo();
    }

    private void initInfo () {
        mModel.setText(mData.mOrderName);
        mOperation.setText(mData.mName);
        mMachine.setText(mData.mMachineName);

        if (mData.mHasBatch) {
            //TODO init batch
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_batch_button:
                mListener.continueLoad();
                break;
            case R.id.info_cancel_button:
                mListener.restartLoad();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mListener.restartLoad();
        super.onBackPressed();
    }
}
