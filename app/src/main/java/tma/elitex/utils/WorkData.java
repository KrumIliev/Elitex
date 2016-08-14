package tma.elitex.utils;

import org.json.JSONArray;

/**
 * Created by Krum Iliev.
 */
public class WorkData {

    public Batch batch;
    public JSONArray operationIDs;
    public JSONArray workIDs;
    public String workTitle;
    public String startDate;
    public boolean isSeparate;

    public WorkData(Batch batch, JSONArray operationIDs, JSONArray workIDs, String workTitle, String startDate, boolean isSeparate) {
        this.batch = batch;
        this.operationIDs = operationIDs;
        this.workIDs = workIDs;
        this.workTitle = workTitle;
        this.startDate = startDate;
        this.isSeparate = isSeparate;
    }
}
