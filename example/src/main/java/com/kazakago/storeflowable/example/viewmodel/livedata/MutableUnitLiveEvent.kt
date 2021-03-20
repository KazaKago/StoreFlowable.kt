package com.kazakago.storeflowable.example.viewmodel.livedata

import androidx.annotation.MainThread

class MutableUnitLiveEvent : UnitLiveEvent() {

    @MainThread
    public override fun call() {
        super.call()
    }
}
