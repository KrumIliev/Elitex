package tma.elitex.Utils;

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

    public OperationAndBatch(int operationId, String name, int serialNum, float time, int orderId, String orderName, int machineId, String machineName) {
        this.mOperationId = operationId;
        this.mName = name;
        this.mSerialNumber = serialNum;
        this.mAlignedTime = time;
        this.mOrderId = orderId;
        this.mOrderName = orderName;
        this.mMachineId = machineId;
        this.mMachineName = machineName;
    }

    public void setBatch (int batchId, int batchNumber, String features, int distId, String colour, int batchCount) {
        this.mBatchId = batchId;
        this.mBatchNumber = batchNumber;
        this.mFeatures = features;
        this.mDistributionId = distId;
        this.mColour = colour;
        this.mBatchCount = batchCount;

        mHasBatch = true;
    }
}
