package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jf.baksmali.fix.FixClassCall;
import org.jf.baksmali.fix.FixDumpClassCodeItem;
import org.jf.baksmali.fix.FixDumpMethodCodeItem;
import org.jf.baksmali.fix.FixMain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public  static  int kDexFileIndexStart = 1;


    public static void  DownloadFile(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub, long[] cookie, String outDir)
    {
//        DownloadFileRequest  downloadRequest =  DownloadFileRequest.newBuilder().setCookie(cookie).build();
////        downloadRequest.getFilePath()
//        userServiceBlockingStub.downloadFile(downloadRequest);

    }

    public static void main(String[] args) throws InterruptedException {


        String host = "192.168.12.104";
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);
        String dir = "D:\\apk\\dumpdex";
//        JsonObject jsonObject = JsonParser.parseString("D:\\androidGRPC\\grpc_client\\Filter.json").getAsJsonObject();

//        dumpDexByClass(service,dir,"com.hepta.androidgrpc.AndroidClassLoaderInfo");
//        dumpDexByClass(service,dir,"com.yunmai.valueoflife.MainActivity");
//        dumpWholeDexFileAndFix(service,dir);
//        OnlyDumpDexFile(service,dir);
        service.OnlyDumpDexFile(dir);
        Thread.sleep(2000);
        channel.shutdown();
    }

    private static void OnlyDumpDexFile(GrpcService service, String dir) {
        service.OnlyDumpDexFile(dir);
    }


    public static void dumpWholeDexFileAndFix(GrpcService service,String Dir) {

        DexClassLoaders dexInfoList =  service.getDexClassLoaderList();
        for(DexClassLoaderInfo dexInfo : dexInfoList.getDexClassLoadInfoList()) {
            DumpdexListByDexDexClassLoaderAndFix(service,Dir,dexInfo);
        }

    }
    public static void dumpDexByClassAndFix(GrpcService service,String Dir,String cls_name){
        DexClassLoaderInfo dexClassLoaderInfo = service.getClassLoaderInfo(cls_name);
        if(dexClassLoaderInfo.getStatus()){
            DumpdexListByDexDexClassLoaderAndFix(service,Dir,dexClassLoaderInfo);
        }else {
            System.err.println(dexClassLoaderInfo.getMsg());
        }

    }


    public static void DumpdexListByDexDexClassLoaderAndFix(GrpcService service,String Dir,DexClassLoaderInfo dexInfo) {
        System.out.println("android dex class path: "+dexInfo.getDexpath());
        if(dexInfo.getDexpath().contains("/system_ext")){
            System.out.println("system lib and continue");
            return;
        }
        System.out.println("android dex class type: "+dexInfo.getClassLoadType());
        long[] CookieList = dexInfo.getValuesList().stream().mapToLong(Long::longValue).toArray();
        for(int i = kDexFileIndexStart;i<CookieList.length;++i) {
            long DexFile_Point = CookieList[i];
            DumpdexByDexFilePointAndFix(service,Dir,DexFile_Point);
        }

    }
    public static void DumpdexByDexFilePointAndFix(GrpcService service,String Dir, long DexFilePoint){

        FixMain fixMain = new FixMain();
        String file_name = Long.toHexString(DexFilePoint);
        String smail_out = Dir + "/" + file_name;
        byte[] data = service.dexDumpByDexFilePoint(DexFilePoint);


        Path filePath = Paths.get(Dir, file_name+".dex"); // 构建文件路径
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data, StandardOpenOption.CREATE);
            System.out.println("    dumpdex successfule To path -> : "+filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("    dex dump erroe: " + e.getMessage());
        }

        fixMain.Main1(filePath.toString(), smail_out, 1, new FixClassCall() {
            @Override
            public FixDumpClassCodeItem ClassFixCall(String clsName) {
                String java_cls_name = SignatureConverter.JNISignerToClassName(clsName);
                System.out.println("\""+java_cls_name+"\",");
                DumpClassInfo dumpClassInfo = service.dumpClass(java_cls_name);
                if (dumpClassInfo != null) {
                    Map<String, FixDumpMethodCodeItem> methodCodeItemList =new HashMap<>();
                    for(DumpMethodInfo dumpmethodInfo:dumpClassInfo.getDumpMethodInfoList()){
                        if(dumpmethodInfo.getStatus()){
                            methodCodeItemList.put(dumpmethodInfo.getMethodName(),new FixDumpMethodCodeItem(dumpmethodInfo.getContent().toByteArray()));
                        }
                    }
                    return new FixDumpClassCodeItem(methodCodeItemList,null);
                }
                return null;
            }
        });
        String[] smali_args = new String[4];
        smali_args[0] = "assemble";
        smali_args[1] = smail_out;
        smali_args[2] = "-o";
        smali_args[3] = smail_out + "_fix.dex";
        org.jf.smali.Main.main(smali_args);
    }


//    public static void dumpText(){
//        String host = "192.168.0.86";
//        int port = 9091;
//        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
//        GrpcService service = new GrpcService(channel);
//
//        String [] clsList = {
//                "com.hepta.androidgrpc.GrpcServiceImpl",
//                "com.hepta.androidgrpc.dump",
//                "com.hepta.androidgrpc.JNISignatureConverter",
//                "com.hepta.androidgrpc.LoadEntry",
//                "com.hepta.androidgrpc.MainActivity"
//        };
//
//        Map<String, FixDumpClassCodeItem> dumpClassCodeItemList = new HashMap<>();
//        for(String clsName : clsList){
//            String classTypeString = JNISignatureConverter.ClassNameToJNISigner(clsName);
//            DumpClassInfo dumpClassList = service.dumpClass(clsName);
//
//            Map<String, FixDumpMethodCodeItem> methodCodeItems = new HashMap<>();
//            for(DumpMethodInfo dumpMethodInfo:dumpClassList.getDumpMethodInfoList()){
//                String MethodName = dumpMethodInfo.getMethodName();
//                String MethodSign = dumpMethodInfo.getMethodSign();
//                String MethodString = MethodName+MethodSign;
//                FixDumpMethodCodeItem fixDumpMethodCodeItem = new FixDumpMethodCodeItem(dumpMethodInfo.toByteArray());
//                methodCodeItems.put(MethodString,fixDumpMethodCodeItem);
//            }
//            FixDumpClassCodeItem fixDumpClassCodeItem = new FixDumpClassCodeItem(methodCodeItems);
//            dumpClassCodeItemList.put(classTypeString,fixDumpClassCodeItem);
//
//        }
//    }

}