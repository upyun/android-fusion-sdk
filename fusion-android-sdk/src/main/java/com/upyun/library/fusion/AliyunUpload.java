package com.upyun.library.fusion;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.upyun.library.listener.UpProgressListener;

import java.io.File;

public class AliyunUpload implements Runnable {

    private Context context;
    private File file;
    private String saveKey;
    private UpProgressListener progressListener;
    private FusionCompleteListener completeListener;
    private OSSCredentialProvider credentialProvider;
    private String endpoint;
    private String bucket;

    public AliyunUpload(Context context, File file, String endpoint, String bucket, String saveKey, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {
        if (FusionConfig.ALITOKEN != null) {
            this.credentialProvider = new OSSStsTokenCredentialProvider(FusionConfig.ALITOKEN);
        } else {
            this.credentialProvider = new OSSPlainTextAKSKCredentialProvider(FusionConfig.ACCESSKEYID, FusionConfig.ACCESSKEYSECRET);
        }
        this.context = context;
        this.file = file;
        this.saveKey = saveKey;
        this.progressListener = progressListener;
        this.completeListener = completeListener;
        this.endpoint = endpoint;
        this.bucket = bucket;
    }

    @Override
    public void run() {

        if (saveKey.startsWith("/")) {
            saveKey = saveKey.subSequence(1, saveKey.length()).toString();
        }

        OSS oss = new OSSClient(context, endpoint, credentialProvider);
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucket, saveKey, file.getPath());

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                progressListener.onRequestProgress(currentSize, totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                completeListener.onComplete(true, result.getServerCallbackReturnBody(), FusionConfig.ALIYUN);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {

                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    completeListener.onComplete(false, clientExcepion.toString(), FusionConfig.ALIYUN);
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    completeListener.onComplete(false, serviceException.toString(), FusionConfig.ALIYUN);
                    serviceException.printStackTrace();
                }
            }
        });
        // task.cancel(); // 可以取消任务
        task.waitUntilFinished(); // 可以等待直到任务完成
    }
}
