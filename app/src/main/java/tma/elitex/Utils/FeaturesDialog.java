package tma.elitex.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import tma.elitex.R;

/**
 * Created by Krum Iliev.
 */
public class FeaturesDialog extends Dialog implements View.OnClickListener {

    private String mFeatures;

    public FeaturesDialog(Context context, String features) {
        super(context);
        this.mFeatures = features;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_features);

        ((TextView) findViewById(R.id.dialog_features_text)).setText(mFeatures);
        findViewById(R.id.dialog_features_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
