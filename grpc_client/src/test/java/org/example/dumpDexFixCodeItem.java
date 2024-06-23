package org.example;

import com.kone.pbdemo.protocol.DumpClassInfo;
import com.kone.pbdemo.protocol.DumpMethodInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.dumpData.FixDumpClassCodeItem;
import org.example.dumpData.FixDumpMethodCodeItem;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class dumpDexFixCodeItem {

    public static void main(String[] args) {


        String host = "10.1.2.242";
        String fix_file = "D:\\apk\\dumpdex\\ccb_MainActivity.fixdata";   //pc dump fix path
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);

        String [] clsList = {
//                "com.hepta.androidgrpc.GrpcServiceImpl",
//                "com.hepta.androidgrpc.dump",
//                "com.hepta.androidgrpc.JNISignatureConverter",
//                "com.hepta.androidgrpc.LoadEntry",
//                "com.hepta.androidgrpc.MainActivity",
//                "com.yunmai.valueoflife.MainActivity$a",
//                "com.yunmai.valueoflife.StartActivity$c",
//                "com.yunmai.valueoflife.StartActivity$b",
//                "com.yunmai.valueoflife.StartActivity$e",
//                "com.yunmai.valueoflife.activity.AiDouVideoActivity"
                "com.ccb.start.MainActivity"
//                "com.PEP.biaori.activity.SplashActivity"
        };


        Map<String, FixDumpClassCodeItem> dumpClassCodeItemList = new HashMap<>();
        for(String clsName : clsList){
            String classTypeString = JNISignatureConverter.ClassNameToJNISigner(clsName);
            DumpClassInfo dumpClassList = service.dumpClass(clsName);

            Map<String, FixDumpMethodCodeItem> methodCodeItems = new HashMap<>();
            for(DumpMethodInfo dumpMethodInfo:dumpClassList.getDumpMethodInfoList()){
                String MethodName = dumpMethodInfo.getMethodName();
                String MethodSign = dumpMethodInfo.getMethodSign();
                String MethodString = MethodName+MethodSign;
                FixDumpMethodCodeItem fixDumpMethodCodeItem = new FixDumpMethodCodeItem(dumpMethodInfo.getContent().toByteArray());
                methodCodeItems.put(MethodString,fixDumpMethodCodeItem);
            }
            FixDumpClassCodeItem fixDumpClassCodeItem = new FixDumpClassCodeItem(methodCodeItems);
            dumpClassCodeItemList.put(classTypeString,fixDumpClassCodeItem);
        }

        try {
            File fix_path = new File(fix_file);
            if (!fix_path.exists()) {
                if (!fix_path.getParentFile().exists()) {
                    fix_path.getParentFile().mkdirs();
                }
            }
            FileOutputStream fileOut = new FileOutputStream(fix_path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(dumpClassCodeItemList);
            out.close();
            fileOut.close();
            System.out.println("Map serialized successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
