package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@Api(tags = "通用接口")
@RequestMapping("/admin/common")
public class Common {
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("upload")
    @ApiOperation("文件上传接口")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}",file);
        //获取文件的原始名 （****.***）
        String originalFilename = file.getOriginalFilename();
        //从最后一个.的索引位置开始截取原始名得到文件名后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        //通过uuid获取随机字符串并与后缀进行拼接 得到上传到阿里云的文件名
        String objectName = UUID.randomUUID().toString() + extension;
        //调用aliossutill工具类的upload方法 获取文件访问路径 返回给前端 前端通过访问该路径进行页面展示
        String path = aliOssUtil.upload(file.getBytes(), objectName);
        return Result.success(path);
    }
}
