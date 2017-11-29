package com.upyun.library.fusion;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.upyun.library.listener.UpProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AliyunResumeUpload implements Runnable {

    private Context context;
    private File file;
    private String saveKey;
    private UpProgressListener progressListener;
    private FusionCompleteListener completeListener;
    private OSSCredentialProvider credentialProvider;
    private String endpoint;
    private String bucket;

    public AliyunResumeUpload(Context context, File file, String endpoint, String bucket, String saveKey, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {
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


        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(bucket, saveKey);
        try {
            InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

            String uploadId = initResult.getUploadId();

            // part size is 2MB
            long partSize = 2 * 1024 * 1024;

            int currentIndex = 1;

            InputStream input = new FileInputStream(file.getPath());
            long fileLength = file.length();

            long uploadedLength = 0;
            List<PartETag> partETags = new ArrayList<PartETag>();
            while (uploadedLength < fileLength) {
                int partLength = (int) Math.min(partSize, fileLength - uploadedLength);
                byte[] partData = IOUtils.readStreamAsBytesArray(input, partLength);

                UploadPartRequest uploadPart = new UploadPartRequest(bucket, saveKey, uploadId, currentIndex);
                uploadPart.setPartContent(partData);
                UploadPartResult uploadPartResult = oss.uploadPart(uploadPart);
                partETags.add(new PartETag(currentIndex, uploadPartResult.getETag()));

                uploadedLength += partLength;
                currentIndex++;
                progressListener.onRequestProgress(uploadedLength, file.length());
            }

            CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(bucket, saveKey, uploadId, partETags);
            CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);

            completeListener.onComplete(true, completeResult.getServerCallbackReturnBody(), FusionConfig.ALIYUN);

        } catch (ClientException e) {
            e.printStackTrace();
            completeListener.onComplete(false, e.toString(), FusionConfig.ALIYUN);
        } catch (ServiceException e) {
            e.printStackTrace();
            completeListener.onComplete(false, e.toString(), FusionConfig.ALIYUN);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            completeListener.onComplete(false, e.toString(), FusionConfig.ALIYUN);
        } catch (IOException e) {
            e.printStackTrace();
            completeListener.onComplete(false, e.toString(), FusionConfig.ALIYUN);
        }
    }
}
