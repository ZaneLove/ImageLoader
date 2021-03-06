package com.zanelove.imageloader.imageloaderdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.zanelove.imageloader.imageloaderdemo.utils.ImageLoader;
import com.zanelove.imageloader.imageloaderdemo.utils.Images;

/**
 * Created by ZaneLove on 2015/3/15.
 */
public class ListImgsFragment extends Fragment {
    private GridView mGridView;
    private String[] mUrlStrs = Images.imageThumbUrls;
    private ImageLoader mImageLoader;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = ImageLoader.getInstance(3, ImageLoader.Type.LIFO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmet_list_imgs,container,false);
        mGridView = (GridView) view.findViewById(R.id.id_gridview);
        setUpAdapter();
        return view;
    }

    private void setUpAdapter() {
        if(getActivity() == null || mGridView == null) {
            return;
        }
        if(mUrlStrs != null) {
            mGridView.setAdapter(new ListImgItemAdapter(getActivity(),0,mUrlStrs));
        }else {
            mGridView.setAdapter(null);
        }
    }

    private class ListImgItemAdapter extends ArrayAdapter<String> {
        public ListImgItemAdapter(Context context, int resource, String[] datas) {
            super(getActivity(), 0, datas);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.item_fragment_list_imgs, parent, false);
            }
            ImageView imageview = (ImageView) convertView.findViewById(R.id.id_img);
            imageview.setImageResource(R.drawable.pictures_no);
            mImageLoader.loadImage(getItem(position), imageview, true);
            return convertView;
        }
    }
}
