package com.javaboy.aliyun_oss_upload.bean;

import lombok.Data;

/**
 * oss上传成功后的回调参数
 */
@Data
public class OssCallbackParam {
    //请求的回调地址
    private String callbackUrl;
   //回调是传入request中的参数
    private String callbackBody;
   //回调时传入参数的格式，比如表单提交形式
    private String callbackBodyType;
}
