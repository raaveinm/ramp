cmake_minimum_required(VERSION 3.22.1)
project("chirro")

add_library(shuffle-core SHARED IMPORTED)
set_target_properties(shuffle-core PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libshuffle-core.so)


add_library(audio_dsp STATIC
    audio_core.cpp
    dsp/equalizer.cpp
    dsp/speed_shifter.cpp
)

add_library(visualizer STATIC visualizer/fft_processor.cpp)
add_library(chirro SHARED shuffle-lib.cpp)

target_link_options(chirro PRIVATE "-Wl,-z,max-page-size=16384")

target_link_libraries(chirro PRIVATE
    shuffle-core
    audio_dsp
    visualizer
    android
    log)
