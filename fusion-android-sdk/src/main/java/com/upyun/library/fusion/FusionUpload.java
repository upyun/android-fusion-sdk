package com.upyun.library.fusion;

import android.content.Context;
import android.util.Log;

import com.upyun.library.common.Params;
import com.upyun.library.common.ResumeUploader;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FusionUpload {

    private static ExecutorService executor = Executors.newSingleThreadExecutor();


    /**
     * 表单上传
     *
     * @param context          Android 上下文
     * @param file             上传文件
     * @param params           表单上传参数 详见 https://docs.upyun.com/api/form_api/
     * @param operator         操作员
     * @param password         密码（MD5 加密后）
     * @param fusionSavePath   融合云备份上传文件路径
     * @param completeListener 上传完成监听
     * @param progressListener 上传进度监听
     */
    public static void formUpload(final Context context, final File file, final Map<String, Object> params, String operator, String password, String fusionSavePath, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {

        String saveKey = (String) params.get(Params.SAVE_KEY);
        if (saveKey == null) {
            saveKey = (String) params.get(Params.PATH);
        }
        final String finalFusionSaveKey = fusionSavePath != null ? fusionSavePath : saveKey;
        final Runnable[] reUpload = new Runnable[1];
        UpCompleteListener reCompleteListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                if (!isSuccess) {
                    Log.w("upyunFailed", result);
                    switch (FusionConfig.BACKUP_SERVER) {
                        case FusionConfig.QINIU:
                            reUpload[0] = new QiniuUpload(file, FusionConfig.QINIUTOKEN, finalFusionSaveKey, null, completeListener, progressListener);
                            break;
                        case FusionConfig.ALIYUN:
//                            reUpload[0] = new AliyunUpload(context, file, FusionConfig.ACCESSKEYID, FusionConfig.ACCESSKEYSECRET, FusionConfig.ENDPOINT, FusionConfig.ALIBUCKET, finalFusionSaveKey, completeListener, progressListener);
                            reUpload[0] = new AliyunUpload(context, file, FusionConfig.ENDPOINT, FusionConfig.ALIBUCKET, finalFusionSaveKey, completeListener, progressListener);
                            break;
                    }
                    if (reUpload[0] != null) {
                        executor.execute(reUpload[0]);
                    }
                } else {
                    completeListener.onComplete(isSuccess, result, FusionConfig.UPYUN_FORM);
                }
            }
        };
        UploadEngine.getInstance().formUpload(file, params, operator, password, reCompleteListener, progressListener);
    }

    /**
     * 表单上传
     *
     * @param context          Android 上下文
     * @param file             上传文件
     * @param operator         操作员
     * @param signature        签名
     * @param fusionSavePath   融合云备份上传文件路径
     * @param completeListener 上传完成监听
     * @param progressListener 上传进度监听
     */
    public static void formUpload(final Context context, final File file, final String policy, String operator, String signature, String fusionSavePath, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {

        final String finalFusionSaveKey = fusionSavePath;
        final Runnable[] reUpload = new Runnable[1];
        UpCompleteListener reCompleteListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                if (!isSuccess) {
                    Log.w("upyunFailed", result);
                    switch (FusionConfig.BACKUP_SERVER) {
                        case FusionConfig.QINIU:
                            reUpload[0] = new QiniuUpload(file, FusionConfig.QINIUTOKEN, finalFusionSaveKey, null, completeListener, progressListener);
                            break;
                        case FusionConfig.ALIYUN:
//                            reUpload[0] = new AliyunUpload(context, file, FusionConfig.ACCESSKEYID, FusionConfig.ACCESSKEYSECRET, FusionConfig.ENDPOINT, FusionConfig.ALIBUCKET, finalFusionSaveKey, completeListener, progressListener);
                            reUpload[0] = new AliyunUpload(context, file, FusionConfig.ENDPOINT, FusionConfig.ALIBUCKET, finalFusionSaveKey, completeListener, progressListener);
                            break;
                    }
                    if (reUpload[0] != null) {
                        executor.execute(reUpload[0]);
                    }
                } else {
                    completeListener.onComplete(isSuccess, result, FusionConfig.UPYUN_FORM);
                }
            }
        };
        UploadEngine.getInstance().formUpload(file, policy, operator, signature, reCompleteListener, progressListener);
    }

    /**
     * 断点上传
     *
     * @param context          Android 上下文
     * @param file             上传文件
     * @param bucket           空间名
     * @param restParams       rest 上传参数 详见 https://docs.upyun.com/api/rest_api/
     * @param operator         操作员
     * @param password         密码（MD5 加密后）
     * @param upyunSavePath    upyun 上传文件路径
     * @param fusionSavePath   融合云备份上传文件路径
     * @param completeListener 上传完成监听
     * @param progressListener 上传进度监听
     */
    public static void resumeUpload(final Context context, final File file, String bucket, Map<String, String> restParams, String operator, String password, String upyunSavePath, String fusionSavePath, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {

        final String finalFusionSaveKey = fusionSavePath != null ? fusionSavePath : upyunSavePath;
        final Runnable[] reUpload = new Runnable[1];
        ResumeUploader uploader = new ResumeUploader(bucket, operator, password);
        //设置进度监听
        uploader.setOnProgressListener(new UpProgressListener() {
            @Override
            public void onRequestProgress(long bytesWrite, long contentLength) {
                if (progressListener != null) {
                    progressListener.onRequestProgress(bytesWrite, contentLength);
                }
            }
        });

        uploader.upload(file, "/" + URLEncoder.encode(upyunSavePath), restParams, new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result) {
                if (!isSuccess) {
                    Log.w("upyunFailed", result);
                    switch (FusionConfig.BACKUP_SERVER) {
                        case FusionConfig.QINIU:
                            reUpload[0] = new QiniuUpload(file, FusionConfig.QINIUTOKEN, finalFusionSaveKey, null, completeListener, progressListener);
                            break;
                        case FusionConfig.ALIYUN:
                            reUpload[0] = new AliyunResumeUpload(context, file, FusionConfig.ENDPOINT, FusionConfig.ALIBUCKET, finalFusionSaveKey, completeListener, progressListener);
                            break;
                    }
                    if (reUpload[0] != null) {
                        executor.execute(reUpload[0]);
                    }
                } else {
                    completeListener.onComplete(isSuccess, result, FusionConfig.UPYUN_RESUME);
                }
            }
        });
    }
}
