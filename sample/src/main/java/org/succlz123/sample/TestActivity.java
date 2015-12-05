package org.succlz123.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.succlz123.okplayer.utils.OkPlayerUtils;
import org.succlz123.okplayer.view.OkVideoView;

import java.io.File;

/**
 * Created by succlz123 on 15/12/2.
 */
public class TestActivity extends AppCompatActivity {
    private OkVideoView mOkVideoView;
    private Uri uri;
    private int contentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test);
        mOkVideoView = (OkVideoView) findViewById(R.id.ok_video_view);

//        Intent intent = getIntent();
//
//        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        uri = Uri.parse(storagePath + File.separator + "0.mp4");
//        contentType = intent.getIntExtra(OkPlayerUtils.CONTENT_TYPE,
//                OkPlayerUtils.inferContentType(uri, intent.getStringExtra(OkPlayerUtils.TYPE)));
//
//        okExoVideoView.setVideoUri(uri, contentType);
    }

    @Override
    public void onNewIntent(Intent intent) {
        mOkVideoView.onNewIntent();
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOkVideoView.onResume();
        Intent intent = getIntent();
//        uri = intent.getData();
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        uri = Uri.parse(storagePath + File.separator + "0.mp4");
        contentType = intent.getIntExtra(OkPlayerUtils.CONTENT_TYPE,
                OkPlayerUtils.inferContentType(uri, intent.getStringExtra(OkPlayerUtils.TYPE)));

        mOkVideoView.setVideoUri(uri, contentType);
    }

    @Override
    public void onPause() {
        super.onPause();
        mOkVideoView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOkVideoView.onDestroy();
    }
}
