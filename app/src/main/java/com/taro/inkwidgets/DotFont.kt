package com.taro.inkwidgets

/**
 * 5x7 dot-matrix glyphs for the digits 0-9.
 * Each digit is 7 rows of 5 characters; '1' = lit dot, '0' = unlit dot.
 */
object DotFont {

    const val COLS = 5
    const val ROWS = 7

    private val DIGITS: Array<Array<String>> = arrayOf(
        // 0
        arrayOf("01110", "10001", "10001", "10001", "10001", "10001", "01110"),
        // 1
        arrayOf("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
        // 2
        arrayOf("01110", "10001", "00001", "00010", "00100", "01000", "11111"),
        // 3
        arrayOf("11111", "00010", "00100", "00010", "00001", "10001", "01110"),
        // 4
        arrayOf("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
        // 5
        arrayOf("11111", "10000", "11110", "00001", "00001", "10001", "01110"),
        // 6
        arrayOf("00110", "01000", "10000", "11110", "10001", "10001", "01110"),
        // 7
        arrayOf("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
        // 8
        arrayOf("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
        // 9
        arrayOf("01110", "10001", "10001", "01111", "00001", "00010", "01100"),
    )

    /** True when the dot at [row], [col] of [digit] (0-9) is lit. */
    fun isOn(digit: Int, row: Int, col: Int): Boolean =
        DIGITS[digit.coerceIn(0, 9)][row][col] == '1'
}
