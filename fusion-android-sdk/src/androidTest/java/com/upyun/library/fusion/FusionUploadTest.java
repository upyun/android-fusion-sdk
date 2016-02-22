package com.upyun.library.fusion;

import android.test.AndroidTestCase;
import android.util.Log;

import com.upyun.library.common.Params;
import com.upyun.library.common.UpConfig;
import com.upyun.library.listener.SignatureListener;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;
import com.upyun.library.utils.UpYunUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class FusionUploadTest extends AndroidTestCase {

    private static final String TAG = "FusionUploadTest";
    public static String KEY = "";
    public static String SPACE = "";

    public void testFusion() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        //设置七牛token
        UpConfig.FORM_HOST = "http://127.0.0.1";
        UpConfig.BLOCK_HOST = "http://127.0.0.1";
        UpConfig.BACKUP_SERVER = UpConfig.QINIU;
        UpConfig.TOKEN = "yH06mc5EzezWl2IassAdeRvD4rnpc6VchnHG01Ch:D4OoBkqRa4owQlAdA03ORxfBOyU=:eyJzY29wZSI6InN1bmRvd24iLCJkZWFkbGluZSI6MTQ5NTI2NjQ4Mn0=";

        String savePath = System.currentTimeMillis() + "";
        File temp = File.createTempFile("upyun", "test");
        temp.deleteOnExit();
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(temp));
        outputStream.write("just for test !".getBytes());
        outputStream.close();
        final Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Params.BUCKET, SPACE);
        paramsMap.put(Params.SAVE_KEY, savePath);

        //进度回调，可为空
        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                assertNotNull(bytesWrite);
                assertNotNull(contentLength);
                assertTrue(bytesWrite <= contentLength);
            }
        };

        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result, int uploadType) {
                Log.e(TAG, isSuccess + ":" + result + ":" + uploadType);
                assertNotNull(isSuccess);
                assertNotNull(uploadType);
                assertTrue(isSuccess);
                assertEquals(uploadType, UpConfig.QINIU);
                latch.countDown();
            }
        };

        SignatureListener signatureListener = new SignatureListener() {
            @Override
            public String getSignature(String raw) {
                return UpYunUtils.md5(raw + KEY);
            }
        };

        FusionUpload.upload(mContext, temp, paramsMap, KEY + 1, null, completeListener, progressListener);
        latch.await();
    }
}