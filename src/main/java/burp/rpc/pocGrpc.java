package burp.rpc;

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
 * <pre>
 * `service` 是用来给gRPC服务定义方法的, 格式固定, 类似于Golang中定义一个接口
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.32.1)",
    comments = "Source: scan.proto")
public final class pocGrpc {

  private pocGrpc() {}

  public static final String SERVICE_NAME = "burp.rpc.poc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<burp.rpc.Poc_Request,
      burp.rpc.Poc_Response> getPocscanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "pocscan",
      requestType = burp.rpc.Poc_Request.class,
      responseType = burp.rpc.Poc_Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<burp.rpc.Poc_Request,
      burp.rpc.Poc_Response> getPocscanMethod() {
    io.grpc.MethodDescriptor<burp.rpc.Poc_Request, burp.rpc.Poc_Response> getPocscanMethod;
    if ((getPocscanMethod = pocGrpc.getPocscanMethod) == null) {
      synchronized (pocGrpc.class) {
        if ((getPocscanMethod = pocGrpc.getPocscanMethod) == null) {
          pocGrpc.getPocscanMethod = getPocscanMethod =
              io.grpc.MethodDescriptor.<burp.rpc.Poc_Request, burp.rpc.Poc_Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "pocscan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  burp.rpc.Poc_Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  burp.rpc.Poc_Response.getDefaultInstance()))
              .setSchemaDescriptor(new pocMethodDescriptorSupplier("pocscan"))
              .build();
        }
      }
    }
    return getPocscanMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static pocStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<pocStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<pocStub>() {
        @java.lang.Override
        public pocStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new pocStub(channel, callOptions);
        }
      };
    return pocStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static pocBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<pocBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<pocBlockingStub>() {
        @java.lang.Override
        public pocBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new pocBlockingStub(channel, callOptions);
        }
      };
    return pocBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static pocFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<pocFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<pocFutureStub>() {
        @java.lang.Override
        public pocFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new pocFutureStub(channel, callOptions);
        }
      };
    return pocFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * `service` 是用来给gRPC服务定义方法的, 格式固定, 类似于Golang中定义一个接口
   * </pre>
   */
  public static abstract class pocImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 一元模式(在一次调用中, 客户端只能向服务器传输一次请求数据, 服务器也只能返回一次响应)
     * </pre>
     */
    public void pocscan(burp.rpc.Poc_Request request,
        io.grpc.stub.StreamObserver<burp.rpc.Poc_Response> responseObserver) {
      asyncUnimplementedUnaryCall(getPocscanMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPocscanMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                burp.rpc.Poc_Request,
                burp.rpc.Poc_Response>(
                  this, METHODID_POCSCAN)))
          .build();
    }
  }

  /**
   * <pre>
   * `service` 是用来给gRPC服务定义方法的, 格式固定, 类似于Golang中定义一个接口
   * </pre>
   */
  public static final class pocStub extends io.grpc.stub.AbstractAsyncStub<pocStub> {
    private pocStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected pocStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new pocStub(channel, callOptions);
    }

    /**
     * <pre>
     * 一元模式(在一次调用中, 客户端只能向服务器传输一次请求数据, 服务器也只能返回一次响应)
     * </pre>
     */
    public void pocscan(burp.rpc.Poc_Request request,
        io.grpc.stub.StreamObserver<burp.rpc.Poc_Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPocscanMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * `service` 是用来给gRPC服务定义方法的, 格式固定, 类似于Golang中定义一个接口
   * </pre>
   */
  public static final class pocBlockingStub extends io.grpc.stub.AbstractBlockingStub<pocBlockingStub> {
    private pocBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected pocBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new pocBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 一元模式(在一次调用中, 客户端只能向服务器传输一次请求数据, 服务器也只能返回一次响应)
     * </pre>
     */
    public burp.rpc.Poc_Response pocscan(burp.rpc.Poc_Request request) {
      return blockingUnaryCall(
          getChannel(), getPocscanMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * `service` 是用来给gRPC服务定义方法的, 格式固定, 类似于Golang中定义一个接口
   * </pre>
   */
  public static final class pocFutureStub extends io.grpc.stub.AbstractFutureStub<pocFutureStub> {
    private pocFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected pocFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new pocFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 一元模式(在一次调用中, 客户端只能向服务器传输一次请求数据, 服务器也只能返回一次响应)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<burp.rpc.Poc_Response> pocscan(
        burp.rpc.Poc_Request request) {
      return futureUnaryCall(
          getChannel().newCall(getPocscanMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_POCSCAN = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final pocImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(pocImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_POCSCAN:
          serviceImpl.pocscan((burp.rpc.Poc_Request) request,
              (io.grpc.stub.StreamObserver<burp.rpc.Poc_Response>) responseObserver);
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

  private static abstract class pocBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    pocBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return burp.rpc.Scan.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("poc");
    }
  }

  private static final class pocFileDescriptorSupplier
      extends pocBaseDescriptorSupplier {
    pocFileDescriptorSupplier() {}
  }

  private static final class pocMethodDescriptorSupplier
      extends pocBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    pocMethodDescriptorSupplier(String methodName) {
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
      synchronized (pocGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new pocFileDescriptorSupplier())
              .addMethod(getPocscanMethod())
              .build();
        }
      }
    }
    return result;
  }
}
