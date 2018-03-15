package com.attackt.logivisual.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.10.0-SNAPSHOT)",
        comments = "Source: proto/ExcelFileTransfer.proto")
public final class ExcelQuoteParseGrpc {

  private ExcelQuoteParseGrpc() {}

  public static final String SERVICE_NAME = "ExcelQuoteParse";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getParseMethod()} instead.
  public static final io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> METHOD_PARSE = getParseMethod();

  private static volatile io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getParseMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getParseMethod() {
    io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getParseMethod;
    if ((getParseMethod = ExcelQuoteParseGrpc.getParseMethod) == null) {
      synchronized (ExcelQuoteParseGrpc.class) {
        if ((getParseMethod = ExcelQuoteParseGrpc.getParseMethod) == null) {
          ExcelQuoteParseGrpc.getParseMethod = getParseMethod =
                  io.grpc.MethodDescriptor.<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>newBuilder()
                          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                          .setFullMethodName(generateFullMethodName(
                                  "ExcelQuoteParse", "Parse"))
                          .setSampledToLocalTracing(true)
                          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest.getDefaultInstance()))
                          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger.getDefaultInstance()))
                          .setSchemaDescriptor(new ExcelQuoteParseMethodDescriptorSupplier("Parse"))
                          .build();
        }
      }
    }
    return getParseMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ExcelQuoteParseStub newStub(io.grpc.Channel channel) {
    return new ExcelQuoteParseStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ExcelQuoteParseBlockingStub newBlockingStub(
          io.grpc.Channel channel) {
    return new ExcelQuoteParseBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ExcelQuoteParseFutureStub newFutureStub(
          io.grpc.Channel channel) {
    return new ExcelQuoteParseFutureStub(channel);
  }

  /**
   */
  public static abstract class ExcelQuoteParseImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * java
     * </pre>
     */
    public void parse(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request,
                      io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnimplementedUnaryCall(getParseMethod(), responseObserver);
    }

    public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
              .addMethod(
                      getParseMethod(),
                      asyncUnaryCall(
                              new MethodHandlers<
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest,
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>(
                                      this, METHODID_PARSE)))
              .build();
    }
  }

  /**
   */
  public static final class ExcelQuoteParseStub extends io.grpc.stub.AbstractStub<ExcelQuoteParseStub> {
    private ExcelQuoteParseStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ExcelQuoteParseStub(io.grpc.Channel channel,
                                io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ExcelQuoteParseStub build(io.grpc.Channel channel,
                                        io.grpc.CallOptions callOptions) {
      return new ExcelQuoteParseStub(channel, callOptions);
    }

    /**
     * <pre>
     * java
     * </pre>
     */
    public void parse(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request,
                      io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnaryCall(
              getChannel().newCall(getParseMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ExcelQuoteParseBlockingStub extends io.grpc.stub.AbstractStub<ExcelQuoteParseBlockingStub> {
    private ExcelQuoteParseBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ExcelQuoteParseBlockingStub(io.grpc.Channel channel,
                                        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ExcelQuoteParseBlockingStub build(io.grpc.Channel channel,
                                                io.grpc.CallOptions callOptions) {
      return new ExcelQuoteParseBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * java
     * </pre>
     */
    public com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger parse(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request) {
      return blockingUnaryCall(
              getChannel(), getParseMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ExcelQuoteParseFutureStub extends io.grpc.stub.AbstractStub<ExcelQuoteParseFutureStub> {
    private ExcelQuoteParseFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ExcelQuoteParseFutureStub(io.grpc.Channel channel,
                                      io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ExcelQuoteParseFutureStub build(io.grpc.Channel channel,
                                              io.grpc.CallOptions callOptions) {
      return new ExcelQuoteParseFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * java
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> parse(
            com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request) {
      return futureUnaryCall(
              getChannel().newCall(getParseMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PARSE = 0;

  private static final class MethodHandlers<Req, Resp> implements
          io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
          io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
          io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
          io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ExcelQuoteParseImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ExcelQuoteParseImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PARSE:
          serviceImpl.parse((com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest) request,
                  (io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
            io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ExcelQuoteParseBaseDescriptorSupplier
          implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ExcelQuoteParseBaseDescriptorSupplier() {}

    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.attackt.logivisual.protobuf.ExcelFileTransfer.getDescriptor();
    }

    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ExcelQuoteParse");
    }
  }

  private static final class ExcelQuoteParseFileDescriptorSupplier
          extends ExcelQuoteParseBaseDescriptorSupplier {
    ExcelQuoteParseFileDescriptorSupplier() {}
  }

  private static final class ExcelQuoteParseMethodDescriptorSupplier
          extends ExcelQuoteParseBaseDescriptorSupplier
          implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ExcelQuoteParseMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ExcelQuoteParseGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                  .setSchemaDescriptor(new ExcelQuoteParseFileDescriptorSupplier())
                  .addMethod(getParseMethod())
                  .build();
        }
      }
    }
    return result;
  }
}
