# UPYUN Android Fusion SDK

[![Build Status](https://travis-ci.org/upyun/android-fusion-sdk.svg?branch=master)](https://travis-ci.org/upyun/android-fusion-sdk)
[ ![Download](https://api.bintray.com/packages/upyun/maven/fusion-android-sdk/images/download.svg) ](https://bintray.com/upyun/maven/fusion-android-sdk/_latestVersion)
[![Software License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](License.md)

UPYUN Android Fusion SDK, 集成：
- [UPYUN HTTP FORM 接口](http://docs.upyun.com/api/form_api/)
- [UPYUN 分块上传接口](http://docs.upyun.com/api/multipart_upload/)
- [七牛上传接口](http://developer.qiniu.com/docs/v6/api/overview/up/upload-models/)
- [阿里上传接口](https://help.aliyun.com/document_detail/oss/sdk/android-sdk/upload-object.html?spm=5176.docoss/sdk/android-sdk/preface.6.281.7RIWDc)

SDK在上传 UPYUN 服务器失败后会自动备份到第三方存储。

## 使用说明：

1.直接[下载 JAR 包](http://jcenter.bintray.com/com/upyun/fusion-android-sdk/1.0.0/)复制进项目使用, SDK 依赖 [okhttp](http://square.github.io/okhttp/) 和 [阿里OSS SDK](https://help.aliyun.com/document_detail/oss/sdk/android-sdk/preface.html?spm=5176.docoss/sdk/android-sdk/upload-object.6.276.j9dUjt)。

2.SDK 已经上传 Jcenter，Android Studio 的用户可以直接在 gradle 中添加一条 dependencies:

```
compile 'com.upyun:fusion-android-sdk:1.0.0'
```
## 参数设置

在 [UpConfig](https://github.com/upyun/android-fusion-sdk/blob/master/fusion-android-sdk/src/main/java/com/upyun/library/common/UpConfig.java) 中可以对 SDK 的一些参数进行配置。

* `BLOCK_SIZE` 单个分块大小
* `FILE_BOUND` 自动判断使用分块或者表单上传的文件大小界限
* `CONCURRENCY` 上传线程并发数量
* `EXPIRATION` 默认过期时间偏移量（秒）
* `FORM_HOST` 表单上传 HOST
* `BLOCK_HOST` 分块上传 HOST
* `RETRY_TIME` 失败重传次数
* `CONNECT_TIMEOUT` 连接超时（秒）
* `READ_TIMEOUT` 读超时（秒）
* `WRITE_TIMEOUT` 写超时（秒）
* `BACKUP_SERVER` 融合云备份服务商，默认七牛
* `TOKEN` 七牛上传 token
* `ENDPOINT` 阿里云OSS endpoint
* `accessKeyId` 阿里云OSS accessKeyId
* `accessKeySecret` 阿里云OSS accessKeySecret
* `aliBucket` 阿里云OSS 空间名

`注：选择融合云服务商后，相应服务商配置必须进行设置`


## 上传接口

> 详细示例请见 app module 下的 [MainActivity](https://github.com/upyun/android-fusion-sdk/blob/master/app/src/main/java/com/upyun/fusionyun/MainActivity.java)。


```
public static void upload(final Context context, final File file, final Map<String, Object> params, String apiKey, SignatureListener signatureListener, final UpCompleteListener completeListener, final UpProgressListener progressListener)
```
参数说明：

* `context` Android 上下文
* `localFilePath` 文件路径
* `paramsMap` 参数键值对
* `KEY` 表单 API 验证密钥（form_api_secret）
* `signatureListener` 获取签名回调
* `completeListener` 结束回调(回调到 UI 线程，不可为 NULL)
* `progressListener` 进度条回调(回调到 UI 线程，可为 NULL)


两种上传方式可根据自己情况选择一种，`KEY` 用户可直接保存在客户端，`signatureListener` 用户可以通过请求服务器获取签名返回客户端。`signatureListener` 回调接口规则如下：

```
SignatureListener signatureListener=new SignatureListener() {
    @Override
    public String getSignature(String raw) {
        return UpYunUtils.md5(raw+KEY);
    }
};
```
将参数 `raw` 传给后台服务器和表单密匙连接后做一次 md5 运算返回结果。

参数键值对中 `Params.BUCKET`（上传空间名）和 `Params.SAVE_KEY` 或 `Params.PATH`（保存路径，任选一个）为必选参数，
其他可选参数见 [Params](https://github.com/upyun/android-fusion-sdk/blob/master/fusion-android-sdk/src/main/java/com/upyun/library/common/Params.java) 或者[官网 API 文档](http://docs.upyun.com/api/form_api/)。

结束回调说明：

```
public interface UpCompleteListener {
    void onComplete(boolean isSuccess, String result,int uploadType);
}
```
* `isSuccess` 成功或者失败
* `result` 返回信息
* `uploadType` 上传返回标识（见 [UpConfig](https://github.com/upyun/android-fusion-sdk/blob/master/fusion-android-sdk/src/main/java/com/upyun/library/common/UpConfig.java)，不同上传类型返回信息不同）


## 测试

```
./gradlew connectedAndroidTest
```
 

## 兼容性

Android 2.3（API10） 以上环境
