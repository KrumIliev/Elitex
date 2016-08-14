package tma.elitex.utils;

import org.json.JSONException;
import org.json.JSONObject;

import tma.elitex.R;

/**
 * Created by Krum Iliev
 */
public class Operation {

    public String id;
    public String name;
    public String serialNumber;
    public String alignedTime;
    public boolean isSeparate;
    public String orderId;
    public String orderName;
    public String orderModel;
    public String orderIdentificationNumber;
    public String clientName;
    public String clientModel;
    public String clientIdentificationNumber;
    public String machineId;
    public String machineName;

    public Operation(String id, String name, String serialNumber, String alignedTime, boolean isSeparate,
                     String orderId, String orderName, String orderModel, String orderIdentificationNumber,
                     String clientName, String clientModel, String clientIdentificationNumber,
                     String machineId, String machineName) {

        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.alignedTime = alignedTime;
        this.isSeparate = isSeparate;
        this.orderId = orderId;
        this.orderName = orderName;
        this.orderModel = orderModel;
        this.orderIdentificationNumber = orderIdentificationNumber;
        this.clientName = clientName;
        this.clientModel = clientModel;
        this.clientIdentificationNumber = clientIdentificationNumber;
        this.machineId = machineId;
        this.machineName = machineName;
    }
}
