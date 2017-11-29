package com.upyun.library.fusion;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.upyun.library.common.ProgressHelper;
import com.upyun.library.exception.RespException;
import com.upyun.library.listener.UpProgressListener;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yemingm on 2017/11/29.
 */

public class QiniuUpload implements Runnable {

    public static String url = "http://upload.qiniu.com/";
    private OkHttpClient client;
    private String token;
    private String key;
    private UpProgressListener progressListener;
    private FusionCompleteListener completeListener;
    private File file;
    private Map<String, String> requestParams;

    protected QiniuUpload(File file, String token, String key, Map<String, String> requestParams, FusionCompleteListener completeListener, UpProgressListener progressListener) {
        this.client = new OkHttpClient();
        this.token = token;
        this.key = key;
        this.progressListener = progressListener;
        this.completeListener = completeListener;
        this.file = file;
        this.requestParams = requestParams;
    }

    @Override
    public void run() {

        UploadOptions uploadOptions = new UploadOptions(null, null, false,
                new UpProgressHandler() {
                    @Override
                    public void progress(String key, double percent) {
                        progressListener.onRequestProgress((long) (file.length() * percent), file.length());
                    }
                }, null);

        UploadManager manager = new UploadManager();
        manager.put(file, key, token, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                completeListener.onComplete(true, response.toString(), FusionConfig.QINIU);
            }
        }, uploadOptions);

//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file))
//                .addFormDataPart("token", token);
//
//        if (key != null) {
//            builder.addFormDataPart("key", key);
//        }
//
//        if (requestParams != null) {
//            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
//                builder.addFormDataPart(entry.getKey(), entry.getValue());
//            }
//        }
//
//        RequestBody requestBody = builder.build();
//
//        if (progressListener != null) {
//            requestBody = ProgressHelper.addProgressListener(requestBody, progressListener);
//        }
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//        try {
//            String result = post(request);
//            completeListener.onComplete(true, result, FusionConfig.QINIU);
//        } catch (IOException | RespException e) {
//            completeListener.onComplete(false, e.toString(), FusionConfig.QINIU);
//        }
//    }
//
//    private String post(Request request) throws IOException, RespException {
//
//        Response response = client.newCall(request).execute();
//        if (!response.isSuccessful()) {
//            throw new RespException(response.code(), response.body().string());
//        } else {
//            return response.body().string();
//        }
    }
}
