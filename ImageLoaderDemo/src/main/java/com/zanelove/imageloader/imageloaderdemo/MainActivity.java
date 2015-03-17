package com.zanelove.imageloader.imageloaderdemo;

import android.support.v4.app.Fragment;


public class MainActivity extends AbsSingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ListImgsFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_single_fragment;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView iv = (ImageView) findViewById(R.id.iv);

        *//**
         * 本地图片的压缩
         *//*
        //获的图片真正的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),R.drawable.v,options);

        //根据ImageView获得适应的压缩的宽和高
        ImageSizeUtil.ImageSize imageSize = ImageSizeUtil.getImageViewSize(iv);
        //根据需求的宽和高以及图片实际的宽和高计算SampleSize,为了对图片进行压缩
        options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,imageSize.width,imageSize.height);

        //使用获得到的inSampleSize，再次解析图片并压缩图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.v, options);
//        iv.setImageBitmap(bitmap);

        //----------------------------------------------------------------------------------------------
        *//**
         * 网络图片的压缩
         *  a、直接下载存到sd卡，然后采用本地的压缩方案。这种方式当前是在硬盘缓存开启的情况下，如果没有开启呢？
         *  b、使用BitmapFactory.decodeStream(is, null, opts);
         *//*
        Bitmap bitmap1 =  DownloadImgUtil.downloadImageByUrl("http://img.my.csdn.net/uploads/201407/26/1406383299_1976.jpg", iv);
        iv.setImageBitmap(bitmap1);
    }*/
}
