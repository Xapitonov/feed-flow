@file:Suppress("MagicNumber")

package com.prof18.feedflow.domain

import co.touchlab.kermit.Logger
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

@Suppress("TooGenericExceptionCaught")
internal actual fun getDateMillisFromString(dateString: String): Long? {
    val dateFormatter = NSDateFormatter().apply {
        setDateFormat("E, d MMM yyyy HH:mm:ss Z")
        setLocale(NSLocale("en_US_POSIX"))
    }
    val timeZoneDateFormatter = NSDateFormatter().apply {
        setDateFormat("E, d MMM yyyy HH:mm:ss zzz")
        setLocale(NSLocale("en_US_POSIX"))
    }

    var exception: Throwable? = null
    var message: String? = null

    var date = try {
        dateFormatter.dateFromString(dateString)
    } catch (e: Throwable) {
        exception = e
        message = "Error while trying to format the date with dateFormatter. Date: $dateString"
        null
    }

    if (date == null) {
        date = try {
            timeZoneDateFormatter.dateFromString(dateString)
        } catch (e: Throwable) {
            exception = e
            message = "Error while trying to format the date with timeZoneDateFormatter. Date: $dateString"
            null
        }
    }

    if (date == null) {
        if (exception != null && message != null) {
            Logger.e(exception) {
                message
            }
        }
        return null
    }

    return (date.timeIntervalSince1970 * 1000).toLong()
}

internal actual fun formatDate(millis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970((millis.toDouble() / 1000.0))

    val calendar = NSCalendar.currentCalendar
    val isToday = calendar.isDateInToday(date)

    val pattern = if (isToday) {
        "HH:mm"
    } else {
        "dd/MM - HH:mm"
    }

    val dateFormatter = NSDateFormatter().apply {
        setDateFormat(pattern)
        setLocale(NSLocale.currentLocale)
    }

    return dateFormatter.stringFromDate(date)
}

internal actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
