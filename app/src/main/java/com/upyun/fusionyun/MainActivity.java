package com.upyun.fusionyun;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.upyun.library.common.Params;
import com.upyun.library.fusion.FusionCompleteListener;
import com.upyun.library.fusion.FusionConfig;
import com.upyun.library.fusion.FusionUpload;
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
    //空间名
    public static String bucket = "formtest";
    //操作员
    public static String operator = "one";
    //密码
    public static String password = "*****";
    private ProgressBar uploadProgress;
    private TextView textView;
    private String localPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "text1.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置七牛token
        FusionConfig.BACKUP_SERVER = FusionConfig.QINIU;
        FusionConfig.QINIUTOKEN = "yH06mc5EzezWl2IassAdeRvD4rnpc6VchnHG01Ch:GcaVWdefi4uhPmFKNLc-D46aDCU=:eyJzY29wZSI6InN1bmRvd24iLCJkZWFkbGluZSI6MTUxMTk4MDg4N30=";
        //设置 aliyun
        FusionConfig.BACKUP_SERVER = FusionConfig.ALIYUN;
        FusionConfig.ENDPOINT = "http://oss-cn-hangzhou.aliyuncs.com";
        FusionConfig.ACCESSKEYID = "*******";
        FusionConfig.ACCESSKEYSECRET = "*******";
        FusionConfig.ALIBUCKET = "sundown";

        String savePath = System.currentTimeMillis() + "";
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
        paramsMap.put(Params.BUCKET, bucket);
        //保存路径，任选其中一个
        paramsMap.put(Params.SAVE_KEY, savePath);
//        paramsMap.put(Params.PATH, savePath);
        //可选参数（详情见api文档介绍）
        paramsMap.put(Params.RETURN_URL, "httpbin.org/post");
        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                Log.e(TAG, bytesWrite + "::" + contentLength + "::" + (100 * bytesWrite) / contentLength + "%");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgress.setProgress((int) ((100 * bytesWrite) / contentLength));
                        textView.setText((100 * bytesWrite) / contentLength + "%");
                    }
                });
            }
        };

        //结束回调，不可为空
        FusionCompleteListener completeListener = new FusionCompleteListener() {
            @Override
            public void onComplete(final boolean isSuccess, final String result, final String uploadType) {
                Log.e(TAG, isSuccess + ":" + result + ":" + uploadType);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(isSuccess + ":" + result + ":" + uploadType);
                    }
                });
            }
        };

        //表单上传
        FusionUpload.formUpload(this, temp, paramsMap, operator, UpYunUtils.md5(password), "/fusion/" + savePath, completeListener, progressListener);
        //断点上传
        FusionUpload.resumeUpload(this, temp, bucket, null, operator, UpYunUtils.md5(password), savePath, "/fusion/" + savePath, completeListener, progressListener);
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
