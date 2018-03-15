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
public final class ExcelParserGrpc {

  private ExcelParserGrpc() {}

  public static final String SERVICE_NAME = "ExcelParser";

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
    if ((getParseMethod = ExcelParserGrpc.getParseMethod) == null) {
      synchronized (ExcelParserGrpc.class) {
        if ((getParseMethod = ExcelParserGrpc.getParseMethod) == null) {
          ExcelParserGrpc.getParseMethod = getParseMethod =
                  io.grpc.MethodDescriptor.<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>newBuilder()
                          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                          .setFullMethodName(generateFullMethodName(
                                  "ExcelParser", "Parse"))
                          .setSampledToLocalTracing(true)
                          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest.getDefaultInstance()))
                          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger.getDefaultInstance()))
                          .setSchemaDescriptor(new ExcelParserMethodDescriptorSupplier("Parse"))
                          .build();
        }
      }
    }
    return getParseMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getAddQuoteResultMethod()} instead.
  public static final io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> METHOD_ADD_QUOTE_RESULT = getAddQuoteResultMethod();

  private static volatile io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getAddQuoteResultMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getAddQuoteResultMethod() {
    io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getAddQuoteResultMethod;
    if ((getAddQuoteResultMethod = ExcelParserGrpc.getAddQuoteResultMethod) == null) {
      synchronized (ExcelParserGrpc.class) {
        if ((getAddQuoteResultMethod = ExcelParserGrpc.getAddQuoteResultMethod) == null) {
          ExcelParserGrpc.getAddQuoteResultMethod = getAddQuoteResultMethod =
                  io.grpc.MethodDescriptor.<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>newBuilder()
                          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                          .setFullMethodName(generateFullMethodName(
                                  "ExcelParser", "AddQuoteResult"))
                          .setSampledToLocalTracing(true)
                          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult.getDefaultInstance()))
                          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger.getDefaultInstance()))
                          .setSchemaDescriptor(new ExcelParserMethodDescriptorSupplier("AddQuoteResult"))
                          .build();
        }
      }
    }
    return getAddQuoteResultMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetQuoteResultMethod()} instead.
  public static final io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> METHOD_GET_QUOTE_RESULT = getGetQuoteResultMethod();

  private static volatile io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getGetQuoteResultMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getGetQuoteResultMethod() {
    io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID, com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getGetQuoteResultMethod;
    if ((getGetQuoteResultMethod = ExcelParserGrpc.getGetQuoteResultMethod) == null) {
      synchronized (ExcelParserGrpc.class) {
        if ((getGetQuoteResultMethod = ExcelParserGrpc.getGetQuoteResultMethod) == null) {
          ExcelParserGrpc.getGetQuoteResultMethod = getGetQuoteResultMethod =
                  io.grpc.MethodDescriptor.<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID, com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult>newBuilder()
                          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                          .setFullMethodName(generateFullMethodName(
                                  "ExcelParser", "GetQuoteResult"))
                          .setSampledToLocalTracing(true)
                          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.UID.getDefaultInstance()))
                          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult.getDefaultInstance()))
                          .setSchemaDescriptor(new ExcelParserMethodDescriptorSupplier("GetQuoteResult"))
                          .build();
        }
      }
    }
    return getGetQuoteResultMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getAddResultMethod()} instead.
  public static final io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> METHOD_ADD_RESULT = getAddResultMethod();

  private static volatile io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getAddResultMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getAddResultMethod() {
    io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> getAddResultMethod;
    if ((getAddResultMethod = ExcelParserGrpc.getAddResultMethod) == null) {
      synchronized (ExcelParserGrpc.class) {
        if ((getAddResultMethod = ExcelParserGrpc.getAddResultMethod) == null) {
          ExcelParserGrpc.getAddResultMethod = getAddResultMethod =
                  io.grpc.MethodDescriptor.<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult, com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>newBuilder()
                          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                          .setFullMethodName(generateFullMethodName(
                                  "ExcelParser", "AddResult"))
                          .setSampledToLocalTracing(true)
                          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult.getDefaultInstance()))
                          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger.getDefaultInstance()))
                          .setSchemaDescriptor(new ExcelParserMethodDescriptorSupplier("AddResult"))
                          .build();
        }
      }
    }
    return getAddResultMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetResultMethod()} instead.
  public static final io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> METHOD_GET_RESULT = getGetResultMethod();

  private static volatile io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getGetResultMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
          com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getGetResultMethod() {
    io.grpc.MethodDescriptor<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID, com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getGetResultMethod;
    if ((getGetResultMethod = ExcelParserGrpc.getGetResultMethod) == null) {
      synchronized (ExcelParserGrpc.class) {
        if ((getGetResultMethod = ExcelParserGrpc.getGetResultMethod) == null) {
          ExcelParserGrpc.getGetResultMethod = getGetResultMethod =
                  io.grpc.MethodDescriptor.<com.attackt.logivisual.protobuf.ExcelFileTransfer.UID, com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult>newBuilder()
                          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                          .setFullMethodName(generateFullMethodName(
                                  "ExcelParser", "GetResult"))
                          .setSampledToLocalTracing(true)
                          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.UID.getDefaultInstance()))
                          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                  com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult.getDefaultInstance()))
                          .setSchemaDescriptor(new ExcelParserMethodDescriptorSupplier("GetResult"))
                          .build();
        }
      }
    }
    return getGetResultMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ExcelParserStub newStub(io.grpc.Channel channel) {
    return new ExcelParserStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ExcelParserBlockingStub newBlockingStub(
          io.grpc.Channel channel) {
    return new ExcelParserBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ExcelParserFutureStub newFutureStub(
          io.grpc.Channel channel) {
    return new ExcelParserFutureStub(channel);
  }

  /**
   */
  public static abstract class ExcelParserImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Python
     * </pre>
     */
    public void parse(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request,
                      io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnimplementedUnaryCall(getParseMethod(), responseObserver);
    }

    /**
     */
    public void addQuoteResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request,
                               io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnimplementedUnaryCall(getAddQuoteResultMethod(), responseObserver);
    }

    /**
     */
    public void getQuoteResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request,
                               io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> responseObserver) {
      asyncUnimplementedUnaryCall(getGetQuoteResultMethod(), responseObserver);
    }

    /**
     */
    public void addResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request,
                          io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnimplementedUnaryCall(getAddResultMethod(), responseObserver);
    }

    /**
     */
    public void getResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request,
                          io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> responseObserver) {
      asyncUnimplementedUnaryCall(getGetResultMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
              .addMethod(
                      getParseMethod(),
                      asyncUnaryCall(
                              new MethodHandlers<
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest,
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>(
                                      this, METHODID_PARSE)))
              .addMethod(
                      getAddQuoteResultMethod(),
                      asyncUnaryCall(
                              new MethodHandlers<
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>(
                                      this, METHODID_ADD_QUOTE_RESULT)))
              .addMethod(
                      getGetQuoteResultMethod(),
                      asyncUnaryCall(
                              new MethodHandlers<
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult>(
                                      this, METHODID_GET_QUOTE_RESULT)))
              .addMethod(
                      getAddResultMethod(),
                      asyncUnaryCall(
                              new MethodHandlers<
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult,
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>(
                                      this, METHODID_ADD_RESULT)))
              .addMethod(
                      getGetResultMethod(),
                      asyncUnaryCall(
                              new MethodHandlers<
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.UID,
                                      com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult>(
                                      this, METHODID_GET_RESULT)))
              .build();
    }
  }

  /**
   */
  public static final class ExcelParserStub extends io.grpc.stub.AbstractStub<ExcelParserStub> {
    private ExcelParserStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ExcelParserStub(io.grpc.Channel channel,
                            io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ExcelParserStub build(io.grpc.Channel channel,
                                    io.grpc.CallOptions callOptions) {
      return new ExcelParserStub(channel, callOptions);
    }

    /**
     * <pre>
     * Python
     * </pre>
     */
    public void parse(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request,
                      io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnaryCall(
              getChannel().newCall(getParseMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addQuoteResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request,
                               io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnaryCall(
              getChannel().newCall(getAddQuoteResultMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getQuoteResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request,
                               io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> responseObserver) {
      asyncUnaryCall(
              getChannel().newCall(getGetQuoteResultMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request,
                          io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> responseObserver) {
      asyncUnaryCall(
              getChannel().newCall(getAddResultMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request,
                          io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> responseObserver) {
      asyncUnaryCall(
              getChannel().newCall(getGetResultMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ExcelParserBlockingStub extends io.grpc.stub.AbstractStub<ExcelParserBlockingStub> {
    private ExcelParserBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ExcelParserBlockingStub(io.grpc.Channel channel,
                                    io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ExcelParserBlockingStub build(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
      return new ExcelParserBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Python
     * </pre>
     */
    public com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger parse(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request) {
      return blockingUnaryCall(
              getChannel(), getParseMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger addQuoteResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request) {
      return blockingUnaryCall(
              getChannel(), getAddQuoteResultMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult getQuoteResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request) {
      return blockingUnaryCall(
              getChannel(), getGetQuoteResultMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger addResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request) {
      return blockingUnaryCall(
              getChannel(), getAddResultMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult getResult(com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request) {
      return blockingUnaryCall(
              getChannel(), getGetResultMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ExcelParserFutureStub extends io.grpc.stub.AbstractStub<ExcelParserFutureStub> {
    private ExcelParserFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ExcelParserFutureStub(io.grpc.Channel channel,
                                  io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ExcelParserFutureStub build(io.grpc.Channel channel,
                                          io.grpc.CallOptions callOptions) {
      return new ExcelParserFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Python
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> parse(
            com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest request) {
      return futureUnaryCall(
              getChannel().newCall(getParseMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> addQuoteResult(
            com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request) {
      return futureUnaryCall(
              getChannel().newCall(getAddQuoteResultMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getQuoteResult(
            com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request) {
      return futureUnaryCall(
              getChannel().newCall(getGetQuoteResultMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger> addResult(
            com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult request) {
      return futureUnaryCall(
              getChannel().newCall(getAddResultMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult> getResult(
            com.attackt.logivisual.protobuf.ExcelFileTransfer.UID request) {
      return futureUnaryCall(
              getChannel().newCall(getGetResultMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PARSE = 0;
  private static final int METHODID_ADD_QUOTE_RESULT = 1;
  private static final int METHODID_GET_QUOTE_RESULT = 2;
  private static final int METHODID_ADD_RESULT = 3;
  private static final int METHODID_GET_RESULT = 4;

  private static final class MethodHandlers<Req, Resp> implements
          io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
          io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
          io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
          io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ExcelParserImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ExcelParserImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PARSE:
          serviceImpl.parse((com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseRequest) request,
                  (io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>) responseObserver);
          break;
        case METHODID_ADD_QUOTE_RESULT:
          serviceImpl.addQuoteResult((com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult) request,
                  (io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>) responseObserver);
          break;
        case METHODID_GET_QUOTE_RESULT:
          serviceImpl.getQuoteResult((com.attackt.logivisual.protobuf.ExcelFileTransfer.UID) request,
                  (io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult>) responseObserver);
          break;
        case METHODID_ADD_RESULT:
          serviceImpl.addResult((com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult) request,
                  (io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.Roger>) responseObserver);
          break;
        case METHODID_GET_RESULT:
          serviceImpl.getResult((com.attackt.logivisual.protobuf.ExcelFileTransfer.UID) request,
                  (io.grpc.stub.StreamObserver<com.attackt.logivisual.protobuf.ExcelFileTransfer.ParseResult>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
            io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ExcelParserBaseDescriptorSupplier
          implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ExcelParserBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.attackt.logivisual.protobuf.ExcelFileTransfer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ExcelParser");
    }
  }

  private static final class ExcelParserFileDescriptorSupplier
          extends ExcelParserBaseDescriptorSupplier {
    ExcelParserFileDescriptorSupplier() {}
  }

  private static final class ExcelParserMethodDescriptorSupplier
          extends ExcelParserBaseDescriptorSupplier
          implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ExcelParserMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ExcelParserGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                  .setSchemaDescriptor(new ExcelParserFileDescriptorSupplier())
                  .addMethod(getParseMethod())
                  .addMethod(getAddQuoteResultMethod())
                  .addMethod(getGetQuoteResultMethod())
                  .addMethod(getAddResultMethod())
                  .addMethod(getGetResultMethod())
                  .build();
        }
      }
    }
    return result;
  }
}
