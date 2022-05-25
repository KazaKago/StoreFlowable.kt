package com.kazakago.storeflowable.exception

/**
 * This Exception occurs when `requestNextData()` or `requestPrevData()` is called on `DataState.Error`.
 *
 * @see com.kazakago.storeflowable.datastate.DataState.Error
 * @see com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable.requestPrevData
 */
public class AdditionalRequestOnErrorStateException : IllegalStateException()
