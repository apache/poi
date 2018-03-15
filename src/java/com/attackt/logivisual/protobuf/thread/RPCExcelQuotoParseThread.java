package com.attackt.logivisual.protobuf.thread;

import com.attackt.logivisual.protobuf.client.RPCExcelParserClient;
import com.attackt.logivisual.utils.OSSUtil;
import com.attackt.logivisual.web.ExcelFormulaSplitTransferFuncNew;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * excel 文件
 */
public class RPCExcelQuotoParseThread extends Thread{
    private final Logger logger = Logger.getLogger(RPCExcelQuotoParseThread.class.getName());
    Config config = ConfigFactory.load();

    String uid;
    String filePath;
    String password;
    int accees;
    int threadIndex;

    public String getUid() {
        return uid;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPassword() {
        return password;
    }

    public int getAccees() {
        return accees;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    public  RPCExcelQuotoParseThread(int threadIndex, String uid, String filePath, String password, int accees){
        super();
        this.filePath=filePath;
        this.threadIndex = threadIndex;
        this.password=password;
        this.uid=uid;
        this.accees=accees;
    }
    public void run() {
        ExcelFormulaSplitTransferFuncNew excelFormulaSplitTransferFuncNew = new ExcelFormulaSplitTransferFuncNew();
        String fileJSONPath = null;

        try {
            fileJSONPath = excelFormulaSplitTransferFuncNew.converFile(filePath, password, accees,uid);
        } catch (IOException e) {
            System.err.println("------文件解析失败------");
            System.err.println(e);
            System.err.println(e);
        }catch (Exception e){
            System.err.println("------文件解析失败------");
            System.out.println(e);
        }
        // 上传文件
        if(accees == 2)
        {
            logger.info("上传文件开始");
            String fileName = new OSSUtil().uploadFile(fileJSONPath,true);
            logger.info("上传文件结束");
            fileJSONPath="http://"+config.getString("oss.bucketName")+"."+config.getString("oss.endpoint")+"/"+fileName;
            logger.info(fileJSONPath);
        }
        // 开始传递过程
        if(fileJSONPath !=null)
        {
            RPCExcelParserClient rpcExcelParserClient=new RPCExcelParserClient(config.getString("client.ip"),config.getInt("client.port"));
            try {
                rpcExcelParserClient.sendJsonFileParent(uid,fileJSONPath,accees);
                rpcExcelParserClient.shutdown();
            } catch (InterruptedException e) {
                System.err.println("------文件递送失败------");
                System.err.println(e);
                System.err.println(e);
            }
        }
    }
}
