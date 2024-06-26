package org.example;

import com.google.protobuf.ByteString;
import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;
import org.jf.baksmali.fix.FixClassCall;
import org.jf.baksmali.fix.FixDumpClassCodeItem;
import org.jf.baksmali.fix.FixMain;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

public class GrpcService {

    int kOatFileIndex = 0;
    int kDexFileIndexStart = 1;
    UserServiceGrpc.UserServiceBlockingStub  iServerInface;

    GrpcService(ManagedChannel channel){
        iServerInface = UserServiceGrpc.newBlockingStub(channel);
    }

    void dumpdex(){
        Empty empty = Empty.newBuilder().build();
        iServerInface.dexDumpToLocal(empty);
    }

    DumpClassInfo dumpClass(String className) {
        StringArgument classNameArg = StringArgument.newBuilder().setClassName(className).build();
        DumpClassInfo dumpClassList = iServerInface.dumpClass(classNameArg).next();
        if (dumpClassList.getStatus()) {
            return dumpClassList;
        }
        return null;
    }

    byte[] dumpDexMethod(String className,String MethodName,String MethodSign){
        DumpMethodString.Builder dumpMethod = DumpMethodString.newBuilder();
        dumpMethod.setClassName(className);
        dumpMethod.setMethodName(MethodName);
        dumpMethod.setMethodSign(MethodSign);
        Dexbuff buff = iServerInface.dumpMethod(dumpMethod.build());
        return buff.getContent().toByteArray();
    }

    void dumpDexFile(String Dir){
        String[] smali_args = new String[4];
        smali_args[0] = "assemble";
        smali_args[3] = "-o";

        Empty empty = Empty.newBuilder().build();
        DexClassLoaders dexInfoList =  iServerInface.getDexClassLoaderList(empty);
        for(DexClassLoaderInfo dexInfo : dexInfoList.getDexClassLoadInfoList()){
            System.out.println("android dex class path: "+dexInfo.getDexpath());
            System.out.println("android dex class type: "+dexInfo.getClassLoadType());
            long[] CookieList = dexInfo.getValuesList().stream().mapToLong(Long::longValue).toArray();
            for(int i = kDexFileIndexStart;i<CookieList.length;++i){
                long DexFile_Point = CookieList[i];
                DexFilePoint dexFilePoint = DexFilePoint.newBuilder().setValues(DexFile_Point).build();
                Dexbuff buff = iServerInface.dexDumpByDexFilePoint(dexFilePoint).next();
                ByteString data =  buff.getContent();
                smali_args[1] = Long.toHexString(DexFile_Point);
                Path filePath = Paths.get(Dir, smali_args[1]+".dex"); // 构建文件路径
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, data.toByteArray(), StandardOpenOption.CREATE);
                    System.out.println("    dumpdex successfule To path -> : "+filePath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("    dex dump erroe: " + e.getMessage());
                }
                FixMain fixMain = new FixMain();

                fixMain.Main1(filePath.toString(), smali_args[1], 16, new FixClassCall() {
                    @Override
                    public FixDumpClassCodeItem ClassFixCall(String s) {
                        return null;
                    }
                });
                smali_args[4] = smali_args[1]+"_fix.dex";
                org.jf.smali.Main.main(smali_args);

            }
        }
    }
}
