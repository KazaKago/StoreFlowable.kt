package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.StoreFlowableCallback

/**
 * Callback class used from [PaginatingStoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output according to this interface.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface PaginatingStoreFlowableCallback<KEY, DATA> : StoreFlowableCallback<KEY, DATA>, PaginatingCacheDataManager<DATA>, PaginatingOriginDataManager<DATA>
