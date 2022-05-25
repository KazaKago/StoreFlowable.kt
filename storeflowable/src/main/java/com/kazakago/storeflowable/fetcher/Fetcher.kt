package com.kazakago.storeflowable.fetcher

/**
 * Class for fetching origin data from server.
 *
 * @see com.kazakago.storeflowable.from
 */
public interface Fetcher<PARAM, DATA> {

    /**
     * The latest data acquisition process from origin.
     *
     * @return acquired data.
     */
    public suspend fun fetch(param: PARAM): DATA
}
