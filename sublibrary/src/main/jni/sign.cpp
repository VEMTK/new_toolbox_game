//
// Created by wxmylife on 2017/3/7.
//

#include <jni.h>
#include <string.h>
#include <stdio.h>
#include "com_xxm_sublibrary_jni_Ja.h"

#include <android/log.h> //导入log.h

#define LOG_TAG "love"  //指定打印到logcat的Tag
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

/**
 *这个key是和服务器之间通信的秘钥
 */

/**
 * 发布的app 签名,只有和本签名一致的app 才会返回 AUTH_KEY
 * 这个RELEASE_SIGN的值是上一步用java代码获取的值
 */
const char* RELEASE_SIGN = "3082032e30820216a00302010202035b99fb300d06092a864886f70d01010b05003048310a30080603550406130141310a30080603550408130141310a30080603550407130141310a3008060355040a13014c310a3008060355040b130157310a30080603550403130148301e170d3137303730333039303432325a170d3432303632373039303432325a3048310a30080603550406130141310a30080603550408130141310a30080603550407130141310a3008060355040a13014c310a3008060355040b130157310a3008060355040313014830820122300d06092a864886f70d01010105000382010f003082010a028201010095d8167adaf0745912c915f9c92e88f9b5163ef63a755c2000a273c19abc90c49ed9aa6a324364bde8a5a17965e7a16a422d85b2a0fc0f670652db28a182b55cf13ebfeeef79a0e069f046f8e5fe764116e1d2dbd6a6a6319d2d345a0a277b9624deb119fe292e0056792f830fd30c3f53ef1bc0827f9253855852ac6fa9db2381f54d149e882944b2b1ec768a04d27fc72793038cb7a730e6ac20a79d25f062d0ef03774f75ea4121b5b82555bc128100062c6296caa9720a6afd13504d0cc20dcdf36652a3b35f668c48a63a065f1e472e25bb60da24796b597a59cc21de9676a2070d3a5841f247197bea5d1c54887cdeea46932c9da5a62f20d33749ea130203010001a321301f301d0603551d0e04160414e1ffd116fc87e352759e0261a6e7d51f6badaea8300d06092a864886f70d01010b0500038201010070f85a797742c45ea3624bc45f7da79114ef755b9fc06e13dfc725b9422b9bfae2fb8e6dfe447adc6c89c25ce7f0d668ad2333515f9235629cd18356bc9df921555337759e9ba254dc86ed53ea7cdab150602930798498fa8bc2127f245a21394a0bfb401e57bb483282f1470bb7e0957b98ebcc57757ced96c0a0269e1805d4e1fcdbbe1f56790edab68ea640ddc100c5086acf5713c7c200bfc750c2caf49d262df397a7f54a28c6e51eace278e8dd7b2fb5047a990b7644ac8b2db58e79f24470acbc4334f67713830dd21ba9c63aa26cf86d5f5da6a45735e71df769f57cbf833dfdddf74c689e3ef2179ee1761b3c029b70be81e988fd934ec2f274edda";
const char* AUTH_KEY = "5d4d629bfe85709f";
/**
 * 发布的app 签名 的HashCode
 */
const int RELEASE_SIGN_HASHCODE = -332752192;

JNIEXPORT jstring JNICALL Java_com_xxm_sublibrary_jni_Ja_getPublicKey
  (JNIEnv *env, jclass jclazz, jobject contextObject){

    jclass native_class = env->GetObjectClass(contextObject);
    jmethodID pm_id = env->GetMethodID(native_class, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject pm_obj = env->CallObjectMethod(contextObject, pm_id);
    jclass pm_clazz = env->GetObjectClass(pm_obj);
    // 得到 getPackageInfo 方法的 ID
    jmethodID package_info_id = env->GetMethodID(pm_clazz, "getPackageInfo","(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jclass native_classs = env->GetObjectClass(contextObject);
    jmethodID mId = env->GetMethodID(native_classs, "getPackageName", "()Ljava/lang/String;");
    jstring pkg_str = static_cast<jstring>(env->CallObjectMethod(contextObject, mId));
    // 获得应用包的信息
    jobject pi_obj = env->CallObjectMethod(pm_obj, package_info_id, pkg_str, 64);
    // 获得 PackageInfo 类
    jclass pi_clazz = env->GetObjectClass(pi_obj);
    // 获得签名数组属性的 ID
    jfieldID signatures_fieldId = env->GetFieldID(pi_clazz, "signatures", "[Landroid/content/pm/Signature;");
    jobject signatures_obj = env->GetObjectField(pi_obj, signatures_fieldId);
    jobjectArray signaturesArray = (jobjectArray)signatures_obj;
    jsize size = env->GetArrayLength(signaturesArray);
    jobject signature_obj = env->GetObjectArrayElement(signaturesArray, 0);
    jclass signature_clazz = env->GetObjectClass(signature_obj);

    //第一种方式--检查签名字符串的方式
    jmethodID string_id = env->GetMethodID(signature_clazz, "toCharsString", "()Ljava/lang/String;");
    jstring str = static_cast<jstring>(env->CallObjectMethod(signature_obj, string_id));
    char *c_msg = (char*)env->GetStringUTFChars(str,0);

    if(strcmp(c_msg,RELEASE_SIGN)==0)//签名一致  返回合法的 api key，否则返回错误
    {

        return (env)->NewStringUTF(AUTH_KEY);

    }else
    {
        return (env)->NewStringUTF("error");
    }

    //第二种方式--检查签名的hashCode的方式
    /*
    jmethodID int_hashcode = env->GetMethodID(signature_clazz, "hashCode", "()I");
    jint hashCode = env->CallIntMethod(signature_obj, int_hashcode);
    if(hashCode == RELEASE_SIGN_HASHCODE)
    {
        return (env)->NewStringUTF(AUTH_KEY);
    }else{
        return (env)->NewStringUTF("错误");
    }
     */
}