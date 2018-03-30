package com.thSDK;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.myanycam.bean.VideoData;
import com.myanycamm.cam.AppServer;
import com.myanycamm.utils.ELog;

import static com.thSDK.lib.jvideo_decode_frame;
import static com.thSDK.lib.jvideo_decode_init;
import static com.thSDK.lib.tag;

/**
 * Created by gyl1 on 3/25/18.
 */

public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    public SurfaceHolder surfaceHolder;

    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }



    public VideoSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e(tag,"surface changed");

        new Thread(this).start();
    }
    public void run() {
        lib.jopenglInit(VideoSurfaceView.this.surfaceHolder.getSurface());
        Log.e(tag,"surface run");
        AppServer.isDisplayVideo = true;
        while (AppServer.isDisplayVideo)
        {
            if (null == VideoData.Videolist || VideoData.Videolist.size() == 0) {
                //
                if (VideoData.audioArraryList.isEmpty()) {

                }

            } else {
                try {

                    byte[] videoData = null;
                    synchronized ( VideoData.Videolist)
                    {
                        videoData = VideoData.Videolist.get(0).getVideoData();
                        VideoData.Videolist.remove(0);
                        if (VideoData.Videolist.size() > 100) {
                            VideoData.Videolist.clear();
                        }
                    }
                    if (videoData != null){
                        long parseTimeBefore = System.currentTimeMillis();
                        ELog.i(TAG, "解码---------0" );


                        parse(videoData,videoData.length);
                        long parseTimeAfter = System.currentTimeMillis();
                        int parseTime = (int) (parseTimeAfter - parseTimeBefore);
                        ELog.i(TAG, "解码显示时间："+parseTime);

                    }


                } catch (NullPointerException e) {
                    ELog.i(TAG, "解码时空指针异常");
                    //VideoData.Videolist.remove(0);
                    continue;
                } catch (IndexOutOfBoundsException e) {
                    //
                }

            }

        }

    }

    void parse(byte []buf,int total) {
        // Log.e("gyl", "parse");

        //

        if (buf[6] != recType) {
            recType = buf[6];
            ELog.i("gyl", "_isFir" + _isFir);
            _isFir = false;
            if (buf[1] == 0x00) {
                Log.e("gyl", "mpeg code");
                type = 0;
            }
            if (buf[1] == 0x01) {
                Log.e("gyl", "h264 code");
                type = 1;
            }
            if (buf[6] == 0x00) {

                width = 160;
                height = 120;
            }
            if (buf[6] == 0x01) {
                width = 176;
                height = 144;
            }
            if (buf[6] == 0x02) {
                width = 320;
                height = 240;
            }
            if (buf[6] == 0x03) {
                width = 352;
                height = 288;
            }
            if (buf[6] == 0x04) {
                width = 640;
                height = 480;
            }
            if (buf[6] == 0x05) {
                width = 720;
                height = 480;
            }
            if (buf[6] == 0x06) {
                width = 1280;
                height = 720;
            }
            if (buf[6] == 0x07) {
                width = 1920;
                height = 1080;
            }
            if (buf[6] == 0x08) {
                width = 640;
                height = 360;
            }
            if (buf[6] == 0x09) {
                width = 320;
                height = 180;
            }
            ELog.i(TAG, "width:" + width + "height:" + height);
            jvideo_decode_init(type, width, height);
        }

        byte[] bmp = new byte[total - 7];
        for (int i = 0; i < total - 7; i++) {
            bmp[i] = buf[i + 7];
        }
        //ByteBuffer subbuf = ByteBuffer.wrap(buf,7,total-7);
        long parseTimeBefore = System.currentTimeMillis();
        ELog.i(TAG, "解码---------0" );

        int n = jvideo_decode_frame( bmp, total - 7);
        ELog.e(TAG, "解码n:" + n+"thead id is "+Thread.currentThread().getId());





    }
    int width;
    int height;
    int type;
    int recType = -1;
    boolean _isFir = true;
    Boolean _quit;
    private static String TAG = "recThread";
}