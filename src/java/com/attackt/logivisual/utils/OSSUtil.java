package com.attackt.logivisual.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * 阿里oss文件处理类
 */
public class OSSUtil {
    Config config = ConfigFactory.load();

    /**
     * 下载文件
     * @param filePath 文件路径
     * @return 文件
     */
    public File downloadFile(String filePath)
    {
        File file = new File(filePath);
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(config.getString("oss.endpoint"), config.getString("oss.accessKeyId"), config.getString("oss.accessKeySecret"));
        ossClient.getObject(new GetObjectRequest(config.getString("oss.bucketName"), file.getName()), file);
        // 关闭client
        ossClient.shutdown();
        return file;
    }

    /**
     * 上传文件
     * @param filePath 本地文件路径
     * @param fileName 要保存的文件名称
     * @param isdel 上传完后是否删除(true 删除 false 不删除)
     * @return 文件名
     */
    public String uploadFile(String filePath, String fileName, boolean isdel)
    {
        File file = new File(filePath);
        if(fileName == null)
        {
            fileName = file.getName();
        }
        OSSClient ossClient = new OSSClient(config.getString("oss.endpoint"), config.getString("oss.accessKeyId"), config.getString("oss.accessKeySecret"));
        ossClient.putObject(config.getString("oss.bucketName"), fileName, file);
        ossClient.shutdown();
        if(isdel == true)
        {
            file.delete();
        }
        return file.getName();
    }

    /**
     * 上传文件
     * @param filePath 本地文件路径
     * @param isdel 上传完后是否删除(true 删除 false 不删除)
     * @return 文件名
     */
    public String uploadFile(String filePath, boolean isdel)
    {
        return uploadFile(filePath,null,true);
    }
}
