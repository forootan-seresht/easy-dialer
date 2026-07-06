package app.arteh.easydialer.utility

import java.time.LocalDate

class Roozh {
    var day: Int = 0
        private set

    var month: Int = 0
        private set

    var year: Int = 0
        private set

    var isPersian: Boolean = false
        private set

    private var persianYear = 0
    private var persianMonth = 0
    private var persianDay = 0
    private var gregorianYear = 0
    private var gregorianMonth = 0
    private var gregorianDay = 0
    private var persianLeap = 0
    private var marchDay = 0

    companion object {
        private val BREAKS = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )
    }

    /**
     * Calculates the Julian Day number from Gregorian or Julian calendar dates.
     *
     * @param year  Year
     * @param month Month
     * @param day   Day
     * @param isJulian true for Julian and false for Gregorian calendar
     * @return Julian Day number
     */
    private fun gregorianToJulianDay(year: Int, month: Int, day: Int, isJulian: Boolean): Int {
        var jd = ((1461 * (year + 4800 + (month - 14) / 12)) / 4
                + (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12
                - (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4 + day
                - 32075)

        if (!isJulian)
            jd = jd - (year + 100100 + (month - 8) / 6) / 100 * 3 / 4 + 752

        return jd
    }

    /**
     * Calculates Gregorian and Julian calendar dates from the Julian Day number.
     *
     * @param jd   Julian day number
     * @param isJulian true for Julian and false for Gregorian calendar
     */
    private fun julianDayToGregorian(jd: Int, isJulian: Boolean) {
        var j = 4 * jd + 139361631

        if (!isJulian)
            j += (4 * jd + 183187720) / 146097 * 3 / 4 * 4 - 3908

        val i = (j % 1461) / 4 * 5 + 308
        gregorianDay = (i % 153) / 5 + 1
        gregorianMonth = ((i / 153) % 12) + 1
        gregorianYear = j / 1461 - 100100 + (8 - gregorianMonth) / 6
    }

    /**
     * Converts the Julian Day number to a date in the Persian (Jalali) calendar.
     *
     * @param jdn the Julian Day number
     */
    private fun julianDayToPersian(jdn: Int) {
        julianDayToGregorian(jdn, false)

        persianYear = gregorianYear - 621
        calculatePersianCalendar(persianYear)

        val jdn1F = gregorianToJulianDay(gregorianYear, 3, marchDay, false)
        var k = jdn - jdn1F
        if (k >= 0) {
            if (k <= 185) {
                persianMonth = 1 + k / 31
                persianDay = (k % 31) + 1
                return
            }
            else {
                k -= 186
            }
        }
        else {
            persianYear -= 1
            k += 179
            if (persianLeap == 1) k += 1
        }

        persianMonth = 7 + k / 30
        persianDay = (k % 30) + 1
    }

    /**
     * Converts a date of the Persian (Jalali) calendar to the Julian Day Number.
     *
     * @param year  Persian year
     * @param month Persian month
     * @param day   Persian day
     * @return Julian day number
     */
    private fun persianToJulianDay(year: Int, month: Int, day: Int): Int {
        calculatePersianCalendar(year)
        return (gregorianToJulianDay(gregorianYear, 3, marchDay, true)
                + (month - 1) * 31 - month / 7 * (month - 7)
                + day - 1)
    }

    /**
     * Determines if the Persian year is leap and finds the start day in March.
     *
     * @param year Persian calendar year (-61 to 3177)
     */
    private fun calculatePersianCalendar(year: Int) {
        marchDay = 0
        persianLeap = 0

        gregorianYear = year + 621
        var leapJ = -14
        var jp = BREAKS[0]

        for (j in 1..19) {
            val jm = BREAKS[j]
            val jump = jm - jp
            if (year < jm) {
                var n = year - jp
                leapJ += n / 33 * 8 + (n % 33 + 3) / 4

                if ((jump % 33) == 4 && (jump - n) == 4) leapJ += 1

                val leapG = (gregorianYear / 4) - ((gregorianYear / 100 + 1) * 3 / 4) - 150
                marchDay = 20 + leapJ - leapG

                if ((jump - n) < 6) n = n - jump + (jump + 4) / 33 * 33

                persianLeap = ((((n + 1) % 33) - 1) % 4)

                if (persianLeap == -1) persianLeap = 4
                break
            }

            leapJ += jump / 33 * 8 + (jump % 33) / 4
            jp = jm
        }
    }

    /**
     * Represents the date as a String in YYYY-MM-DD format.
     *
     * @return Date as String
     */
    override fun toString(): String {
        val yearStr = year.toString().padStart(4, '0')
        val monthStr = month.toString().padStart(2, '0')
        val dayStr = day.toString().padStart(2, '0')
        return "$yearStr-$monthStr-$dayStr"
    }

    /**
     * Converts Gregorian date to Persian (Jalali) date and updates the state.
     *
     * @param year  Gregorian year
     * @param month Gregorian month
     * @param day   Gregorian day
     */
    fun gregorianToPersian(year: Int, month: Int, day: Int) {
        val jd = gregorianToJulianDay(year, month, day, false)
        julianDayToPersian(jd)
        this.year = persianYear
        this.month = persianMonth
        this.day = persianDay
        this.isPersian = true
    }

    /**
     * Converts Persian (Jalali) date to Gregorian date and updates the state.
     *
     * @param year  Persian year
     * @param month Persian month
     * @param day   Persian day
     */
    fun persianToGregorian(year: Int, month: Int, day: Int) {
        val jd = persianToJulianDay(year, month, day)
        julianDayToGregorian(jd, false)
        this.year = gregorianYear
        this.month = gregorianMonth
        this.day = gregorianDay
        this.isPersian = false
    }

    /**
     * Adds (or subtracts) a number of days to the current date and updates the state.
     *
     * @param days Number of days to add
     */
    fun plusDays(days: Int) {
        if (year == 0) return // Not initialized

        val currentJdn = if (isPersian)
            persianToJulianDay(year, month, this.day)
        else
            gregorianToJulianDay(year, month, this.day, false)

        val newJdn = currentJdn + days

        if (isPersian) {
            julianDayToPersian(newJdn)

            this.year = persianYear
            this.month = persianMonth
            this.day = persianDay
        }
        else {
            julianDayToGregorian(newJdn, false)

            this.year = gregorianYear
            this.month = gregorianMonth
            this.day = gregorianDay
        }
    }

    fun plusMonth(month: Int) {
        if (year == 0) return // Not initialized

        val currentJdn = if (isPersian)
            persianToJulianDay(year, this.month, day)
        else
            gregorianToJulianDay(year, this.month, day, false)

        val newJdn = currentJdn + month

        if (isPersian) {
            julianDayToPersian(newJdn)

            this.year = persianYear
            this.month = persianMonth
            this.day = persianDay
        }
    }

    fun toGregorian(): LocalDate {
        if (!isPersian)
            return LocalDate.of(year, month, day)
        else {
            persianToGregorian(year, month, day)
            return LocalDate.of(year, month, day)
        }
    }

    fun withDay(day: Int) {
        this.day = day
    }
}
