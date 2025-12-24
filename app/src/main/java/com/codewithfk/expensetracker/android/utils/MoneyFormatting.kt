package com.codewithfk.expensetracker.android.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object MoneyFormatting {
    // Keep only digits
    fun unformat(value: String): String = value.filter { it.isDigit() }

    // Format digits with dot thousand separators (no currency symbol)
    fun formatWithDots(digitsOnly: String): String {
        if (digitsOnly.isEmpty()) return ""
        val sb = StringBuilder()
        var count = 0
        for (i in digitsOnly.length - 1 downTo 0) {
            sb.append(digitsOnly[i])
            count++
            if (count == 3 && i > 0) {
                sb.append('.')
                count = 0
            }
        }
        return sb.reverse().toString()
    }

    // VisualTransformation that displays thousand separators while the underlying
    // text may already contain digits and dots. It handles cursor position via OffsetMapping.
    class ThousandSeparatorTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val original = text.text
            // Extract digits from original to produce canonical formatted string
            val digits = unformat(original)
            val formatted = formatWithDots(digits)

            // Build lists of positions of digit characters in original and formatted strings
            val digitPositionsInOriginal = mutableListOf<Int>()
            for (i in original.indices) if (original[i].isDigit()) digitPositionsInOriginal.add(i)

            val digitPositionsInFormatted = mutableListOf<Int>()
            for (i in formatted.indices) if (formatted[i].isDigit()) digitPositionsInFormatted.add(i)

            val offsetTranslator = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    // offset is a position in the original text (may include dots)
                    if (offset <= 0) return 0
                    if (digitPositionsInOriginal.isEmpty() || digitPositionsInFormatted.isEmpty()) {
                        // fallback: position proportional
                        return offset.coerceAtMost(formatted.length)
                    }
                    // count digits in original strictly before offset
                    var digitCount = 0
                    for (pos in digitPositionsInOriginal) {
                        if (pos < offset) digitCount++ else break
                    }
                    if (digitCount <= 0) return 0
                    if (digitCount > digitPositionsInFormatted.size) return formatted.length
                    return digitPositionsInFormatted[digitCount - 1] + 1
                }

                override fun transformedToOriginal(offset: Int): Int {
                    // offset is a position in the transformed (formatted) text
                    if (offset <= 0) return 0
                    if (digitPositionsInOriginal.isEmpty() || digitPositionsInFormatted.isEmpty()) {
                        // fallback: position proportional
                        return offset.coerceAtMost(original.length)
                    }
                    // count digits in formatted strictly before offset
                    var digitCount = 0
                    for (pos in digitPositionsInFormatted) {
                        if (pos < offset) digitCount++ else break
                    }
                    if (digitCount <= 0) return 0
                    if (digitCount > digitPositionsInOriginal.size) return original.length
                    // return position in original after that digit
                    return digitPositionsInOriginal[digitCount - 1] + 1
                }
            }

            return TransformedText(AnnotatedString(formatted), offsetTranslator)
        }
    }
}
