package gov.nih.nlm.malaria_screener.custom;

import android.app.Dialog;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;


/**
 * Created by yuh5 on 9/14/2017.
 */

public class CustomAdapter_ManualCounts_thick extends RecyclerView.Adapter<CustomAdapter_ManualCounts_thick.ViewHolder> {

    public static final String TAG = "MyDebug";

    private Context context;
    int itemCount;

    String[] slide_item;
    public String[] slide_txt;
    List<RowItem> rowItems_slide;
    List<List<RowItem>> r;
    List<CustomAdapter> adapters;

    public CustomAdapter_ManualCounts_thick(Context context, int itemCount, String[] wbc_eachImageGT, String[] parasite_eachImageGT) {
        this.context = context;
        this.itemCount = itemCount;

        r = new ArrayList<List<RowItem>>();
        adapters = new ArrayList<CustomAdapter>();

        slide_item = context.getResources().getStringArray(R.array.manual_counts_thick);

        slide_txt = new String[itemCount * 2];
        for (int i = 0; i < this.itemCount; i++) {

            slide_txt[2*i+0] = wbc_eachImageGT[i];
            slide_txt[2*i+1] = parasite_eachImageGT[i];

            rowItems_slide = new ArrayList<RowItem>();
            r.add(rowItems_slide);

            RowItem item = new RowItem(slide_item[0], slide_txt[2 * i + 0]);
            rowItems_slide.add(item);
            RowItem item1 = new RowItem(slide_item[1], slide_txt[2 * i + 1]);
            rowItems_slide.add(item1);

        }


    }

    @Override
    public CustomAdapter_ManualCounts_thick.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_manualcounts, viewGroup, false);
        return new CustomAdapter_ManualCounts_thick.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomAdapter_ManualCounts_thick.ViewHolder holder, int position) {

        holder.textView_imageTitle.setText("Image " + (position + 1));

        final int pos = position;

        adapters.add(new CustomAdapter(context, r.get(position)));

        holder.listView_manualcounts.setAdapter(adapters.get(position));

        holder.listView_manualcounts.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showInputBox(position, pos);
                    }
                }
        );


    }

    public void showInputBox(final int pos, final int posInRecycleV) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setContentView(R.layout.input_box_manualcounts);
        TextView textView = (TextView) dialog.findViewById(R.id.textView_manualcounts);
        if (pos == 0) {
            textView.setText(R.string.manual_wbc_count);
        } else if (pos == 1) {
            textView.setText(R.string.manual_parasite_infected);
        }
        final EditText editText = (EditText) dialog.findViewById(R.id.editText_manualcounts);
        Button button_okay = (Button) dialog.findViewById(R.id.button_okay);
        button_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 0) { // if input from user is empty
                    slide_txt[posInRecycleV * 2 + pos] = "N/A";
                } else {
                    slide_txt[posInRecycleV * 2 + pos] = editText.getText().toString();
                }

                RowItem item = new RowItem(slide_item[pos], slide_txt[posInRecycleV * 2 + pos]);

                r.get(posInRecycleV).set(pos, item);

                adapters.get(posInRecycleV).updateRowItems(r.get(posInRecycleV));
                adapters.get(posInRecycleV).notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        Button button_cancel = (Button) dialog.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }

        );
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_imageTitle;
        private ListView listView_manualcounts;


        public ViewHolder(View view) {
            super(view);

            textView_imageTitle = (TextView) view.findViewById(R.id.textView_image_title);
            listView_manualcounts = (ListView) view.findViewById(R.id.listView_manualcount);

        }
    }


}
