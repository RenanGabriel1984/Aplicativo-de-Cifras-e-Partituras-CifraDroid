package com.example.util

enum class MusicalPass {
    FIRST_PASS,
    SECOND_PASS,
    THIRD_PASS,
    FINAL_PASS;

    fun next(): MusicalPass {
        return entries.getOrNull(ordinal + 1) ?: FINAL_PASS
    }
}
