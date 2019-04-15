package com.github.omarmiatello.gdgtools.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


// Date utils

val Date.yearInt get() = Calendar.getInstance().also { it.time = this }.year
val Date.monthInt get() = Calendar.getInstance().also { it.time = this }.month
val Date.dayInt get() = Calendar.getInstance().also { it.time = this }[Calendar.DAY_OF_MONTH]

private val calendarWeekNumber = Calendar.getInstance()
val Date.weekOfYear: Int
    get() {
        calendarWeekNumber.time = this
        return calendarWeekNumber.get(Calendar.WEEK_OF_YEAR)
    }

inline val Calendar.year: Int get() = get(Calendar.YEAR)
inline val Calendar.month: Int get() = get(Calendar.MONTH)
inline val Calendar.weekOfYear: Int get() = get(Calendar.WEEK_OF_YEAR)


private val fullItalianFormatter = DateFormat.getDateInstance(DateFormat.FULL, Locale.ITALIAN)
private val dayMonthFormatter = SimpleDateFormat("d MMM", Locale.ITALIAN)
// val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ITALIAN)
fun Date.formatFull() = fullItalianFormatter.format(this)

fun Date.formatDayMonth() = dayMonthFormatter.format(this)

fun weekRangeFrom(fromCalendar: Calendar.() -> Unit): Pair<Calendar, Calendar> {
    val start = Calendar.getInstance(Locale.ITALIAN).apply {
        fromCalendar()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Monday
    }
    val end = Calendar.getInstance(Locale.ITALIAN).apply {
        time = start.time
        add(Calendar.DAY_OF_MONTH, 7)
        add(Calendar.MILLISECOND, -1)
    }
    return start to end
}