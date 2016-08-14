package tma.elitex.reference.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.TreeMap;

import tma.elitex.R;
import tma.elitex.reference.utils.ReferenceOrder;
import tma.elitex.reference.utils.ReferenceRow;

/**
 * adapter for reference activity list
 * <p/>
 * Created by Krum Iliev
 */
public class ReferenceListAdapter extends BaseAdapter {

    private ArrayList<ReferenceOrder> mData;
    private Context mContext;

    public ReferenceListAdapter(Context context, ArrayList<ReferenceOrder> data) {
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
        return ReferenceRow.getRowView(mContext, mData.get(position));
    }
}
