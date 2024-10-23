package org.example.dumpdex;


import hepta.dump.protocol.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.GrpcService;
import org.jf.baksmali.fix.*;
import org.jf.baksmali.fix.FixMain;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
    public    int kDexFileIndexStart = 1;
    public   JSONArray blockClassList;
    public   JSONArray whileClassList;

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
        blockClassList = jsonObject.getJSONArray("blockClass");
        //        whileClassList = jsonObject.getJSONArray("whileClass");
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        service = new GrpcService(channel);
        worDir = "D:\\apk\\dumpdex";
        OutDexDir = worDir+"\\"+service.getCurrentPackageName();
        jobs = 1;


    }
    public boolean containsClass( String cla_name) {

        for (int i = 0; i < blockClassList.length(); i++) {
            String jsonObject = String.valueOf(blockClassList.get(i));
            if (jsonObject.equals(cla_name)) {
                return true;
            }
        }
        return false;
    }


    public  void dumpEntry() throws FileNotFoundException, InterruptedException {


        dumpDexByClassAndFix(OutDexDir,"com.hepta.androidgrpc.AndroidClassLoaderInfo");
//        dumpDexByClassAndFix(OutDexDir,"com.yunmai.valueoflife.MainActivity");
//        dumpDexByClassAndFix(OutDexDir,"com.ccb.start.MainActivity");
//        dumpWholeDexFileAndFix(OutDexDir);
//        OnlyDumpDexFile(OutDexDir);

    }




    private  void OnlyDumpDexFile( String dir) {
        service.OnlyDumpDexFile(dir);
    }


    public  void dumpWholeDexFileAndFix(String Dir) {

        DexClassLoaders dexInfoList =  service.getDexClassLoaderList();
        for(DexClassLoaderInfo dexInfo : dexInfoList.getDexClassLoadInfoList()) {
            DumpdexListByDexDexClassLoaderAndFix(Dir,dexInfo);
        }

    }
    public  void dumpDexByClassAndFix(String Dir,String cls_name){
        DexClassLoaderInfo dexClassLoaderInfo = service.getClassLoaderInfo(cls_name);
        if(dexClassLoaderInfo.getStatus()){
            DumpdexListByDexDexClassLoaderAndFix(Dir,dexClassLoaderInfo);
        }else {
            System.err.println(dexClassLoaderInfo.getMsg());
        }

    }


    public  void DumpdexListByDexDexClassLoaderAndFix(String Dir,DexClassLoaderInfo dexInfo) {
        System.out.println("android dex class path: "+dexInfo.getDexpath());
        if(dexInfo.getDexpath().contains("/system_ext")){
            System.out.println("system lib and continue");
            return;
        }
        System.out.println("android dex class type: "+dexInfo.getClassLoadType());
        long[] CookieList = dexInfo.getValuesList().stream().mapToLong(Long::longValue).toArray();
        for(int i = kDexFileIndexStart;i<CookieList.length;++i) {
            long DexFile_Point = CookieList[i];
            DumpdexByDexFilePointAndFix(Dir,DexFile_Point);
        }

    }

    public void FixDexMethodCodeItem(String FixfileAbsPath){
        Path FixfilePath = Paths.get(FixfileAbsPath);
        int lastIndex  = FixfilePath.getFileName().toString().lastIndexOf(".");
        String file_name = FixfilePath.getFileName().toString().substring(0,lastIndex);
        String Dir = FixfilePath.getParent().toString();
        FixMain fixMain = new FixMain();
        String smail_out = Dir + "/" + file_name;
        try {

            fixMain.Main1(FixfilePath.toString(), smail_out, jobs, new FixClassCall() {
                @Override
                public FixDumpClassCodeItem ClassFixCall(String clsName) {
                    boolean exists = containsClass(clsName);
                    if (!exists) {
                        String java_cls_name = SignatureConverter.JNISignerToClassName(clsName);
                        System.out.println("\""+java_cls_name+"\",");
                        DumpClassInfo dumpClassInfo = service.dumpClass(java_cls_name);
                        if (dumpClassInfo != null) {
                            Map<String, FixDumpMethodCodeItem> methodCodeItemList =new HashMap<>();
                            for(DumpMethodInfo dumpmethodInfo:dumpClassInfo.getDumpMethodInfoList()){
                                if(dumpmethodInfo.getStatus()){
                                    String methodString = dumpmethodInfo.getMethodName()+dumpmethodInfo.getMethodSign();
                                    methodCodeItemList.put(methodString,new FixDumpMethodCodeItem(dumpmethodInfo.getContent().toByteArray()));
                                }
                            }
                            return new FixDumpClassCodeItem(methodCodeItemList,new FixMethodCall(methodCodeItemList){
                                public FixDumpMethodCodeItem MethodFixCall(String classDescriptor) {
                                    return (FixDumpMethodCodeItem)this.methodCodeItemList.get(classDescriptor);
                                }
                            });
                        }
                    }
                    return null;
                }
            });

        }catch (Exception e){
            System.err.println(e.getMessage());
        }


        buildFixSmaliDex(smail_out);
    }

    public void  buildFixSmaliDex(String smail_out){
        String[] smali_args = new String[4];
        smali_args[0] = "assemble";
        smali_args[1] = smail_out;
        smali_args[2] = "-o";
        smali_args[3] = smail_out + "_fix.dex";
        org.jf.smali.Main.main(smali_args);
    }

    public  void DumpdexByDexFilePointAndFix(String Dir, long DexFilePoint){


        String file_name = Long.toHexString(DexFilePoint);

        byte[] data = service.dexDumpByDexFilePoint(DexFilePoint);


        Path filePath = Paths.get(Dir, file_name+".dex"); // 构建文件路径
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data, StandardOpenOption.CREATE);
            System.out.println("    dumpdex successfule To path -> : "+filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("    dex dump erroe: " + e.getMessage());
        }

        FixDexMethodCodeItem(filePath.toString());

    }

    public void close(){
        channel.shutdown();
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        Main main = new Main();
        main.dumpEntry();
        main.close();
    }

}