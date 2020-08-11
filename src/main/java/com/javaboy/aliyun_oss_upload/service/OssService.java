package com.javaboy.aliyun_oss_upload.service;


import com.javaboy.aliyun_oss_upload.bean.ResultBean;

import javax.servlet.http.HttpServletRequest;

public interface OssService {
    ResultBean getPolicy();

    ResultBean callback(HttpServletRequest request);
}
