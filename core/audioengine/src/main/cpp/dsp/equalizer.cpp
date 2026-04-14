#include "equalizer.h"
#include <cmath>
#include <algorithm>
#include <cstring>
#include <ranges>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

Equalizer::Equalizer() {
    for (auto & band_gain : band_gains_) {
        band_gain.store(.0f);
    }
    updateInterpolatedGains();

    // Initialize Hann window
    for (const int i : std::views::iota(0, FFT_SIZE)) {
        window_[i] = 0.5f * (1.0f - cosf(2.f * M_PI * i / (FFT_SIZE - 1)));
    }
}

Equalizer::~Equalizer() {
    for (auto ch : channels_) {
        delete ch;
    }
    channels_.clear();
}

void Equalizer::init(int sample_rate, int num_channels) {
    sample_rate_ = sample_rate;
    num_channels_ = num_channels;

    for (const auto ch : channels_) {
        delete ch;
    }
    channels_.clear();

    for (int i : std::views::iota(0, num_channels)) {
        auto* ch = new ChannelState();
        ch->forward_cfg = kiss_fftr_alloc(FFT_SIZE, 0, nullptr, nullptr);
        ch->inverse_cfg = kiss_fftr_alloc(FFT_SIZE, 1, nullptr, nullptr);
        channels_.push_back(ch);
    }
    updateInterpolatedGains();
}

void Equalizer::process(uint8_t* data, int length) {
    if (channels_.empty()) return;

    auto* samples = reinterpret_cast<int16_t*>(data);
    const int sample_count = length / sizeof(int16_t);
    const int frames = sample_count / num_channels_;

    if (gains_dirty_.exchange(false)) {
        updateInterpolatedGains();
    }

    // Temporary float buffer for de-interleaved processing
    std::vector<float> channel_buffer(frames);

    for (int c = 0; c < num_channels_; ++c) {
        // De-interleave
        for (const int i : std::views::iota(0, frames)) {
            channel_buffer[i] = static_cast<float>(samples[i * num_channels_ + c]) / 32768.0f;
        }

        processChannel(*channels_[c], channel_buffer.data(), frames);

        // Re-interleave
        for (int i = 0; i < frames; ++i) {
            float processed = channel_buffer[i] * 32768.0f;
            samples[i * num_channels_ + c] = static_cast<int16_t>(std::clamp(processed, -32768.0f, 32767.0f));
        }
    }
}

void Equalizer::processChannel(ChannelState &ch, float *samples, const int count) const {
    for (const int i : std::views::iota(0, count)) {
        // Collect samples into input fifo (second half)
        ch.input_fifo[HOP_SIZE + ch.fifo_pos] = samples[i];
        // Output from previous overlap-add (first half)
        samples[i] = ch.output_fifo[ch.fifo_pos];

        ch.fifo_pos++;

        if (ch.fifo_pos >= HOP_SIZE) {
            // Process full window [0...FFT_SIZE-1]
            float fft_in[FFT_SIZE];
            for (int j = 0; j < FFT_SIZE; ++j) {
                fft_in[j] = ch.input_fifo[j] * window_[j];
            }

            kiss_fft_cpx freq_out[FFT_SIZE / 2 + 1];
            kiss_fftr(ch.forward_cfg, fft_in, freq_out);

            // Apply EQ Gains
            for (const int j : std::views::iota(0, FFT_SIZE / 2 + 1)) {
                freq_out[j].r *= interpolated_gains_[j];
                freq_out[j].i *= interpolated_gains_[j];
            }

            // Inverse FFT
            float fft_out[FFT_SIZE];
            kiss_fftri(ch.inverse_cfg, freq_out, fft_out);

            // Overlap-Add and Shift
            constexpr float norm = 1.0f / FFT_SIZE;
            for (const int j : std::views::iota(0, HOP_SIZE)) {
                ch.output_fifo[j] = ch.output_fifo[j + HOP_SIZE] + fft_out[j] * norm;
                ch.output_fifo[j + HOP_SIZE] = fft_out[j + HOP_SIZE] * norm;
            }

            // Shift input_fifo
            for (const int j : std::views::iota(0, HOP_SIZE)) {
                ch.input_fifo[j] = ch.input_fifo[j + HOP_SIZE];
            }

            ch.fifo_pos = 0;
        }
    }
}

void Equalizer::setBand(int band, float gain_db) {
    if (band >= 0 && band < NUM_BANDS) {
        band_gains_[band].store(gain_db);
        gains_dirty_.store(true);
    }
}

void Equalizer::updateInterpolatedGains() {
    float band_linear[NUM_BANDS];
    for (int i : std::views::iota(0, NUM_BANDS)) {
        band_linear[i] = powf(10.0f, band_gains_[i].load() / 20.0f);
    }

    // Map bands to bins
    int band_bins[NUM_BANDS];
    for (const int i : std::views::iota(0, NUM_BANDS)) {
        band_bins[i] = static_cast<int>(roundf(BAND_FREQS[i] * FFT_SIZE / sample_rate_));
        band_bins[i] = std::clamp(band_bins[i], 0, FFT_SIZE / 2);
    }

    // Interpolate
    for (int bin = 0; bin <= FFT_SIZE / 2; ++bin) {
        if (bin <= band_bins[0]) {
            interpolated_gains_[bin] = band_linear[0];
        } else if (bin >= band_bins[NUM_BANDS - 1]) {
            interpolated_gains_[bin] = band_linear[NUM_BANDS - 1];
        } else {
            // Find which bands this bin is between
            for (const int i : std::views::iota(0, NUM_BANDS - 1)) {
                if (bin >= band_bins[i] && bin <= band_bins[i + 1]) {
                    float t = static_cast<float>(bin - band_bins[i]) / (band_bins[i + 1] - band_bins[i]);
                    interpolated_gains_[bin] = band_linear[i] * (1.0f - t) + band_linear[i + 1] * t;
                    break;
                }
            }
        }
    }
}
