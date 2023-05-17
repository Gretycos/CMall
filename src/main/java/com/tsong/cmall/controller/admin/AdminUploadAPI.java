package com.tsong.cmall.controller.admin;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.annotation.TokenToAdminUser;
import com.tsong.cmall.controller.admin.param.BatchUrlParam;
import com.tsong.cmall.entity.AdminUserToken;
import com.tsong.cmall.util.MallUtils;
import com.tsong.cmall.util.Result;
import com.tsong.cmall.util.ResultGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author Tsong
 * @Date 2023/4/3 17:51
 */
@RestController
@Tag(name = "Admin Upload", description = "2-7.后台管理系统文件上传接口")
@RequestMapping("/admin")
public class AdminUploadAPI {
    private static final Logger logger = LoggerFactory.getLogger(AdminUploadAPI.class);

    @Autowired
    private StandardServletMultipartResolver standardServletMultipartResolver;

    /**
     * 图片上传
     */
    @PostMapping(value = "/upload/file")
    @Operation(summary = "单图上传", description = "file Name \"file\"")
    public Result upload(HttpServletRequest httpServletRequest,
                         @RequestParam("file") MultipartFile file,
                         @TokenToAdminUser AdminUserToken adminUser) throws URISyntaxException {
        logger.info("adminUser:{}", adminUser.toString());
        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //生成文件名称通用方法
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random r = new Random();
        StringBuilder tempName = new StringBuilder();
        tempName.append(sdf.format(new Date())).append(r.nextInt(100)).append(suffixName);
        String newFileName = tempName.toString();
        String dirPath = Constants.FILE_UPLOAD_DIC + adminUser.getAdminUserId() + "/";
        File fileDirectory = new File(dirPath);
        //创建文件
        File destFile = new File(dirPath + newFileName);
        try {
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                }
            }
            file.transferTo(destFile);
            Result resultSuccess = ResultGenerator.genSuccessResult();
//            resultSuccess.setData(MallUtils.getHost(new URI(httpServletRequest.getRequestURL() + ""))
//                    + "/upload/" + adminUser.getAdminUserId() + "/" + newFileName);
            resultSuccess.setData("/image/" + adminUser.getAdminUserId() + "/" + newFileName);
            return resultSuccess;
        } catch (IOException e) {
            e.printStackTrace();
            return ResultGenerator.genFailResult("文件上传失败");
        }
    }

    /**
     * 图片上传
     */
    @PostMapping(value = "/upload/files")
    @Operation(summary = "多图上传", description = "图片上传")
    public Result uploadV2(HttpServletRequest httpServletRequest, @TokenToAdminUser AdminUserToken adminUser) throws URISyntaxException {
        logger.info("adminUser:{}", adminUser.toString());
        List<MultipartFile> multipartFiles = new ArrayList<>(8);
        if (standardServletMultipartResolver.isMultipart(httpServletRequest)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) httpServletRequest;
            Iterator<String> iter = multiRequest.getFileNames();
            int total = 0;
            while (iter.hasNext()) {
                if (total > 5) {
                    return ResultGenerator.genFailResult("最多上传5张图片");
                }
                total += 1;
                MultipartFile file = multiRequest.getFile(iter.next());
                multipartFiles.add(file);
            }
        }
        if (CollectionUtils.isEmpty(multipartFiles)) {
            return ResultGenerator.genFailResult("参数异常");
        }
        if (multipartFiles != null && multipartFiles.size() > 5) {
            return ResultGenerator.genFailResult("最多上传5张图片");
        }
        List<String> fileNames = new ArrayList(multipartFiles.size());
        for (MultipartFile multipartFile : multipartFiles) {
            String fileName = multipartFile.getOriginalFilename();
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            //生成文件名称通用方法
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Random r = new Random();
            StringBuilder tempName = new StringBuilder();
            tempName.append(sdf.format(new Date())).append(r.nextInt(100)).append(suffixName);
            String newFileName = tempName.toString();
            String dirPath = Constants.FILE_UPLOAD_DIC + adminUser.getAdminUserId() + "/";
            File fileDirectory = new File(dirPath);
            //创建文件
            File destFile = new File(dirPath + newFileName);
            try {
                if (!fileDirectory.exists()) {
                    if (!fileDirectory.mkdir()) {
                        throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                    }
                }
                multipartFile.transferTo(destFile);
//                fileNames.add(MallUtils.getHost(new URI(httpServletRequest.getRequestURL() + ""))
//                        + "/upload/" + adminUser.getAdminUserId() + "/" + newFileName);
                fileNames.add("/image/" + adminUser.getAdminUserId() + "/" + newFileName);
            } catch (IOException e) {
                e.printStackTrace();
                return ResultGenerator.genFailResult("文件上传失败");
            }
        }
        Result resultSuccess = ResultGenerator.genSuccessResult();
        resultSuccess.setData(fileNames);
        return resultSuccess;
    }

    /**
     * 图片删除
     */
    @PostMapping(value = "/delete/files")
    @Operation(summary = "多图删除", description = "图片删除")
    public Result deleteFiles(@RequestBody @Valid BatchUrlParam batchUrlParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchUrlParam.getUrls().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String dirPath = Constants.FILE_UPLOAD_DIC + adminUser.getAdminUserId() + "/";
        for (String url: batchUrlParam.getUrls()){
             FileSystemUtils.deleteRecursively(new File(dirPath + url.substring(url.lastIndexOf('/') + 1)));
        }
        return ResultGenerator.genSuccessResult("删除成功");
    }

}
