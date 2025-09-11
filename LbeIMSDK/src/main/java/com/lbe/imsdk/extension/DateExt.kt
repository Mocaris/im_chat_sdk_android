package com.lbe.imsdk.extension

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

/**
 *
 * @Author mocaris
 * @Date 2025-08-27
 */


@SuppressLint("SimpleDateFormat")
val formatter1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")


@SuppressLint("SimpleDateFormat")
val formatter2 = SimpleDateFormat("MM-dd HH:mm:ss")

@SuppressLint("SimpleDateFormat")
val formatter3 = SimpleDateFormat("HH:mm")

@SuppressLint("SimpleDateFormat")
val yyyMMddHHmm = SimpleDateFormat("yyyy-MM-dd HH:mm")


fun Date.toYMDHMS(): String {
    return formatter1.format(this)
}

fun Date.toMDHMS(): String {
    return formatter2.format(this)
}
fun Date.toHM(): String {
    return formatter3.format(this)
}
fun Date.toyyyMMddHHmm(): String {
    return yyyMMddHHmm.format(this)
}