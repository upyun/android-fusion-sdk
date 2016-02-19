package com.upyun.library.fusion;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.upyun.library.common.ProgressHelper;
import com.upyun.library.common.UpConfig;
import com.upyun.library.exception.RespException;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class QiniuForm extends QiniuUpLoad {

    public static String url = "http://upload.qiniu.com/";
    private OkHttpClient client;
    private String token;
    private String key;
    private UpProgressListener progressListener;
    private UpCompleteListener completeListener;
    private File file;
    private Map<String, String> requestParams;

    protected QiniuForm( File file, String token, String key, Map<String, String> requestParams, UpCompleteListener completeListener, UpProgressListener progressListener) {
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

        MultipartBuilder builder = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .addFormDataPart("token", token);

        if(key!=null) {
            builder.addFormDataPart("key", key);
        }

        if(requestParams!=null){
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = builder.build();

        if (progressListener != null) {
            requestBody = ProgressHelper.addProgressListener(requestBody, progressListener);
        }

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            String result = post(request);
            completeListener.onComplete(true, result, UpConfig.QINIU);
        } catch (IOException | RespException e) {
            completeListener.onComplete(false, e.toString(), UpConfig.QINIU);
        }
    }

    private String post(Request request) throws IOException, RespException {

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RespException(response.code(), response.body().string());
        } else {
            return response.body().string();
        }
    }
}
