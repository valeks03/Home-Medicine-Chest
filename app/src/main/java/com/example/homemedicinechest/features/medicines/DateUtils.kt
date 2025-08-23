package com.example.homemedicinechest.features.medicines

import java.util.Calendar

fun endOfMonthMillis(year: Int, month0: Int): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, month0)           // 0..11
    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
    c.set(Calendar.HOUR_OF_DAY, 12)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun plusYearsToEndOfMonth(years: Int): Long {
    val now = Calendar.getInstance()
    val y = now.get(Calendar.YEAR) + years
    val m = now.get(Calendar.MONTH)
    return endOfMonthMillis(y, m)
}
