package com.service;

/**
 * Created by ZloiY on 06.04.17.
 */
public enum PatternGroup {
        MV_PATTERNS(1),
        STRUCT_PATTERNS(2),
        CREAT_PATTERNS(3),
        BEHAVE_PATTERNS(4);

        private final int value;

        private PatternGroup(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    public static PatternGroup findByValue(int value) {
        switch (value) {
            case 1:
                return MV_PATTERNS;
            case 2:
                return STRUCT_PATTERNS;
            case 3:
                return CREAT_PATTERNS;
            case 4:
                return BEHAVE_PATTERNS;
            default:
                return null;
        }
    }
}
