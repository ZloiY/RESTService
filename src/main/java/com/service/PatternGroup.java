package com.service;

/**
 * Класс хранящий в себе группы паттернов
 * MV_PATTERNS - MV-паттерны;
 * STRUCT_PATTERNS - структурные паттерны;
 * CREAT_PATTERNS - порождающие паттерны;
 * BEHAVE_PATTERNS - поведенческие паттерны.
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

    /**
     * Возвращает id текущей группы
     * @return id
     */
    public int getValue() {
            return value;
        }

    /**
     * Возвращает PatternGroup
     * @param value id группы паттерна
     * @return PatternGroup
     */
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
