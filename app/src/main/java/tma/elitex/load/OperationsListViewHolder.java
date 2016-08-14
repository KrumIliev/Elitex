package tma.elitex.load;

import android.view.View;
import android.widget.TextView;

import tma.elitex.R;

/**
 * Created by Krum Iliev
 */
public class OperationsListViewHolder {

    public final TextView id;
    public final TextView description;
    public final TextView machine;

    public OperationsListViewHolder(View view) {
        id = (TextView) view.findViewById(R.id.list_operation_id);
        description  = (TextView) view.findViewById(R.id.list_operation_desc);
        machine = (TextView) view.findViewById(R.id.list_operation_machine);
    }
}
