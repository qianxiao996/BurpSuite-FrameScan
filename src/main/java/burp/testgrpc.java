package burp;

import burp.rpc.pocGrpc;
import burp.rpc.pocGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import burp.rpc.pocGrpc.pocBlockingStub;
import burp.rpc.Poc_Request;
import burp.rpc.Poc_Response;

public class testgrpc {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        pocGrpc.pocBlockingStub client = pocGrpc.newBlockingStub(channel);

        Poc_Request requests = Poc_Request.newBuilder()
                .setPocType("example")
                .setUrl("http://example.com")
                .setPoc("...")
                .build();

        Poc_Response response = client.pocscan(requests);
        System.out.println("扫描结果: " + response.getResult());
        System.out.println("是否成功: " + response.getIsSuccess());

        channel.shutdown();
    }
}