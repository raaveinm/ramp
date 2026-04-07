//
// Created by Kirill Dulov on 4/7/26.
//

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_test(JNIEnv *env, jobject thiz) {
    std::string sample = "test value";
    return env->NewStringUTF(sample.c_str());
}
