package com.attackt.logivisual.utils;

import com.attackt.logivisual.protobuf.thread.RPCExcelQuotoParseThread;

/**
 * 线程通用类
 */
public class ThreadUtil {
    /**
     * 获取excelUid
     * @return
     */
    public  String getExcelUid(){
        Thread thread = Thread.currentThread();
        if(thread instanceof RPCExcelQuotoParseThread)
        {
            RPCExcelQuotoParseThread rpcExcelQuotoParseThread = (RPCExcelQuotoParseThread) thread;
            return rpcExcelQuotoParseThread.getUid();
        }else{
            return "1";
        }
    }
}
