package com.kazakago.storeflowable.exception

/**
 * This Exception occurs when `requestNextData()` or `requestPrevData()` is called on No cache (= cache is null).
 *
 * @see com.kazakago.storeflowable.pagination.oneway.OneWayStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowable.requestPrevData
 */
class AdditionalRequestOnNullException : IllegalStateException()
