package com.upyun.fusionyun;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.upyun.library.common.Params;
import com.upyun.library.fusion.FusionUpload;
import com.upyun.library.listener.SignatureListener;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    public static String KEY = "******";
    public static String SPACE = "******";
    private ProgressBar uploadProgress;
    private TextView textView;
//    private String localPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test3.dmg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置七牛token
        UpConfig.BACKUP_SERVER = UpConfig.QINIU;
        UpConfig.TOKEN = "yH06mc5EzezWl2IassAdeRvD4rnpc6VchnHG01Ch:D4OoBkqRa4owQlAdA03ORxfBOyU=:eyJzY29wZSI6InN1bmRvd24iLCJkZWFkbGluZSI6MTQ5NTI2NjQ4Mn0=";
        
        String savePath = System.currentTimeMillis()+"";
//        File temp = new File(localPath);
        File temp = null;
        try {
            temp = getTempFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        uploadProgress = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.tv_process);
        final Map<String, Object> paramsMap = new HashMap<>();
        //上传空间
        paramsMap.put(Params.BUCKET, SPACE);
        //保存路径，任选其中一个
        paramsMap.put(Params.SAVE_KEY, savePath);
//        paramsMap.put(Params.PATH, savePath);
        //可选参数（详情见api文档介绍）
        paramsMap.put(Params.RETURN_URL, "httpbin.org/post");
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                uploadProgress.setProgress((int) ((100 * bytesWrite) / contentLength));
                textView.setText((100 * bytesWrite) / contentLength + "%");
                Log.e(TAG, bytesWrite + "::" + contentLength + "::" + (100 * bytesWrite) / contentLength + "%");
            }
        };

        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result, int uploadType) {
                textView.setText(isSuccess + ":" + result + ":" + uploadType);
                Log.e(TAG, isSuccess + ":" + result + ":" + uploadType);
            }
        };

        SignatureListener signatureListener = new SignatureListener() {
            @Override
            public String getSignature(String raw) {
                return UpYunUtils.md5(raw + KEY);
            }
        };

        FusionUpload.upload(this, temp, paramsMap, KEY + 1, null, completeListener, progressListener);
    }

    private File getTempFile() throws IOException {
        File temp = File.createTempFile("upyun", "test");
        temp.deleteOnExit();
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(temp));
        outputStream.write("just for test !".getBytes());
        outputStream.close();
        return temp;
    }
}
