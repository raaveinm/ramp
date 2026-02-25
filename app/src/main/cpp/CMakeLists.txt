cmake_minimum_required(VERSION 3.22.1)
project("chirro-shuffle")

add_library(shuffle-core SHARED IMPORTED)

set_target_properties(shuffle-core PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libshuffle-core.so)

add_library(chirro-shuffle SHARED shuffle-lib.cpp)

target_link_options(chirro-shuffle PRIVATE "-Wl,-z,max-page-size=16384")

target_link_libraries(chirro-shuffle shuffle-core android log)