cmake_minimum_required(VERSION 3.22.1)
project("chirro")

set(CMAKE_CXX_FLAGS "-std=c++23")
set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(shuffle-core SHARED IMPORTED)
set_target_properties(shuffle-core PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libshuffle-core.so)


include_directories(
    .
    dsp
    visualizer
    external/kissfft
)

add_library(audio_dsp STATIC
    dsp/equalizer.cpp
    dsp/speed_shifter.cpp
    external/kissfft/kiss_fft.c
    external/kissfft/kiss_fftr.c
)

add_library(visualizer STATIC visualizer/fft_processor.cpp)
add_library(chirro SHARED
    shuffle-lib.cpp
    audio_core.cpp
)

target_link_options(chirro PRIVATE "-Wl,-z,max-page-size=16384")

target_link_libraries(chirro PRIVATE
    shuffle-core
    audio_dsp
    visualizer
    android
    log)
