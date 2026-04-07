//
// Created by Kirill Dulov on 4/7/26.
//

#ifndef CHIRRO_EQUALIZER_H
#define CHIRRO_EQUALIZER_H

#include <cstdint>

class Equalizer {
public:
    Equalizer() = default;
    ~Equalizer() = default;

    /**
     * Processes PCM audio data.
     * @param data Pointer to raw PCM bytes.
     * @param length Length of the data in bytes.
     */
    void process(uint8_t* data, int length);

private:
    // Add filter coefficients and state here
};

#endif //CHIRRO_EQUALIZER_H
