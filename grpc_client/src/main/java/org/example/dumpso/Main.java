package org.example.dumpso;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.GrpcService;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    public GrpcService service;

    public String dumpWork ; ;
    String OutDexDir;
    ManagedChannel channel ;
    static String host;
    static int port;
    int jobs;

    Main(){
        host = "192.168.11.106";
        port = 9091;
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        service = new GrpcService(channel);
        String currentDir = System.getProperty("user.dir");
        dumpWork = currentDir+"/dumpWork";
        System.out.println(dumpWork);

        long  addr = 0x6e7b403000L;
        long end = 0x6e7b5ee000L;

        byte[] data = service.dumpMemByaddr(addr,end-addr);


        Path filePath = Paths.get(dumpWork, "libpdd_secure.so"); // 构建文件路径
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data, StandardOpenOption.CREATE);
            System.out.println("    dumpdex successfule To path -> : "+filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("    dex dump erroe: " + e.getMessage());
        }


    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        Main main = new Main();

    }

}
