package com.attackt.logivisual.protobuf.client;

import com.attackt.logivisual.protobuf.ExcelFileTransfer;
import com.attackt.logivisual.protobuf.ExcelParserGrpc;
import com.attackt.logivisual.protobuf.ExcelQuoteParseGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RPC发送文件地址实现类
 */
public class RPCExcelParserClient {
    private final Logger logger = Logger.getLogger(RPCExcelParserClient.class.getName());

    private final ManagedChannel channel;
    private final ExcelParserGrpc.ExcelParserBlockingStub blockingStub;

    public RPCExcelParserClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true)
                .build();
        blockingStub = ExcelParserGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // 调用服务端方法
    /** Say hello to server. */
    public void sendJsonFilePath(ExcelFileTransfer.ParseResult request) {
        ExcelFileTransfer.Roger response;
        try {
            response = blockingStub.addQuoteResult(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Result Roger msg: " + response.getMsg());
    }

    public void sendJsonFileParent(String  uid,String path, int access){
        ExcelFileTransfer.ParseResult request = ExcelFileTransfer
                .ParseResult.newBuilder()
                .setUid(uid)
                .setPath(path)
                .setAccess(access)
                .build();
        this.sendJsonFilePath(request);
    }
}
