package tma.elitex.utils;

import org.json.JSONArray;

/**
 * Created by Krum Iliev
 */
public class Batch {

    // Batch variables
    public int mBatchId;
    public int mBatchNumber;
    public String mFeatures;
    public String mColour;
    public int mTotalPieces;
    public int mMade;
    public int mRemaining;
    public String mSize;

    // Additional information
    public int mWorkId; // id of the work process to update process state
    public int mNeededPieces; // how many pieces are needed to complete this operation
    public String mStartDate; // Date string when work was started in format yyyy-MM-dd'T'HH:mm:ss.SSSZ

    public Batch(int batchId, int batchNumber, String features, String colour, int totalPieces, int made, int remaining, String size) {
        this.mBatchId = batchId;
        this.mBatchNumber = batchNumber;
        this.mFeatures = features;
        this.mColour = colour;
        this.mTotalPieces = totalPieces;
        this.mMade = made;
        this.mRemaining = remaining;
        this.mSize = size;
    }

    public void setWorkData (int workID, int neededPieces, String startDate) {
        this.mWorkId = workID;
        this.mNeededPieces = neededPieces;
        this.mStartDate = startDate;
    }
}
