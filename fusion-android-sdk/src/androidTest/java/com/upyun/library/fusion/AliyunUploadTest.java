package com.upyun.library.fusion;

import android.test.AndroidTestCase;
import android.util.Log;

import com.upyun.library.common.UpConfig;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

public class AliyunUploadTest extends AndroidTestCase {
    //阿里云配置
    public static final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    public static final String accessKeyId = "7R6uaaS3RJtsjG2n";
    public static final String accessKeySecret = "YZBaLEvIqzT46vHEzkc43Rfrda3w4c";
    public static final String aliBucket = "sundown";

    public void testAliyunUpload() throws Exception {
        String savePath = System.currentTimeMillis() + "";
        final CountDownLatch latch = new CountDownLatch(1);

        File temp = File.createTempFile("upyun", "test");
        temp.deleteOnExit();
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(temp));
        outputStream.write("just for test !".getBytes());
        outputStream.close();

        UpProgressListener progressListener = new UpProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWrite, final long contentLength) {
                assertNotNull(bytesWrite);
                assertNotNull(contentLength);
                assertTrue(bytesWrite <= contentLength);
            }
        };

        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result, int uploadType) {
                Log.e("lalala", isSuccess + result);
                assertNotNull(isSuccess);
                assertTrue(isSuccess);
                assertEquals(uploadType, UpConfig.ALIYUN);
                latch.countDown();
            }
        };


        AliyunUpload aliyunUpload = new AliyunUpload(mContext, temp, accessKeyId, accessKeySecret, endpoint, aliBucket, savePath, completeListener, progressListener);
        new Thread(aliyunUpload).start();
        latch.await();
    }
}