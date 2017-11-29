# UPYUN Android Fusion SDK

[![Build Status](https://travis-ci.org/upyun/android-fusion-sdk.svg?branch=master)](https://travis-ci.org/upyun/android-fusion-sdk)
[ ![Download](https://api.bintray.com/packages/upyun/maven/fusion-android-sdk/images/download.svg) ](https://bintray.com/upyun/maven/fusion-android-sdk/_latestVersion)
[![Software License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](License.md)

UPYUN Android Fusion SDK, 提供融合云存储功能, 与 UPYUN 服务器连接不稳定时自动备份到第三方存储。
## 参数设置

在 [FusionConfig](https://github.com/upyun/android-fusion-sdk/blob/master/fusion-android-sdk/src/main/java/com/upyun/library/fusion/FusionConfig.java) 中可以对 SDK 的一些参数进行配置。

* `BACKUP_SERVER` 融合云备份服务商，默认七牛
* `TOKEN` 七牛上传 token
*  `ALITOKEN` 阿里云 OSS StsToken
* `ENDPOINT` 阿里云 SS endpoint
* `accessKeyId` 阿里云 OSS accessKeyId
* `accessKeySecret` 阿里云 OSS accessKeySecret
* `aliBucket` 阿里云 OSS 空间名

`注：1.选择融合云服务商后，相应服务商配置必须进行设置 2.阿里云 StsToken 和 accessKeyId+accessKeySecret 验证方式可任选一种`


## 上传接口

> 详细示例请见 app module 下的 [MainActivity](https://github.com/upyun/android-fusion-sdk/blob/master/app/src/main/java/com/upyun/fusionyun/MainActivity.java)。


本地签名表单上传接口：

```
/**
     * 表单上传
     *
     * @param context          Android 上下文
     * @param file             上传文件
     * @param params           表单上传参数 详见 https://docs.upyun.com/api/form_api/
     * @param operator         操作员
     * @param password         密码（MD5 加密后）
     * @param fusionSavePath   融合云备份上传文件路径
     * @param completeListener 上传完成监听
     * @param progressListener 上传进度监听
     */
    public static void formUpload(final Context context, final File file, final Map<String, Object> params, String operator, String password, String fusionSavePath, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {

```

服务端签名表单上传接口：

```
/**
     * 表单上传
     *
     * @param context          Android 上下文
     * @param file             上传文件
     * @param operator         操作员
     * @param signature        签名
     * @param fusionSavePath   融合云备份上传文件路径
     * @param completeListener 上传完成监听
     * @param progressListener 上传进度监听
     */
    public static void formUpload(final Context context, final File file, final String policy, String operator, String signature, String fusionSavePath, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {

```

断点上传接口：

```
/**
     * 断点上传
     *
     * @param context          Android 上下文
     * @param file             上传文件
     * @param bucket           空间名
     * @param restParams       rest 上传参数 详见 https://docs.upyun.com/api/rest_api/
     * @param operator         操作员
     * @param password         密码（MD5 加密后）
     * @param upyunSavePath    upyun 上传文件路径
     * @param fusionSavePath   融合云备份上传文件路径
     * @param completeListener 上传完成监听
     * @param progressListener 上传进度监听
     */
    public static void resumeUpload(final Context context, final File file, String bucket, Map<String, String> restParams, String operator, String password, String upyunSavePath, String fusionSavePath, final FusionCompleteListener completeListener, final UpProgressListener progressListener) {

```

结束回调说明：

```
public interface UpCompleteListener {
    void onComplete(boolean isSuccess, String result,int uploadType);
}
```
* `isSuccess` 成功或者失败
* `result` 返回信息
* `uploadType` 上传返回标识（见 [FusionConfig](https://github.com/upyun/android-fusion-sdk/blob/master/fusion-android-sdk/src/main/java/com/upyun/library/fusion/FusionConfig.java)，不同上传类型返回信息不同） 

## 兼容性

Android 4.0（API 14） 以上环境
