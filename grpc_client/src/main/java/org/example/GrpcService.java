package org.example;

import com.google.protobuf.ByteString;
import hepta.dump.protocol.*;
import io.grpc.ManagedChannel;
import org.jf.baksmali.fix.FixClassCall;
import org.jf.baksmali.fix.FixDumpClassCodeItem;
import org.jf.baksmali.fix.FixDumpMethodCodeItem;
import org.jf.baksmali.fix.FixMain;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GrpcService {

    int kOatFileIndex = 0;
    int kDexFileIndexStart = 1;
    UserServiceGrpc.UserServiceBlockingStub  iServerInface;

    public GrpcService(ManagedChannel channel){
        iServerInface = UserServiceGrpc.newBlockingStub(channel);
    }

    void dumpdex(){
        Empty empty = Empty.newBuilder().build();
        iServerInface.dexDumpToLocal(empty);
    }

    public DumpClassInfo dumpClass(String className) {
        StringArgument classNameArg = StringArgument.newBuilder().setStringContent(className).build();
        DumpClassInfo dumpClassList = iServerInface.dumpClass(classNameArg).next();
        if (dumpClassList.getStatus()) {
            return dumpClassList;
        }
        return null;
    }


    public String getCurrentPackageName(){
        Empty empty = Empty.newBuilder().build();
        StringArgument stringArgument = iServerInface.getCurrentPackageName(empty);
        return stringArgument.getStringContent();
    }
    byte[] dumpDexMethod(String className,String MethodName,String MethodSign){
        DumpMethodString.Builder dumpMethod = DumpMethodString.newBuilder();
        dumpMethod.setClassName(className);
        dumpMethod.setMethodName(MethodName);
        dumpMethod.setMethodSign(MethodSign);
        MEMbuff buff = iServerInface.dumpMethod(dumpMethod.build());
        return buff.getContent().toByteArray();
    }

    public DexClassLoaderInfo getClassLoaderInfo(String className){
        StringArgument classNameArg = StringArgument.newBuilder().setStringContent(className).build();
        return  iServerInface.getDexClassLoaderInfoByClass(classNameArg);
    }
    public DexClassLoaders getDexClassLoaderList() {

        Empty empty = Empty.newBuilder().build();
        return  iServerInface.getDexClassLoaderList(empty);
    }

    public byte[] dexDumpByDexFilePoint(long DexFile_Point) {

        DexFilePoint dexFilePoint = DexFilePoint.newBuilder().setValues(DexFile_Point).build();
        MEMbuff buff = iServerInface.dexDumpByDexFilePoint(dexFilePoint).next();
        ByteString data =  buff.getContent();
        return  data.toByteArray();
    }


    public void OnlyDumpDexFile(String Dir){

        DexClassLoaders dexInfoList =  getDexClassLoaderList();
        for(DexClassLoaderInfo dexInfo : dexInfoList.getDexClassLoadInfoList()){
            System.out.println("android dex class path: "+dexInfo.getDexpath());
            if(dexInfo.getDexpath().contains("/system_ext")){
                System.out.println("system lib and continue");
                continue;
            }
            System.out.println("android dex class type: "+dexInfo.getClassLoadType());
            long[] CookieList = dexInfo.getValuesList().stream().mapToLong(Long::longValue).toArray();
            for(int i = kDexFileIndexStart;i<CookieList.length;++i){
                long DexFile_Point = CookieList[i];
                byte[] data = dexDumpByDexFilePoint(DexFile_Point);

                String fileName = Long.toHexString(DexFile_Point);
                Path filePath = Paths.get(Dir, fileName+".dex"); // 构建文件路径
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, data, StandardOpenOption.CREATE);
                    System.out.println("    dumpdex successfule To path -> : "+filePath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("    dex dump erroe: " + e.getMessage());
                }
            }
        }
    }



}
