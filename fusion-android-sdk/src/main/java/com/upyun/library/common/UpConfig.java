package com.upyun.library.common;

public class UpConfig {
    //分块大小
    public static int BLOCK_SIZE = 500 * 1024;
    //分块与表单上传文件大小界限
    public static long FILE_BOUND = 4 * 1024 * 1024;
    //并发线程数
    public static int CONCURRENCY = 2;
    //默认过期时间1800s
    public static long EXPIRATION = 1800;
    //表单和分块host地址
    public static final String FORM_HOST = "http://v0.api.upyun.com";
    public static final String BLOCK_HOST = "http://m0.api.upyun.com";
    //失败重传次数
    public static final int RETRY_TIME = 2;

    //超时设置 单位秒(SECONDS)
    public static int CONNECT_TIMEOUT = 15;
    public static int READ_TIMEOUT = 30;
    public static int WRITE_TIMEOUT = 30;

    //空间名 用户可以直接先设置全局BUCKET，以后上传不用再传入BUCKET参数
    public static String BUCKET;

    //上传方式类型
    public final static int UPYUN_FORM = 11;
    public final static int UPYUN_BLOCK = 12;
    public final static int QINIU = 13;
    public final static int ALIYUN =14;

    //融合云相关配置
    //七牛token配置
    public static String TOKEN = null;
    //阿里云配置
    public static final String ENDPOINT = null;
    public static final String ACCESSKEYID = null;
    public static final String ACCESSKEYSECRET = null;
    public static final String ALIBUCKET = null;

    //融合云第三方存储设置，默认七牛
    public static int BACKUP_SERVER = QINIU;
//    public static int BACKUP_SERVER = ALIYUN;
}
