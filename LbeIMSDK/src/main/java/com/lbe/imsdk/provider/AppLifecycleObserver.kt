package com.lbe.imsdk.provider

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * app 生命周期 监控
 * @Author mocaris
 * @Date 2026-01-28
 * @Since
 */
object AppLifecycleObserver {

     val appBackState = MutableStateFlow<Boolean>(false)


    private val observer = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            appBackState.value = true
        }

        override fun onStart(owner: LifecycleOwner) {
            appBackState.value = false
        }

    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    fun addObserver(observer: DefaultLifecycleObserver) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    fun removeObserver(observer: DefaultLifecycleObserver) {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
    }

}