package tma.elitex.reference;

import android.content.Context;

import tma.elitex.R;

/**
 * Object for holding reference data returned from the server
 *
 * Created by Krum Iliev.
 */
public class ReferenceData {

    public String mModel;
    public String mProcess;
    public String mBatch;
    public String mPieces;

    public ReferenceData(String mModel, String mProcess, String mBatch, String mPieces) {
        this.mModel = mModel;
        this.mProcess = mProcess;
        this.mBatch = mBatch;
        this.mPieces = mPieces;
    }

    public static ReferenceData getTitles(Context context) {
        return new ReferenceData(
                context.getString(R.string.title_model),
                context.getString(R.string.title_operation),
                context.getString(R.string.title_batch),
                context.getString(R.string.title_pieces));
    }
}
