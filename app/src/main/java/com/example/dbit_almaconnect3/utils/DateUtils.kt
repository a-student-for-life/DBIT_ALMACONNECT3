package com.example.dbit_almaconnect3.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Date

/**
 * Formats an ISO 8601 date string to a more readable format (MMM dd, yyyy)
 */
object DateUtils {
    /**
     * Formats an ISO 8601 date string to a more readable date format
     * @param isoDateString The ISO 8601 formatted date string to format
     * @return A user-friendly formatted date string (MMM dd, yyyy)
     */
    fun formatDate(isoDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(isoDateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            isoDateString // Fallback to original string if parsing fails
        }
    }
    
    /**
     * Formats an ISO 8601 date string to a more readable date and time format
     * @param isoDateString The ISO 8601 formatted date string to format
     * @return A user-friendly formatted date and time string (MMM dd, yyyy HH:mm)
     */
    fun formatDateTime(isoDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoDateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            isoDateString // Fallback to original string if parsing fails
        }
    }
    
    /**
     * Returns the current date and time in ISO 8601 format
     * @return Current date and time as yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    fun getCurrentISODateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
} 