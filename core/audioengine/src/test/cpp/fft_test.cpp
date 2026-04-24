#include <gtest/gtest.h>
#include "visualizer/fft_processor.h"
#include <vector>
#include <cstdint>
#include <cmath>

[[maybe_unused]] TEST(FftProcessor, ZeroInput) {
    FftProcessor fft;
    std::vector<int16_t> audio(1024, 0);
    uint8_t* data = reinterpret_cast<uint8_t*>(audio.data());
    int length = audio.size() * sizeof(int16_t);

    std::vector<float> bins = fft.calculate(data, length);

    EXPECT_EQ(bins.size(), 64);
    for (float b : bins) {
        EXPECT_FLOAT_EQ(b, 0.0f);
    }
}

TEST(FftProcessorTest, MaxValueInput) {
    FftProcessor fft;
    // Maximum magnitude in every sample
    std::vector<int16_t> audio(1024, 32767);
    uint8_t* data = reinterpret_cast<uint8_t*>(audio.data());
    int length = audio.size() * sizeof(int16_t);

    std::vector<float> bins = fft.calculate(data, length);

    EXPECT_EQ(bins.size(), 64);
    for (float b : bins) {
        EXPECT_NEAR(b, 1.0f, 0.01f);
    }
}
