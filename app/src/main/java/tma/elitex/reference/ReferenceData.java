package tma.elitex.reference;

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
}
