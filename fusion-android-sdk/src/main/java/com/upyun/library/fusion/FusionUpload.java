package com.upyun.library.fusion;

import android.content.Context;
import android.util.Log;

import com.upyun.library.common.Params;
import com.upyun.library.common.UpConfig;
import com.upyun.library.common.UploadManager;
import com.upyun.library.listener.SignatureListener;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FusionUpload {

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void upload(final Context context, final File file, final Map<String, Object> params, String apiKey, SignatureListener signatureListener, final UpCompleteListener completeListener, final UpProgressListener progressListener) {

        String saveKey = (String) params.get(Params.SAVE_KEY);
        if (saveKey == null) {
            saveKey = (String) params.get(Params.PATH);
        }
        final String finalSaveKey = saveKey;

        final Runnable[] reUpload = new Runnable[1];
        UpCompleteListener reCompleteListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result, int uploadType) {
                if (!isSuccess) {
                    Log.w("upyunFailed", result);
                    switch (UpConfig.BACKUP_SERVER) {
                        case UpConfig.QINIU:
                            reUpload[0] = new QiniuUpLoad(file, UpConfig.TOKEN, finalSaveKey, null, completeListener, progressListener);
                            break;
                        case UpConfig.ALIYUN:
                            reUpload[0] = new AliyunUpload(context, file, UpConfig.ACCESSKEYID, UpConfig.ACCESSKEYSECRET, UpConfig.ENDPOINT, UpConfig.ALIBUCKET, finalSaveKey, completeListener, progressListener);
                            break;
                    }
                    if (reUpload[0] != null) {
                        executor.execute(reUpload[0]);
                    }
                } else {
                    completeListener.onComplete(isSuccess, result, uploadType);
                }
            }
        };

        UploadManager.getInstance().upload(file, params, apiKey, signatureListener, reCompleteListener, progressListener);
    }
}
