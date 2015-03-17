package com.zanelove.imageloader.imageloaderdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载类
 * Created by ZaneLove on 2015/3/15.
 */
public class ImageLoader {
    //线程数量
    private int threadCount;
    //队列调度方式
    private Type mType = Type.LIFO;
    //图片缓存的核心对象
    private LruCache<String,Bitmap> mLruCache;
    //线程池
    private ExecutorService mThreadPool;
    private static final int DEAFULT_THREAD_COUNT = 1;
    //任务队列
    private LinkedList<Runnable> mTaskQueue;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    //后台轮询线程
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    private Runnable task;

    //UI线程中的Handler
    private Handler mUIHandler;
    //是否开启硬盘缓存
    private boolean isDiskCacheEnable = true;
    /**
     * 单例模式
     */
    private static ImageLoader instance = null;

    private ImageLoader(int threadCount,Type type){
        init(threadCount,type);
    }

    public static ImageLoader getInstance(int threadCount,Type type) {
        if(instance == null) {
            synchronized (ImageLoader.class) {
                if(instance == null) {
                    instance = new ImageLoader(threadCount, type);
                    return  instance;
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     * @param threadCount 线程数量
     * @param type 队列调度方式
     */
    private void init(int threadCount, Type type) {
        //初始化后台轮询线程
        initBackThread();
        //获得应用最大可用内存的1/8，分配给LruCache
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        //创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    private void initBackThread() {
        mPoolThread = new Thread(){
            @Override
            public void run() {
                //创建轮询器也会创建消息队列
                Looper.prepare();
                //创建消息处理器
                mPoolThreadHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //去线程池取出一个任务进行执行
                        mThreadPool.execute(getTask());
                        try{
                            mSemaphoreThreadPool.acquire();
                        }catch (Exception e) {
                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                //开启轮询器
                Looper.loop();
            }
        };
        //开启线程
        mPoolThread.start();
    }

    public enum Type {
        FIFO, //先进先出
        LIFO  //后进先出
    }

    /**
     * 从任务队列取出一个任务
     * @return
     */
    public Runnable getTask() {
        if(mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        }else if(mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }


    /**
     * 根据path为ImageView设置图片
     * @param path
     * @param imageView
     * @param isFromNet 是否来自网络
     */
    public void loadImage(final String path,final ImageView imageView,final boolean isFromNet) {
        imageView.setTag(path);
        if(mUIHandler == null) {
            mUIHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取得到图片，为ImageView回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageView = holder.imageView;
                    String path = holder.path;
                    //将path与getTag存储路径进行比较,防止图片混乱
                    if(imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }
                }
            };
        }
        //根据path缓存中获取bitmap
        Bitmap bm = getBitmapFromLruCache(path);
        if(bm != null) {
            //找到了图片
            refreashBitmap(path,imageView,bm);
        }else {
            //没有找到图片
            //通过buildTask去新建一个任务，再addTask到任务队列
            addTask(buildTask(path,imageView,isFromNet));
        }
    }

    /**
     * 添加任务到队列
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try{
            if(mPoolThreadHandler == null) {
                mSemaphorePoolThreadHandler.acquire();
            }
        }catch (Exception e) {

        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * 新建一个任务
     * @param path
     * @param imageView
     * @param isFromNet
     * @return
     */
    private Runnable buildTask(final String path, final ImageView imageView, final boolean isFromNet) {
        return new Runnable() {
            @Override
            public void run() {
                Bitmap bm = null;
                if (isFromNet) {
                    File file = getDiskCacheDir(imageView.getContext(), md5(path));
                    // 如果在缓存文件中发现
                    if (file.exists()) {
                        bm = loadImageFromLocal(file.getAbsolutePath(), imageView);
                    } else {
                        // 检测是否开启硬盘缓存
                        if (isDiskCacheEnable) {
                            boolean downloadState = DownloadImgUtil.downloadImgByUrl(path, file);
                            // 如果下载成功
                            if (downloadState) {
                                bm = loadImageFromLocal(file.getAbsolutePath(), imageView);
                            }
                        } else {// 直接从网络加载
                            bm = DownloadImgUtil.downloadImageByUrl(path,imageView);
                        }
                    }
                } else {
                    bm = loadImageFromLocal(path, imageView);
                }
                // 把图片加入到缓存
                addBitmapToLruCache(path, bm);
                refreashBitmap(path, imageView, bm);
                mSemaphoreThreadPool.release();
            }
        };
    }

    /**
     * 将图片加入LruCache
     * @param path
     * @param bm
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if(getBitmapFromLruCache(path) == null) {
            if(bm != null) {
                mLruCache.put(path,bm);
            }
        }
    }

    /**
     * 从本地缓存文件中加载图片
     * @param path
     * @param imageView
     * @return
     */
    private Bitmap loadImageFromLocal(String path, ImageView imageView) {
        Bitmap bm;
        //加载图片
        //图片压缩
        //1、获得图片需要显示的大小
        ImageSizeUtil.ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
        //2、压缩图片
        bm = decodeSampledBitmapFromPath(path,imageSize.width,imageSize.height);
        return bm;
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        //获得图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,width,height);
        //使用获得到的inSampleSize再次解析图片
        options.inJustDecodeBounds =false;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        return bitmap;
    }

    /**
     * 获得缓存图片的地址
     * @param context
     * @param mdStr
     * @return
     */
    private File getDiskCacheDir(Context context, String mdStr) {
        String cachePath;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cachePath = context.getExternalCacheDir().getPath();
            isDiskCacheEnable = true;
        }else {
            cachePath = context.getCacheDir().getPath();
            isDiskCacheEnable = false;
        }
        return new File(cachePath + File.separator + mdStr);
    }

    /**
     * 利用签名辅助类，将字符串字节数组
     * @param str
     */
    private String md5(String str) {
        byte[] digest = null;
        try{
            MessageDigest md = MessageDigest.getInstance("md5");
            digest = md.digest(str.getBytes());
            return bytes2hex02(digest);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String bytes2hex02(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for(byte b : bytes) {
            //将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            if(tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }
        return sb.toString();
    }

    /**
         * 去设置图片显示在控件上
     * @param path
     * @param imageView
     * @param bm
     */
    private void refreashBitmap(String path, ImageView imageView, Bitmap bm) {
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 根据path从缓存当中获取bitmap
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    /**
     * Holder
     */
    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}
