package com.raaveinm.ramp.ext;

import java.util.Random;

public class ExtMethods {
    public static int getRandom(int[] ToSort) {
        int rnd = new Random().nextInt(ToSort.length);
        return ToSort[rnd];
    }
}
