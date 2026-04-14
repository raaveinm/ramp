#ifndef CHIRRO_EQUALIZER_H
#define CHIRRO_EQUALIZER_H

#include <cstdint>
#include <vector>
#include <atomic>
#include "kiss_fftr.h"

class Equalizer {
public:
    Equalizer();
    ~Equalizer();

    /**
     * Initializes the equalizer.
     * @param sample_rate Audio sample rate (e.g., 44100).
     * @param num_channels Number of channels (e.g., 2 for stereo).
     */
    void init(int sample_rate, int num_channels);

    /**
     * Processes PCM audio data.
     * @param data Pointer to raw 16-bit PCM bytes.
     * @param length Length of the data in bytes.
     */
    void process(uint8_t* data, int length);

    /**
     * Sets the gain for a specific band.
     * @param band Band index (0-7).
     * @param gain_db Gain in dB.
     */
    void setBand(int band, float gain_db);

private:
    static constexpr int FFT_SIZE = 2048;
    static constexpr int HOP_SIZE = FFT_SIZE / 2;
    static constexpr int NUM_BANDS = 8;
    static constexpr float BAND_FREQS[NUM_BANDS] = {
        60.0f, 150.0f, 400.0f, 1000.0f, 2500.0f, 6000.0f, 10000.0f, 15000.0f
    };

    struct ChannelState {
        kiss_fftr_cfg forward_cfg = nullptr;
        kiss_fftr_cfg inverse_cfg = nullptr;
        float input_fifo[FFT_SIZE]{};
        float output_fifo[FFT_SIZE]{};
        int fifo_pos = 0;

        ChannelState() {
            for (int i = 0; i < FFT_SIZE; ++i) {
                input_fifo[i] = 0.0f;
                output_fifo[i] = 0.0f;
            }
        }
        ~ChannelState() {
            if (forward_cfg) kiss_fftr_free(forward_cfg);
            if (inverse_cfg) kiss_fftr_free(inverse_cfg);
        }
    };

    int sample_rate_ = 44100;
    int num_channels_ = 2;
    std::vector<ChannelState*> channels_;
    
    std::atomic<float> band_gains_[NUM_BANDS];
    float interpolated_gains_[FFT_SIZE / 2 + 1]{};
    float window_[FFT_SIZE]{};
    std::atomic<bool> gains_dirty_{true};

    void updateInterpolatedGains();
    void processChannel(ChannelState &ch, float *samples, int count) const;
};

#endif //CHIRRO_EQUALIZER_H
