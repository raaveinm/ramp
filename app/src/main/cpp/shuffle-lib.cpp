//
// Created by raaveinm on 2/8/26.
//
#include <jni.h>
#include "shuffle_core.h"

extern "C" JNIEXPORT void JNICALL
Java_com_raaveinm_chirro_domain_Shuffle_shuffleTrackList(JNIEnv *env, jobject thiz, jlongArray array) {
    jlong *elements = env->GetLongArrayElements(array, nullptr);
    jsize size = env->GetArrayLength(array);

    shuffle_tracklist(reinterpret_cast<int64_t*>(elements), size);
    env->ReleaseLongArrayElements(array, elements, 0);
}