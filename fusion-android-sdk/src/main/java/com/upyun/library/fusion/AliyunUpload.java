package com.upyun.library.fusion;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.upyun.library.common.UpConfig;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.AsyncRun;

import java.io.File;

public class AliyunUpload implements Runnable {

    private Context context;
    private File file;
    private String saveKey;
    private UpProgressListener progressListener;
    private UpCompleteListener completeListener;
    private OSSPlainTextAKSKCredentialProvider credentialProvider;
    private String endpoint;
    private String bucket;

    public AliyunUpload(Context context, File file, String accessKeyId, String accessKeySecret, String endpoint, String bucket, String saveKey, final UpCompleteListener completeListener, final UpProgressListener progressListener) {
        this.credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
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

        final UpProgressListener uiProgressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                AsyncRun.run(new Runnable() {
                    @Override
                    public void run() {
                        if (progressListener != null) {
                            progressListener.onRequestProgress(bytesWrite, contentLength);
                        }
                    }
                });
            }
        };

        final UpCompleteListener uiCompleteListener = new UpCompleteListener() {
            @Override
            public void onComplete(final boolean isSuccess, final String result, final int uploadType) {

                AsyncRun.run(new Runnable() {
                    @Override
                    public void run() {
                        completeListener.onComplete(isSuccess, result, uploadType);
                    }
                });
            }
        };

        OSS oss = new OSSClient(context, endpoint, credentialProvider);
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucket, saveKey, file.getPath());

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                uiProgressListener.onRequestProgress(currentSize, totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                uiCompleteListener.onComplete(true, result.getServerCallbackReturnBody(), UpConfig.ALIYUN);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {

                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    completeListener.onComplete(false, clientExcepion.toString(), UpConfig.ALIYUN);
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    completeListener.onComplete(false, serviceException.toString(), UpConfig.ALIYUN);
                    serviceException.printStackTrace();
                }
            }
        });

        // task.cancel(); // 可以取消任务

        task.waitUntilFinished(); // 可以等待直到任务完成

    }

}
