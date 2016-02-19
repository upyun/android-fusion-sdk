package com.upyun.library.fusion;

import com.upyun.library.common.UpConfig;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.AsyncRun;

import java.io.File;
import java.util.Map;

public class QiniuUpLoad implements Runnable {

    private QiniuUpLoad instance;

    public QiniuUpLoad() {
    }

    public QiniuUpLoad(File file, String token, String key, Map<String, String> requestParams, final UpCompleteListener completeListener, final UpProgressListener progressListener) {


        UpProgressListener uiProgressListener = new UpProgressListener() {
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

        UpCompleteListener uiCompleteListener = new UpCompleteListener() {
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

        if (file.length() > UpConfig.FILE_BOUND) {
            instance = new QiniuResume(file, token, key, requestParams, uiCompleteListener, uiProgressListener);
        } else {
            instance = new QiniuForm(file, token, key, requestParams, uiCompleteListener, uiProgressListener);
        }
    }

    @Override
    public void run() {
        instance.run();
    }
}
