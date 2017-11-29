package com.upyun.library.fusion;

import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;

public class FusionConfig {
    //分块与表单上传文件大小界限
    public static long FILE_BOUND = 4 * 1024 * 1024;

    //返回标识
    public final static String UPYUN_FORM = "UPYUN_FORM";
    public final static String UPYUN_RESUME = "UPYUN_RESUME";
    public final static String QINIU = "QINIU";
    public final static String ALIYUN = "ALIYUN";

    //融合云相关配置
    //七牛token配置
    public static String QINIUTOKEN = null;
    //阿里云配置
    public static String ENDPOINT = null;
    public static String ACCESSKEYID = null;
    public static String ACCESSKEYSECRET = null;
    public static String ALIBUCKET = null;

    public static OSSFederationToken ALITOKEN = null;

    //融合云第三方存储设置，默认七牛
    public static String BACKUP_SERVER = QINIU;
//    public static int BACKUP_SERVER = ALIYUN;
}
