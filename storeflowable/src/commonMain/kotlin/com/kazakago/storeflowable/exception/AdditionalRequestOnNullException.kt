package com.kazakago.storeflowable.exception

/**
 * This Exception occurs when `requestNextData()` or `requestPrevData()` is called on No cache (= cache is null).
 *
 * @see com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable.requestNextData
 * @see com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable.requestPrevData
 */
class AdditionalRequestOnNullException : IllegalStateException()
