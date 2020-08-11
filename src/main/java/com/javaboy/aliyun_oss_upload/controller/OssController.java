package com.javaboy.aliyun_oss_upload.controller;


import com.javaboy.aliyun_oss_upload.bean.ResultBean;
import com.javaboy.aliyun_oss_upload.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/aliyun/oss")
public class OssController {

    private static Logger log = LoggerFactory.getLogger(OssController.class);
    @Autowired
    private OssService ossService;

    @GetMapping("/policy")
    public ResultBean policy() {
        ResultBean result = ossService.getPolicy();
        log.info("服务端生成签名:{}",result);
        return result;
    }


    @PostMapping("/callback")
    public ResultBean callback(HttpServletRequest request) {
        ResultBean ossCallbackResult = ossService.callback(request);
        log.info("oss成功的回调:{}",ossCallbackResult);
        return ossCallbackResult;
    }

}
