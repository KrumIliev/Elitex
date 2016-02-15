package tma.elitex.utils;

/**
 * Created by Krum Iliev.
 */
public class OperationAndBatch {

    // Operation variables
    public String mOperationId;
    public String mOperationName;
    public int mSerialNumber;
    public float mAlignedTime;
    public int mOrderId;
    public String mModelName;
    public int mMachineId;
    public String mMachineName;

    // Batch variables
    public int mBatchId;
    public int mBatchNumber;
    public String mFeatures;
    public String mColour;
    public int mBatchCount;
    public int mMade;
    public int mRemaining;
    public String mSize;

    public boolean mHasBatch = false;

    public OperationAndBatch(String operationId, String name, int serialNum, double time, int orderId, String orderName, int machineId, String machineName) {
        setOperation(operationId, name, serialNum, time, orderId, orderName, machineId, machineName);
    }

    /**
     * Sets the operation data
     */
    public void setOperation (String operationId, String name, int serialNum, double time, int orderId, String orderName, int machineId, String machineName) {
        this.mOperationId = operationId;
        this.mOperationName = name;
        this.mSerialNumber = serialNum;
        this.mAlignedTime = (float) time;
        this.mOrderId = orderId;
        this.mModelName = orderName;
        this.mMachineId = machineId;
        this.mMachineName = machineName;
    }

    /**
     * Sets the batch data
     */
    public void setBatch (int batchId, int batchNumber, String features, String colour, int batchCount, int made, int remaining, String size) {
        this.mBatchId = batchId;
        this.mBatchNumber = batchNumber;
        this.mFeatures = features;
        this.mColour = colour;
        this.mBatchCount = batchCount;
        this.mMade = made;
        this.mRemaining = remaining;
        this.mSize = size;

        mHasBatch = true;
    }

    /**
     * Resets all the object data
     */
    public void reset () {
        this.mOperationId = null;
        this.mOperationName = null;
        this.mSerialNumber = 0;
        this.mAlignedTime = 0;
        this.mOrderId = 0;
        this.mModelName = null;
        this.mMachineId = 0;
        this.mMachineName = null;
        resetBatch();
    }

    /**
     * Resets the batch data
     */
    public void resetBatch () {
        this.mBatchId = 0;
        mBatchNumber = 0;
        this.mFeatures = null;
        this.mColour = null;
        this.mBatchCount = 0;
        this.mMade = 0;
        this.mRemaining = 0;
        this.mSize = null;

        mHasBatch = false;
    }
}
