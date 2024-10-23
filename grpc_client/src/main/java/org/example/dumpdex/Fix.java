package org.example.dumpdex;

import hepta.dump.protocol.DumpClassInfo;
import hepta.dump.protocol.DumpMethodInfo;
import org.jf.baksmali.fix.FixDumpClassCodeItem;
import org.jf.baksmali.fix.FixDumpMethodCodeItem;
import org.jf.baksmali.fix.FixMain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Fix {

    public void FixDexClass(String DexInput,String Smaliout, DumpClassInfo dumpClassList,String className){
        Map<String, FixDumpClassCodeItem> dumpClassCodeItemList = new HashMap<>();
        if(dumpClassList != null) {
            Map<String, FixDumpMethodCodeItem> methodCodeItems = new HashMap<>();
            for(DumpMethodInfo dumpMethodInfo:dumpClassList.getDumpMethodInfoList()){
                String MethodName = dumpMethodInfo.getMethodName();
                String MethodSign = dumpMethodInfo.getMethodSign();
                String MethodString = MethodName+MethodSign;
                FixDumpMethodCodeItem fixDumpMethodCodeItem = new FixDumpMethodCodeItem(dumpMethodInfo.toByteArray());
                methodCodeItems.put(MethodString,fixDumpMethodCodeItem);
            }
//            FixDumpClassCodeItem fixDumpClassCodeItem = new FixDumpClassCodeItem(methodCodeItems);
//            String classTypeString = JNISignatureConverter.ClassNameToJNISigner(className);
//            dumpClassCodeItemList.put(classTypeString,fixDumpClassCodeItem);
//            FixMain fixMain = new FixMain();
//            fixMain.Main1(DexInput,Smaliout,16,null,dumpClassCodeItemList);
        }
    }

//    public void FixDirDexList(String DexDir,Map<String, FixDumpClassCodeItem> dumpClassCodeItemList){
//        File directory = new File(DexDir);
//        File[] files = directory.listFiles();
//        // 遍历并打印所有文件名
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile() && file.getName().toLowerCase().endsWith(".dex")) {
//                    FixMain fixMain = new FixMain();
//                    fixMain.Main1(file.getAbsolutePath(),file.getName(),16,null,dumpClassCodeItemList);
//                }
//            }
//        }
//
//    }

}
