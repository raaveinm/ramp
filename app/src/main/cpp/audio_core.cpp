#include <jni.h>
#include <string>
#include <vector>
#include "dsp/equalizer.h"
#include "visualizer/fft_processor.h"

static Equalizer g_equalizer;
static FftProcessor g_fft_processor;

extern "C" JNIEXPORT jstring JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_test(JNIEnv *env, jobject thiz) {
    std::string sample = "test value";
    return env->NewStringUTF(sample.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_initEqualizer(JNIEnv *env, jobject thiz, jint sample_rate, jint num_channels) {
    g_equalizer.init(sample_rate, num_channels);
}

extern "C" JNIEXPORT void JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_processAudio(JNIEnv *env, jobject thiz, jobject buffer, jint length) {
    auto *data = static_cast<uint8_t *>(env->GetDirectBufferAddress(buffer));
    if (data == nullptr) return;

    g_equalizer.process(data, length);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_calculateFFT(JNIEnv *env, jobject thiz, jobject buffer, jint length) {
    auto *data = static_cast<uint8_t *>(env->GetDirectBufferAddress(buffer));
    if (data == nullptr) return nullptr;

    std::vector<float> result = g_fft_processor.calculate(data, length);

    jfloatArray j_result = env->NewFloatArray(result.size());
    if (j_result != nullptr) {
        env->SetFloatArrayRegion(j_result, 0, result.size(), result.data());
    }
    return j_result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_setEqualizerBand(JNIEnv *env, jobject thiz, jint band, jfloat gain) {
    g_equalizer.setBand(band, gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_raaveinm_chirro_domain_jni_AudioCore_setEffectEnabled(JNIEnv *env, jobject thiz, jint effect_id, jboolean enabled) {
    // Enable/disable effects in global state
    // This is a placeholder for future implementation
}
