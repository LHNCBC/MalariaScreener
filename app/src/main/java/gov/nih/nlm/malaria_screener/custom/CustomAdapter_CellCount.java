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
 * Created by yuh5 on 4/29/2016.
 */
public class CustomAdapter_CellCount extends BaseAdapter {

    Context context;
    List<RowItem_CellCount> rowItemCellCounts;

    public CustomAdapter_CellCount(Context context, List<RowItem_CellCount> rowItemCellCounts){
        this.context = context;
        this.rowItemCellCounts = rowItemCellCounts;
    }

    @Override
    public int getCount() {
        return rowItemCellCounts.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItemCellCounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItemCellCounts.indexOf(getItem(position));
    }

    /* private view holder class */
    private class ViewHolder {

        TextView title;
        TextView cells;
        TextView infectedcells;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(gov.nih.nlm.malaria_screener.R.layout.list_item_cellnum, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_title);
            holder.cells = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_cells);
            holder.infectedcells = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_infected);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RowItem_CellCount row_pos = rowItemCellCounts.get(position);

        holder.title.setText(row_pos.getTitle());
        holder.cells.setText(String.valueOf(row_pos.getCells()));
        holder.infectedcells.setText(String.valueOf(row_pos.getInfectedCells()));

        return convertView;
    }




}
