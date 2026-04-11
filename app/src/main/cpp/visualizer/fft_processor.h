#ifndef CHIRRO_FFT_PROCESSOR_H
#define CHIRRO_FFT_PROCESSOR_H

#include <vector>
#include <cstdint>

class FftProcessor {
public:
    FftProcessor() = default;
    ~FftProcessor() = default;

    /**
     * Calculates FFT frequency bins from raw PCM data.
     * @param data Pointer to raw PCM bytes.
     * @param length Length of the data in bytes.
     * @return Vector of frequency magnitudes.
     */
    std::vector<float> calculate(uint8_t* data, int length);

private:
    // FFT state or library state
};

#endif //CHIRRO_FFT_PROCESSOR_H
