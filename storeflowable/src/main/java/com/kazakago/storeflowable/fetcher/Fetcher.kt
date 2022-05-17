package com.kazakago.storeflowable.fetcher

public interface Fetcher<PARAM, DATA> {

    public suspend fun fetch(param: PARAM): DATA
}
