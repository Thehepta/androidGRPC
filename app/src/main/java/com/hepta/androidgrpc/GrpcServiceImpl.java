package com.hepta.androidgrpc;

import static com.hepta.androidgrpc.dump.getDexBuffbyCookieLong;

import android.content.Context;
import android.util.Log;


import com.google.protobuf.ByteString;
import com.kone.pbdemo.protocol.DexFilePoint;
import com.kone.pbdemo.protocol.DexInfo;
import com.kone.pbdemo.protocol.DexInfoList;
import com.kone.pbdemo.protocol.DownloadFileRequest;
import com.kone.pbdemo.protocol.DownloadFileResponse;
import com.kone.pbdemo.protocol.DumpClassInfo;
import com.kone.pbdemo.protocol.DumpMethodInfo;
import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.StringArgument;
import com.kone.pbdemo.protocol.User;
import com.kone.pbdemo.protocol.UserServiceGrpc;
import com.kone.pbdemo.protocol.Dexbuff;
import com.kone.pbdemo.protocol.DumpMethodString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import io.grpc.stub.StreamObserver;


public class GrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {


    public Context context;
    public String source;
    public String argument;
    public ClassLoader [] classLoaders;
    public GrpcServiceImpl(Context ctx,String source, String argument){
        dump.Entry(ctx,source,argument);
        classLoaders = dump.getBaseDexClassLoaderList();
        context = ctx;
        this.source = source;
        this.argument = argument;
    }

    public Class<?> AndroidFindClass(String name){
        for (ClassLoader classLoader:classLoaders){
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {

            }
        }
        return null;
    }

    @Override
    public void dexDumpToLocal(Empty request, StreamObserver<Empty> responseObserver) {
        Log.e("rzx","dumpdex");
        dump.Entry(context,source,argument);
        dump.dumpdexToLocal(context);
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void dumpClass(StringArgument request, StreamObserver<DumpClassInfo> responseObserver) {
        String clsName = request.getClassName();
        DumpClassInfo.Builder classInfobuilder = DumpClassInfo.newBuilder();

        Log.e("rzx","dumpClass:"+clsName);
        Class<?> cls = null;
        for (ClassLoader classLoader:classLoaders) {
            try {
                cls =  classLoader.loadClass(clsName);
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
        if(cls != null){
            classInfobuilder.setStatus(true);
            Method[] Declaredmethods =  cls.getDeclaredMethods();
            for (Method method :Declaredmethods ) {
                int modifiers = method.getModifiers();
                if(Modifier.isNative(modifiers)){
                    continue;
                }
                byte[] MethodCodeItem = dump.dumpMethodByMember(method);
                DumpMethodInfo.Builder methodInfobuilder = DumpMethodInfo.newBuilder();
                String jniSignature = JNISignatureConverter.convertToJNISignature(method);
                methodInfobuilder.setMethodName(method.getName());
                methodInfobuilder.setMethodSign(jniSignature);
                methodInfobuilder.setContent(ByteString.copyFrom(MethodCodeItem));
                classInfobuilder.addDumpMethodInfo(methodInfobuilder.build());
            }
            Constructor[] DeclaredConstructors =  cls.getDeclaredConstructors();
            for (Constructor method :DeclaredConstructors ) {
                byte[] MethodCodeItem = dump.dumpMethodByMember(method);
                DumpMethodInfo.Builder methodInfobuilder = DumpMethodInfo.newBuilder();
                String jniSignature = JNISignatureConverter.convertToJNISignature(method);
                methodInfobuilder.setMethodName(method.getName());
                methodInfobuilder.setMethodSign(jniSignature);
                methodInfobuilder.setContent(ByteString.copyFrom(MethodCodeItem));
                classInfobuilder.addDumpMethodInfo(methodInfobuilder.build());
            }
        }else {
            classInfobuilder.setStatus(false);
        }

        responseObserver.onNext(classInfobuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCookieList(Empty request, StreamObserver<DexInfoList> responseObserver) {
        Map<long[],String> cookieList = dump.getCookieList(context);
        DexInfoList.Builder cooki_list_build =  DexInfoList.newBuilder();
        for(long[] cook:cookieList.keySet() ){
            DexInfo.Builder cookie_build = DexInfo.newBuilder();
            for(long k_long:cook){
                cookie_build.addValues(k_long);
            }
            String dex_path = cookieList.get(cook);
            if(dex_path == null){
                cookie_build.setDexpath("[none]");
            }
            cooki_list_build.addDexinfo(cookie_build.build());
        }
        responseObserver.onNext(cooki_list_build.build());
        responseObserver.onCompleted();
    }

    @Override
    public void dexDumpByDexFilePoint(DexFilePoint request, StreamObserver<Dexbuff> responseObserver) {
        long dexFilePoint =  request.getValues();
        byte[] buff = getDexBuffbyCookieLong(dexFilePoint);
        Dexbuff dexbuff = Dexbuff.newBuilder().setContent(ByteString.copyFrom(buff)).build();
        responseObserver.onNext(dexbuff);
        responseObserver.onCompleted();
    }

    @Override
    public void dexDumpDownload(DownloadFileRequest request, StreamObserver<DownloadFileResponse> responseObserver) {
        DexInfo cookie = request.getDexinfo();
        long[] longArray = cookie.getValuesList().stream().mapToLong(Long::longValue).toArray();
        List<byte[]> dexfile_list= dump.dumpDexBuffListByCookie(longArray);
        DexInfo.Builder ret_dexinfo_builder = DexInfo.newBuilder(cookie);
        for(byte[] dex:dexfile_list){
            Dexbuff dex_buff = Dexbuff.newBuilder().setContent(ByteString.copyFrom(dex)).build();
            ret_dexinfo_builder.addBuff(dex_buff);
        }
        DownloadFileResponse downloadFileResponse = DownloadFileResponse.newBuilder().setContent(ret_dexinfo_builder.build()).build();
        responseObserver.onNext(downloadFileResponse);
        responseObserver.onCompleted();
    }


    @Override
    public void dumpMethod(DumpMethodString request, StreamObserver<Dexbuff> responseObserver) {
        String clsName = request.getClassName();
        Class<?> cls = AndroidFindClass(clsName);
        String methodName = request.getMethodName();
        String methodSign = request.getMethodSign();
        byte[] method_code_item_buff =  dump.dumpMethodByString(cls,methodName,methodSign);
        Dexbuff buff = Dexbuff.newBuilder().setContent(ByteString.copyFrom(method_code_item_buff)).build();
        responseObserver.onNext(buff);
        responseObserver.onCompleted();
    }
}