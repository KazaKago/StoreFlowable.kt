package com.kazakago.cacheflowable.sample.viewmodel.livedata

import androidx.annotation.MainThread

class MutableUnitLiveEvent : UnitLiveEvent() {

    @MainThread
    public override fun call() {
        super.call()
    }

}