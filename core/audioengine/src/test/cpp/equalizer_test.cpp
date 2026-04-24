#include <gtest/gtest.h>
#include "dsp/equalizer.h"
#include <vector>
#include <cstdint>

TEST(Equalizer, Initialization) {
    Equalizer eq;
    eq.init(44100, 2);
}

TEST(Equalizer, ProcessIdentity) {
    Equalizer eq;
    eq.init(44100, 2);
    
    // Set all gains to 0 dB (linear 1.0)
    for (int i = 0; i < 8; ++i) {
        eq.setBand(i, 0.0f);
    }

    // Create 1 second of silent audio
    std::vector<int16_t> audio(44100 * 2, 0);
    uint8_t* data = reinterpret_cast<uint8_t*>(audio.data());
    int length = audio.size() * sizeof(int16_t);

    eq.process(data, length);

    // After processing silent audio with 0dB gain, it should still be silent
    for (size_t i = 0; i < audio.size(); ++i) {
        EXPECT_EQ(audio[i], 0);
    }
}

TEST(Equalizer, ProcessSineWave) {
    Equalizer eq;
    eq.init(44100, 1);
    
    // 1kHz sine wave
    std::vector<int16_t> audio(1024);
    for (int i = 0; i < 1024; ++i) {
        audio[i] = static_cast<int16_t>(10000.0 * sin(2.0 * M_PI * 1000.0 * i / 44100.0));
    }

    uint8_t* data = reinterpret_cast<uint8_t*>(audio.data());
    int length = audio.size() * sizeof(int16_t);

    eq.process(data, length);
}
