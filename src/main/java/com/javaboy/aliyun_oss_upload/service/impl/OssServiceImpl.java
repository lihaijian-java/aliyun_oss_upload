package com.javaboy.aliyun_oss_upload.service.impl;

import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.javaboy.aliyun_oss_upload.bean.Constants;
import com.javaboy.aliyun_oss_upload.bean.ResultBean;
import com.javaboy.aliyun_oss_upload.bean.OssCallbackParam;
import com.javaboy.aliyun_oss_upload.service.OssService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class OssServiceImpl implements OssService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssServiceImpl.class);
    @Value("${aliyun.oss.policy.expire}")
    private int ALIYUN_OSS_EXPIRE;
    @Value("${aliyun.oss.maxSize}")
    private int ALIYUN_OSS_MAX_SIZE;
    @Value("${aliyun.oss.callback}")
    private String ALIYUN_OSS_CALLBACK;
    @Value("${aliyun.oss.bucketName}")
    private String ALIYUN_OSS_BUCKET_NAME;
    @Value("${aliyun.oss.endpoint}")
    private String ALIYUN_OSS_ENDPOINT;
    @Value("${aliyun.oss.dir.prefix}")
    private String ALIYUN_OSS_DIR_PREFIX;

    @Autowired
    private OSSClient ossClient;
    @Override
    public ResultBean getPolicy() {
        JSONObject resultBean = new JSONObject();
        // 存储目录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dir = ALIYUN_OSS_DIR_PREFIX+sdf.format(new Date())+"/";
        // 签名有效期
        long expireEndTime = System.currentTimeMillis() + ALIYUN_OSS_EXPIRE * 1000;
        Date expiration = new Date(expireEndTime);
        // 文件大小
        long maxSize = ALIYUN_OSS_MAX_SIZE * 1024 * 1024;
        // 回调
        OssCallbackParam callback = new OssCallbackParam();
        callback.setCallbackUrl(ALIYUN_OSS_CALLBACK);
        callback.setCallbackBody("filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
        callback.setCallbackBodyType("application/x-www-form-urlencoded");
        // 提交节点
        String action = "https://" + ALIYUN_OSS_ENDPOINT;
        try {
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, maxSize);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String policy = BinaryUtil.toBase64String(binaryData);
            String signature = ossClient.calculatePostSignature(postPolicy);
            String callbackData = BinaryUtil.toBase64String(JSONUtil.parse(callback).toString().getBytes("utf-8"));
            resultBean.put("accessid",ossClient.getCredentialsProvider().getCredentials().getAccessKeyId());
            resultBean.put("policy",policy);
            resultBean.put("signature",signature);
            resultBean.put("dir",dir);
            resultBean.put("expire",String.valueOf(expireEndTime / 1000));
            resultBean.put("callback",callbackData);
            resultBean.put("host",action);
            // 返回结果
        } catch (Exception e) {
            LOGGER.error("签名生成失败", e);
            return new ResultBean(Constants.RESCODE_SIGNATION_FAILED,Constants.RESMSG_SIGNATION_FAILED);
        }
        return new ResultBean(Constants.RESCODE_SUCCESS,Constants.RESMSG_SUCCESS,resultBean);
    }

    @Override
    public ResultBean callback(HttpServletRequest request) {
        JSONObject resultBean = new JSONObject();
        String filename = request.getParameter("filename");
        filename = "https://".concat(ALIYUN_OSS_ENDPOINT).concat("/").concat(filename);
        resultBean.put("filename",filename);
        resultBean.put("size",request.getParameter("size"));
        resultBean.put("mimeType",request.getParameter("mimeType"));
        resultBean.put("width",request.getParameter("width"));
        resultBean.put("height",request.getParameter("height"));

       return new ResultBean(Constants.RESCODE_SUCCESS,Constants.RESMSG_SUCCESS,resultBean);
    }
}
