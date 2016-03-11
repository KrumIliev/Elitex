package tma.elitex.reference;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tma.elitex.R;

/**
 * adapter for reference activity list
 * <p/>
 * Created by Krum Iliev
 */
public class ReferenceListAdapter extends BaseAdapter {

    private ArrayList<ReferenceData> mData;
    private Context mContext;

    public ReferenceListAdapter(Context context, ArrayList<ReferenceData> data) {
        this.mData = data;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            // If the view is new create it, create the view holder class and pass it to the view via tag
            // to prevent multiple initializations
            view = LayoutInflater.from(mContext).inflate(R.layout.reference_row, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            // Get the view holder class from view tag
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.model.setText(mData.get(position).mModel);
        viewHolder.process.setText(mData.get(position).mProcess);
        viewHolder.batch.setText(mData.get(position).mBatch);
        viewHolder.pieces.setText(mData.get(position).mPieces);

        return view;
    }

    /**
     * Class for holding the layout views. It is needed to prevent list data visualization problems.
     * And prevents views multiple initializations
     */
    public static class ViewHolder {
        public final TextView model;
        public final TextView process;
        public final TextView batch;
        public final TextView pieces;

        public ViewHolder(View view) {
            model = (TextView) view.findViewById(R.id.ref_row_model);
            process = (TextView) view.findViewById(R.id.ref_row_process);
            batch = (TextView) view.findViewById(R.id.ref_row_batch);
            pieces = (TextView) view.findViewById(R.id.ref_row_pieces);
        }
    }
}
