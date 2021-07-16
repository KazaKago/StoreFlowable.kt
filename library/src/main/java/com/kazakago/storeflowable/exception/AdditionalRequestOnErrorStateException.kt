package com.kazakago.storeflowable.exception

/**
 * This Exception occurs when `requestNextData()` or `requestPrevData()` is called on `DataState.Error`.
 *
 * @see com.kazakago.storeflowable.datastate.DataState.Error
 * @see com.kazakago.storeflowable.pagination.oneway.OneWayStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowable.requestPrevData
 */
class AdditionalRequestOnErrorStateException : IllegalStateException()