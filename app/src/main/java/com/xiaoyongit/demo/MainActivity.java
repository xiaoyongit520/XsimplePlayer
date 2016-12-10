package com.xiaoyongit.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xiaoyongit.xsimpleplayer.CustomVideoPlayer;

import io.vov.vitamio.Vitamio;

public class MainActivity extends AppCompatActivity {
    CustomVideoPlayer mVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_main);
        mVideo = (CustomVideoPlayer)findViewById(R.id.video);
        mVideo.setVideTitle("这是一个电影");
        mVideo.setUp("http://cmcc.ips.cnlive.com/content/movie?contentId=622746963&productid=2028593060&ratelevel=2");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideo.onDestroy();
    }
}
