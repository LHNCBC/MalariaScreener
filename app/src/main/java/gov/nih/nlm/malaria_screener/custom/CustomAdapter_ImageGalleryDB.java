package gov.nih.nlm.malaria_screener.custom;

import android.content.Context;
import android.graphics.Point;

import androidx.recyclerview.widget.RecyclerView;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 3/22/2017.
 */

public class CustomAdapter_ImageGalleryDB extends RecyclerView.Adapter<CustomAdapter_ImageGalleryDB.ViewHolder> {

    private static final String TAG = "MyDebug";

    private ArrayList<RowItem_Image> rowItem_imageArrayList;
    private Context context;

    OnPhotoCallback photoCallback;

    String[] cell_eachImage;
    String[] infected_eachImage;

    String[] cell_eachImageGT;
    String[] infected_eachImageGT;

    String PIDstr;
    String SIDstr;

    int thinOrThick; // 0 is thin, 1 is thick

    //String[] manualCountsFromTxt;

    public CustomAdapter_ImageGalleryDB(Context context, ArrayList<RowItem_Image> rowItem_imageArrayList, OnPhotoCallback photoCallback, String[] cell_eachImage, String[] infected_eachImage, String PIDstr, String SIDstr, String[] cell_eachImageGT, String[] infected_eachImageGT, int thinOrThick) {
        this.rowItem_imageArrayList = rowItem_imageArrayList;
        this.context = context;
        this.photoCallback = photoCallback;
        this.cell_eachImage = cell_eachImage;
        this.infected_eachImage = infected_eachImage;
        this.PIDstr = PIDstr;
        this.SIDstr = SIDstr;
        this.cell_eachImageGT = cell_eachImageGT;
        this.infected_eachImageGT = infected_eachImageGT;

        this.thinOrThick = thinOrThick;

    }

    @Override
    public CustomAdapter_ImageGalleryDB.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_imagedb, viewGroup, false);

        //manualCountsFromTxt = readTxtFile4ManualCounts();
