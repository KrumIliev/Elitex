package tma.elitex.load;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import tma.elitex.R;
import tma.elitex.utils.Operation;

/**
 * Created by Krum Iliev
 */
public class OperationsListAdapter extends BaseAdapter {

    private ArrayList<Operation> mOperations;
    private Context mContext;

    public OperationsListAdapter(Context context, ArrayList<Operation> operations) {
        mOperations = operations;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mOperations.size();
    }

    @Override
    public Object getItem(int position) {
        return mOperations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(mOperations.get(position).orderId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        OperationsListViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.operations_list_row, parent, false);
            viewHolder = new OperationsListViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (OperationsListViewHolder) view.getTag();
        }

        viewHolder.id.setText(mOperations.get(position).serialNumber);
        viewHolder.description.setText(mOperations.get(position).name);
        viewHolder.machine.setText(mOperations.get(position).machineName);

        return view;
    }
}
