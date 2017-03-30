package com.example.danmudemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {
    private boolean showDanmaku;
    private DanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;
    private BaseDanmakuParser mParser=new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };
    private LinearLayout mLinearLayout;
    private Button mButton;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoView videoView= (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath(Environment.getExternalStorageDirectory()+"/acdjm.mp4");//这里换成自己sd卡的视频资源
        videoView.start();
        mDanmakuView= (DanmakuView) findViewById(R.id.danmaku);
        mLinearLayout= (LinearLayout) findViewById(R.id.ll);
        mButton= (Button) findViewById(R.id.send);
        mEditText= (EditText) findViewById(R.id.edt);
        mDanmakuView.enableDanmakuDrawingCache(true);
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku=true;
                mDanmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        mDanmakuContext=DanmakuContext.create();
        mDanmakuView.prepare(mParser,mDanmakuContext);
        mDanmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLinearLayout.getVisibility()==View.GONE){
                    mLinearLayout.setVisibility(View.VISIBLE);
                }else {
                    mLinearLayout.setVisibility(View.GONE);
                }
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content=mEditText.getText().toString();
                if (!TextUtils.isEmpty(content)){
                    addDanmaku(content,true);
                    mEditText.setText("");
                }
            }
        });
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if (i==View.SYSTEM_UI_FLAG_VISIBLE){
                    onWindowFocusChanged(true);
                }
            }
        });
    }

    private void generateSomeDanmaku() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (showDanmaku){
                            int time=new Random().nextInt(300);
                            String content=""+time+time;
                            addDanmaku(content,false);
                            try {
                                Thread.sleep(time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
    }

    private void addDanmaku(String content, boolean b) {
        BaseDanmaku danmaku=mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text=content;
        danmaku.padding=5;
        danmaku.textSize=sp2px(20);
        danmaku.textColor= Color.BLUE;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        if (b){
            danmaku.borderColor=Color.GREEN;
        }
        mDanmakuView.addDanmaku(danmaku);
    }

    private float sp2px(int i) {
        float fontScale=getResources().getDisplayMetrics().scaledDensity;
        return (int)(i*fontScale+0.5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView!=null&&mDanmakuView.isPrepared()){
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView!=null&&mDanmakuView.isPrepared()&&mDanmakuView.isPaused()){
            mDanmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku=false;
        if (mDanmakuView!=null){
            mDanmakuView.release();
            mDanmakuView=null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus&& Build.VERSION.SDK_INT>19){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        |View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
}
