package com.upyun.library.listener;

import junit.framework.TestCase;

public class UpCompleteListenerTest extends TestCase {

    public void testOnComplete() throws Exception {
        final boolean s = false;
        final String r = "test";

        UpCompleteListener listener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, String result, int uploadType) {
                assertEquals(s, isSuccess);
                assertEquals(r, result);
                assertEquals(11, uploadType);
            }
        };
        listener.onComplete(s, r, 11);
    }
}