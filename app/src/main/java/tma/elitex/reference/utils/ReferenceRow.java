package tma.elitex.reference.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tma.elitex.R;

/**
 * Created by Krum Iliev
 */
public class ReferenceRow {

    public static View getRowView(Context context, ReferenceOrder data) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.reference_top, null);

        TextView header = (TextView) rowView.findViewById(R.id.header);
        header.setText(String.format("%s - %s", data.model, data.name));

        LinearLayout dataView = (LinearLayout) rowView.findViewById(R.id.data_container);

        for (ReferenceOperation operation : data.operations) {

            dataView.addView(createProcessView(inflater, context, operation));

            View footer = inflater.inflate(R.layout.reference_footer, null);
            TextView count = (TextView) footer.findViewById(R.id.total);
            count.setText(String.valueOf(operation.total));
            dataView.addView(footer);
        }

        return rowView;
    }

    private static LinearLayout createProcessView(LayoutInflater inflater, Context context, ReferenceOperation operation) {
        LinearLayout dataLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dataLayout.setLayoutParams(params);
        dataLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.reference_data_row, null);
        TextView operationView = (TextView) layout.findViewById(R.id.operation);
        operationView.setText(operation.name);
        TextView batchView = (TextView) layout.findViewById(R.id.batch);
        batchView.setText(operation.batches.get(0).name);
        TextView countView = (TextView) layout.findViewById(R.id.count);
        countView.setText(String.valueOf(operation.batches.get(0).count));
        dataLayout.addView(layout);

        for (int i = 1; i < operation.batches.size(); i++) {
            LinearLayout batchLayout = (LinearLayout) inflater.inflate(R.layout.reference_data_row, null);
            ((TextView) batchLayout.findViewById(R.id.batch)).setText(operation.batches.get(i).name);
            ((TextView) batchLayout.findViewById(R.id.count)).setText(String.valueOf(operation.batches.get(i).count));
            dataLayout.addView(batchLayout);
        }

        return dataLayout;
    }
}
