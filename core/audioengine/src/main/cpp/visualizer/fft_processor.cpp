#include "fft_processor.h"
#include <cmath>
#include <algorithm>

std::vector<float> FftProcessor::calculate(uint8_t* data, int length) {

    int num_samples = length / sizeof(int16_t);
    int num_bins = 64; // frequency
    std::vector<float> bins(num_bins, 0.0f);

    auto* samples = reinterpret_cast<int16_t*>(data);

    // Simplistic visual response: map sample chunks to bins
    int samples_per_bin = std::max(1, num_samples / num_bins);
    for (int i = 0; i < num_bins; ++i) {
        float sum = 0;
        int count = 0;
        for (int j = 0; j < samples_per_bin && (i * samples_per_bin + j) < num_samples; ++j) {
            sum += std::abs(static_cast<float>(samples[i * samples_per_bin + j]));
            count++;
        }
        if (count > 0) {
            bins[i] = (sum / count) / 32768.0f;
        }
    }

    return bins;
}
