package com.zanelove.imageloader.imageloaderdemo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ZaneLove on 2015/3/15.
 */
public class DownloadImgUtil {
    /**
     * 根据url下载图片在指定的文件
     * @param urlStr
     * @param imageView
     * @return
     */
    public static Bitmap downloadImageByUrl(String urlStr,ImageView imageView) {
        InputStream is = null;

        try {
            //网络图片地址
            URL url = new URL(urlStr);
            //网络连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //管道输入流
            is = new BufferedInputStream(conn.getInputStream());
            /**
             * BufferedInputStream类调用mark(int readlimit)方法后读取多少字节标记才失效，是取readlimit和BufferedInputStream类的缓冲区大小两者中的最大值，而并非完全由readlimit确定。这个在JAVA文档中是没有提到的。
             */
            is.mark(is.available());

            //图片压缩
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);

            //获得ImageView控件想要显示的宽和高
            ImageSizeUtil.ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
            options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,imageSize.width,imageSize.height);
            options.inJustDecodeBounds = false;
            is.reset();
            bitmap = BitmapFactory.decodeStream(is,null,options);

            conn.disconnect();
            return bitmap;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(is != null) {
                    is.close();
                }
            }catch (Exception e) {

            }
        }
        return null;
    }

    /**
     * 根据Url下载图片在指定的文件
     * @param path
     * @param file
     * @return
     */
    public static boolean downloadImgByUrl(String path,File file) {
        FileOutputStream fos = null;
        InputStream is = null;
        try{
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            is = conn.getInputStream();
            fos = new FileOutputStream(file);
            byte[] buf = new byte[512];
            int len = 0;
            while ((len = is.read(buf))!= -1) {
                fos.write(buf,0,len);
            }
            fos.flush();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(is != null) {
                    is.close();
                }
            }catch (Exception e){
            }
            try{
                if(fos != null) {
                    fos.close();
                }
            }catch (Exception e){
            }
        }
        return false;
    }
}
