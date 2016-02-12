package tma.elitex.utils;

/**
 * Created by Krum Iliev.
 */
public class OperationAndBatch {

    // Operation variables
    public int mOperationId;
    public String mName;
    public int mSerialNumber;
    public float mAlignedTime;
    public int mOrderId;
    public String mOrderName;
    public int mMachineId;
    public String mMachineName;

    // Batch variables
    public int mBatchId;
    public int mBatchNumber;
    public String mFeatures;
    public int mDistributionId;
    public String mColour;
    public int mBatchCount;

    public boolean mHasBatch = false;

    public OperationAndBatch(int operationId, String name, int serialNum, double time, int orderId, String orderName, int machineId, String machineName) {
        setOperation(operationId, name, serialNum, time, orderId, orderName, machineId, machineName);
    }

    /**
     * Sets the operation data
     */
    public void setOperation (int operationId, String name, int serialNum, double time, int orderId, String orderName, int machineId, String machineName) {
        this.mOperationId = operationId;
        this.mName = name;
        this.mSerialNumber = serialNum;
        this.mAlignedTime = (float) time;
        this.mOrderId = orderId;
        this.mOrderName = orderName;
        this.mMachineId = machineId;
        this.mMachineName = machineName;
    }

    /**
     * Sets the batch data
     */
    public void setBatch (int batchId, int batchNumber, String features, int distId, String colour, int batchCount) {
        this.mBatchId = batchId;
        this.mBatchNumber = batchNumber;
        this.mFeatures = features;
        this.mDistributionId = distId;
        this.mColour = colour;
        this.mBatchCount = batchCount;

        mHasBatch = true;
    }

    /**
     * Resets all the object data
     */
    public void reset () {
        this.mOperationId = 0;
        this.mName = null;
        this.mSerialNumber = 0;
        this.mAlignedTime = 0;
        this.mOrderId = 0;
        this.mOrderName = null;
        this.mMachineId = 0;
        this.mMachineName = null;
        resetBatch();
    }

    /**
     * Resets the batch data
     */
    public void resetBatch () {
        this.mBatchId = 0;
        this.mBatchNumber = 0;
        this.mFeatures = null;
        this.mDistributionId = 0;
        this.mColour = null;
        this.mBatchCount = 0;

        mHasBatch = false;
    }
}
