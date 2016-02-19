package com.upyun.library.fusion;

import android.util.Log;

import com.upyun.library.common.UpConfig;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.listener.UpProgressListener;

import junit.framework.TestCase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

public class QiniuFormTest extends TestCase {
    public String TOKEN = "yH06mc5EzezWl2IassAdeRvD4rnpc6VchnHG01Ch:D4OoBkqRa4owQlAdA03ORxfBOyU=:eyJzY29wZSI6InN1bmRvd24iLCJkZWFkbGluZSI6MTQ5NTI2NjQ4Mn0=";
    String savePath = System.currentTimeMillis() + "";

    public void testQiniuForm() throws Exception {

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
                assertNotNull(result);
                assertTrue(isSuccess);
                assertEquals(uploadType, UpConfig.QINIU);
                latch.countDown();
            }
        };


        QiniuForm qiniuForm = new QiniuForm(temp, TOKEN, savePath, null, completeListener, progressListener);
        new Thread(qiniuForm).start();
        latch.await();
    }
}