//        if (manualCountsFromTxt == null){
//            manualCountsFromTxt = new String[cell_eachImage.length*2]; //*2 because there are 2 counts each image, cell count and infected count
//            Arrays.fill(manualCountsFromTxt, "N/A");
//        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomAdapter_ImageGalleryDB.ViewHolder viewHolder, int i) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        String imgStr = "";
        String totalStr = "";
        String infectedStr = "";
        String totalManualStr = "";

        if (thinOrThick == 0) {
            imgStr = context.getResources().getString(R.string.image);
            totalStr = context.getResources().getString(R.string.total);
            infectedStr = context.getResources().getString(R.string.infected);
            totalManualStr = context.getResources().getString(R.string.manual_total);
        } else if (thinOrThick == 1) {
            imgStr = context.getResources().getString(R.string.image);
            totalStr = context.getResources().getString(R.string.wbc);
            infectedStr = context.getResources().getString(R.string.parasite);
            totalManualStr = context.getResources().getString(R.string.manual_wbc);
        }

        viewHolder.textView_imageDetail.setText(imgStr + " " + (i + 1) + " " + totalStr + " " + cell_eachImage[i] + "  " + infectedStr + " " + infected_eachImage[i]);

        String whiteSpce = " ";
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < 13; index++) {
            out.append(whiteSpce);
        }

        viewHolder.textView_imageDetailmanual.setText(out.toString() + totalManualStr + " " + cell_eachImageGT[i] + "  " + infectedStr + " " + infected_eachImageGT[i]);

        // image view 1
        final ImageView imageViewOriginal = viewHolder.imageView_original;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width / 2 - 25, width / 2 - 25);
        parms.setMarginStart(3);
        imageViewOriginal.setLayoutParams(parms);
        imageViewOriginal.setTag(R.id.imageview_original, i);

        viewHolder.imageView_original.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context).load(rowItem_imageArrayList.get(i).getImage_original()).transform(new RotateTransformation(context, 90f)).override(width / 2 - 25, width / 2 - 25).into(imageViewOriginal);

        // image view 2
        final ImageView imageViewResult = viewHolder.imageView_result;
        LinearLayout.LayoutParams parms1 = new LinearLayout.LayoutParams(width / 2 - 25, width / 2 - 25);
        parms1.setMarginStart(3);
        imageViewResult.setLayoutParams(parms1);
        imageViewResult.setTag(R.id.imageview_result, i);

        viewHolder.imageView_result.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context).load(rowItem_imageArrayList.get(i).getImage_result()).override(width / 2 - 25, width / 2 - 25).into(imageViewResult);

        // click on images
        imageViewOriginal.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object object = v.getTag(R.id.imageview_original);
                        View view = imageViewOriginal;
                        photoCallback.onPhotoClick((int) object, "left", view);
                    }
                }

        );

        imageViewResult.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object object = v.getTag(R.id.imageview_result);
                        View view = imageViewResult;
                        photoCallback.onPhotoClick((int) object, "right", view);
                    }
                }

        );

        // long clicks on images
        imageViewOriginal.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Object object = v.getTag(R.id.imageview_original);
                        View view = imageViewOriginal;
                        photoCallback.onPhotoLongClick((int) object, "left", view);

                        return true; // return true so that regular click won't be triggered
                    }
                }
        );

        imageViewResult.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Object object = v.getTag(R.id.imageview_result);
                        View view = imageViewResult;
                        photoCallback.onPhotoLongClick((int) object, "right", view);

                        return true;
                    }
                }

        );

    }

    @Override
    public int getItemCount() {
        return rowItem_imageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_imageDetail;
        private TextView textView_imageDetailmanual;
        private ImageView imageView_original;
        private ImageView imageView_result;

        public ViewHolder(View view) {
            super(view);

            textView_imageDetail = (TextView) view.findViewById(R.id.textView_image_detail);
            textView_imageDetailmanual = (TextView) view.findViewById(R.id.textView_image_detail_manual);
            imageView_original = (ImageView) view.findViewById(R.id.imageview_original);
            imageView_result = (ImageView) view.findViewById(R.id.imageview_result);
        }
    }

    public interface OnPhotoCallback {
        void onPhotoClick(int position, String LOR, View view);

        void onPhotoLongClick(int position, String LOR, View view);
    }

    /*private String[] readTxtFile4ManualCounts() {
        // Get image path
        File slideDir = null;
        if (PIDstr.equals("test")) { // added for test folder images
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/Test/" + SIDstr);
        } else {
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/" + PIDstr + "_" + SIDstr);
        }

        String[] allCounts = new String[rowItem_imageArrayList.size()*2];
        Arrays.fill(allCounts, "N/A");

        if (slideDir.exists()) {

            File[] allImageListing = slideDir.listFiles(); // list all images in this slide directory

            // get all original image file names
            String[] imageName = new String[rowItem_imageArrayList.size()];
            int index = 0;
            for (int i = 0; i < allImageListing.length; i++) {
                String imagePath = allImageListing[i].getAbsolutePath();

                if ((imagePath.indexOf("result") == -1) && (imagePath.indexOf("mask") == -1) && (imagePath.indexOf("png") != -1)) {
                    imageName[index] = imagePath.substring(imagePath.lastIndexOf("/")+1, imagePath.lastIndexOf("."));

                    index++;
                }

            }

            for (int i=0; i<imageName.length; i++){

                for (int j = 0; j < allImageListing.length; j++) {
                    String imagePath = allImageListing[j].getAbsolutePath();
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/")+1);
                    String imageNameTemp = imagePath.substring(imagePath.lastIndexOf("/")+1, imagePath.lastIndexOf("."));
                    if (imageNameTemp.equals(imageName[i]) && fileName.contains("txt")){
                        File file = new File(imagePath);
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line = br.readLine();
                            String[] eachItem = line.split(" ");
                            allCounts[2*i] = eachItem[1];
                            allCounts[2*i+1] = eachItem[2];

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return allCounts;

        }

        return allCounts;
    }*/


}
