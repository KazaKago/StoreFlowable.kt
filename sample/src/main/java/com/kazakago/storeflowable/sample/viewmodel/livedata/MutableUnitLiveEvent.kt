package com.kazakago.storeflowable.sample.viewmodel.livedata

import androidx.annotation.MainThread

class MutableUnitLiveEvent : UnitLiveEvent() {

    @MainThread
    public override fun call() {
        super.call()
    }
}
