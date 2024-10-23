package org.example.dumpso;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.GrpcService;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {
    public GrpcService service;

    public String worDir ;
    String OutDexDir;
    ManagedChannel channel ;
    static String host;
    static int port;
    int jobs;

    Main(){
        host = "192.168.12.104";
        port = 9091;

        FileReader reader = null;
        try {
            reader = new FileReader("D:\\androidGRPC\\grpc_client\\Filter.json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
//        blockClassList = jsonObject.getJSONArray("blockClass");
//        //        whileClassList = jsonObject.getJSONArray("whileClass");
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        service = new GrpcService(channel);
//        worDir = "D:\\apk\\dumpdex";
//        OutDexDir = worDir+"\\"+service.getCurrentPackageName();
//        jobs = 1;


    }

}
