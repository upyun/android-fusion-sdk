package com.upyun.library.fusion;


import android.util.Base64;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.upyun.library.common.UpConfig;
import com.upyun.library.exception.RespException;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

public class QiniuResume extends QiniuUpLoad {

    public static String baseUrl = "http://upload.qiniu.com";
    private OkHttpClient client;
    private String token;
    private String key;
    private UpProgressListener progressListener;
    private UpCompleteListener completeListener;
    private File file;
    private RandomAccessFile randomAccessFile = null;
    private Map<String, String> requestParams;
    private int size;
    //七牛分块和分片大小
    public static final int BLOCK_SIZE = 4 * 1024 * 1024;
    private static final int CHUNK_SIZE = 512 * 1024;
    private byte[] chunkBuffer;
    private static final String DefaultMime = "application/octet-stream";
    private String[] contexts;
    private int offset;

    protected QiniuResume(File file, String token, String key, Map<String, String> requestParams, UpCompleteListener completeListener, UpProgressListener progressListener) {
        this.client = new OkHttpClient();
        this.token = token;
        this.key = key;
        this.progressListener = progressListener;
        this.completeListener = completeListener;
        this.file = file;
        this.size = (int) file.length();
        this.requestParams = requestParams;
    }

    public void run() {
        try {
            offset = 0;
            int blockCount = (size + BLOCK_SIZE - 1) / BLOCK_SIZE;
            contexts = new String[blockCount];
            randomAccessFile = new RandomAccessFile(file, "r");
            startUpload();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            completeListener.onComplete(false, e.toString(), UpConfig.QINIU);
            return;
        }
    }

    private void startUpload() {
        while (true) {
            if (offset >= size) {
                try {
                    String result = makeFile();
                    completeListener.onComplete(true, result, UpConfig.QINIU);
                } catch (IOException | RespException e) {
                    e.printStackTrace();
                    completeListener.onComplete(false, e.toString(), UpConfig.QINIU);
                }
                return;
            } else if (offset % BLOCK_SIZE == 0) {
                try {
                    makeBlock();
                    progressListener.onRequestProgress(offset, size);
                } catch (IOException | RespException | JSONException e) {
                    e.printStackTrace();
                    completeListener.onComplete(false, e.toString(), UpConfig.QINIU);
                    return;
                }
            } else {
                try {
                    postChunk();
                    progressListener.onRequestProgress(offset, size);
                } catch (IOException | RespException | JSONException e) {
                    e.printStackTrace();
                    completeListener.onComplete(false, e.toString(), UpConfig.QINIU);
                    return;
                }
            }
        }
    }

    private void postChunk() throws IOException, RespException, JSONException {
        int chunkOffset = offset % BLOCK_SIZE;
        String url = baseUrl + format(Locale.ENGLISH, "/bput/%s/%d", contexts[offset / BLOCK_SIZE], chunkOffset);

        int chunkSize = calcChunkSize(offset);
        chunkBuffer = new byte[chunkSize];
        randomAccessFile.seek(offset);
        randomAccessFile.read(chunkBuffer, 0, chunkSize);

        RequestBody rbody;
        if (chunkBuffer != null && chunkBuffer.length > 0) {
            MediaType t = MediaType.parse(DefaultMime);
            rbody = RequestBody.create(t, chunkBuffer, 0, chunkSize);
        } else {
            rbody = RequestBody.create(null, new byte[0]);
        }
        Request.Builder requestBuilder = new Request.Builder().header("Authorization", "UpToken " + token).url(url).post(rbody);
        String result = post(requestBuilder.build());
        JSONObject reJson = new JSONObject(result);
        contexts[offset / BLOCK_SIZE] = reJson.getString("ctx");
        offset = offset + chunkSize;
    }

    private void makeBlock() throws IOException, RespException, JSONException {
        int blockSize = calcBlockSize(offset);
        String url = baseUrl + format(Locale.ENGLISH, "/mkblk/%d", blockSize);
        int chunkSize = calcChunkSize(offset);
        chunkBuffer = new byte[chunkSize];
        randomAccessFile.seek(offset);
        randomAccessFile.read(chunkBuffer, 0, chunkSize);
        RequestBody rbody;
        if (chunkBuffer != null && chunkBuffer.length > 0) {
            MediaType t = MediaType.parse(DefaultMime);
            rbody = RequestBody.create(t, chunkBuffer, 0, chunkSize);
        } else {
            rbody = RequestBody.create(null, new byte[0]);
        }
        Request.Builder requestBuilder = new Request.Builder().header("Authorization", "UpToken " + token).url(url).post(rbody);
        String result = post(requestBuilder.build());
        JSONObject reJson = new JSONObject(result);
        contexts[offset / BLOCK_SIZE] = reJson.getString("ctx");
        offset = offset + chunkSize;
    }

    private int calcChunkSize(int offset) {
        int left = size - offset;
        return left < CHUNK_SIZE ? left : CHUNK_SIZE;
    }

    private int calcBlockSize(int offset) {
        int left = size - offset;
        return left < BLOCK_SIZE ? left : BLOCK_SIZE;
    }

    private String makeFile() throws IOException, RespException {

        String keyStr = "";
        if (key != null) {
            keyStr = format("/key/%s", encodeToString(key));
        }

        String url = baseUrl + format(Locale.ENGLISH, "/mkfile/%d%s", size, keyStr);
        String bodyStr = join(contexts, ",");
        byte[] data = bodyStr.getBytes();

        RequestBody rbody;
        if (data != null && data.length > 0) {
            MediaType t = MediaType.parse(DefaultMime);
            rbody = RequestBody.create(t, data);
        } else {
            rbody = RequestBody.create(null, new byte[0]);
        }
        Request.Builder requestBuilder = new Request.Builder().header("Authorization", "UpToken " + token).url(url).post(rbody);
        String result = post(requestBuilder.build());
        return result;
    }

    private Object encodeToString(String key) {
        try {
            return Base64.encodeToString(key.getBytes("utf-8"), Base64.URL_SAFE | Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("utf-8 不支持", e);
        }
    }

    public static String join(String[] array, String sep) {
        if (array == null) {
            return null;
        }

        int arraySize = array.length;
        int sepSize = 0;
        if (sep != null && !sep.equals("")) {
            sepSize = sep.length();
        }

        int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].length()) + sepSize) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(sep);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
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
