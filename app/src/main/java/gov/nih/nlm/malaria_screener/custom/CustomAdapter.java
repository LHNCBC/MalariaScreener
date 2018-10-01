package gov.nih.nlm.malaria_screener.custom;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yuh5 on 4/27/2016.
 */
public class CustomAdapter extends BaseAdapter {

    Context context;
    List<RowItem> rowItems;

    public CustomAdapter(Context context, List<RowItem> rowItems) {
        this.context = context;
        this.rowItems = rowItems;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }

    /* private view holder class */
    private class ViewHolder {

        TextView item;
        TextView txt;
    }

    public void updateRowItems(List<RowItem> rowItems){
        this.rowItems = rowItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(gov.nih.nlm.malaria_screener.R.layout.list_item, null);

            holder = new ViewHolder();
            holder.item = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_item);
            holder.txt = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_txt);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RowItem row_pos = rowItems.get(position);

        holder.item.setText(row_pos.getItem());
        holder.txt.setText(row_pos.getTxt());

        return convertView;
    }

}
