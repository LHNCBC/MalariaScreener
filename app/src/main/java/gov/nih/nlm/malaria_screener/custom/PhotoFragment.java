package gov.nih.nlm.malaria_screener.custom;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 4/3/2017.
 */

public class PhotoFragment extends Fragment{

    private static final String TAG = "MyDebug";

    private Context context;

    private int position;

    private String imagePath;

    //PassView passView;

    TouchImageView backgroundImage;

    public static PhotoFragment create(int pos, String imgPath) {

        PhotoFragment frag = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putString("img_path", imgPath);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
        //passView = (PassView) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");

        imagePath = getArguments().getString("img_path");

        //position = getArguments() != null ? getArguments().getInt("pos") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.item_photo_full, container, false);

        //FrameLayout frameLayout = (FrameLayout)rootView.findViewById(R.id.container);

        backgroundImage = (TouchImageView) rootView.findViewById(R.id.imageView_full);

        if (position%2==0) {
            Glide.with(this).load(imagePath).transform(new RotateTransformation(context, 90f)).skipMemoryCache(true).into(backgroundImage);
        } else {
            Glide.with(this).load(imagePath).skipMemoryCache(true).into(backgroundImage);
        }

        //passView.passExpandedImageView_Container(backgroundImage, frameLayout);

        return rootView;
    }

//    public void setImageView(){
//        Glide.with(this).load(imagePath).into(backgroundImage);
//
//    }

//    public interface PassView{
//
//        void passExpandedImageView_Container(TouchImageView imageView, FrameLayout frameLayout);
//
//    }

}
