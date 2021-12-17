# StoreFlowable.kt

[![Maven Central](https://img.shields.io/maven-central/v/com.kazakago.storeflowable/storeflowable.svg)](https://search.maven.org/artifact/com.kazakago.storeflowable/storeflowable)
[![javadoc](https://javadoc.io/badge2/com.kazakago.storeflowable/storeflowable/javadoc.svg)](https://javadoc.io/doc/com.kazakago.storeflowable/storeflowable)
[![Test](https://github.com/KazaKago/StoreFlowable.kt/workflows/Test/badge.svg)](https://github.com/KazaKago/StoreFlowable.kt/actions?query=workflow%3ATest)
[![License](https://img.shields.io/github/license/kazakago/storeflowable.kt.svg)](LICENSE)

[Repository pattern](https://msdn.microsoft.com/en-us/library/ff649690.aspx) support library for Kotlin with Coroutines &amp; Flow.  
Available for Android or any Kotlin/JVM projects.  

## Overview

This library provides remote and local data abstraction and observation with [Kotlin Coroutines Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).  
Created according to the following 5 policies.  

- **Repository pattern**
    - Abstract remote and local data acquisition.
- **Single Source of Truth**
    - Looks like a single source from the user side.
- **Observer pattern**
    - Observing data with Kotlin Coroutines.
- **Return value as soon as possible**
- **Representing the state of data**

The following is the class structure of Repository pattern using this library.  

![https://user-images.githubusercontent.com/7742104/125610947-516c9508-f7fd-4466-81a8-c3ee159cb141.jpg](https://user-images.githubusercontent.com/7742104/125610947-516c9508-f7fd-4466-81a8-c3ee159cb141.jpg)

The following is an example of screen display using [`LoadingState`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt).

![https://user-images.githubusercontent.com/7742104/125714730-381eee65-4126-4ee8-991a-7fc64dfb325c.jpg](https://user-images.githubusercontent.com/7742104/125714730-381eee65-4126-4ee8-991a-7fc64dfb325c.jpg)

## Install

Add the following gradle dependency exchanging x.x.x for the latest release.  

```kotlin
implementation("com.kazakago.storeflowable:storeflowable:x.x.x")
```

Optional: if you use [`LoadingState`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) class and related functions only.  
```kotlin
implementation("com.kazakago.storeflowable:storeflowable-core:x.x.x")
```

## Basic Usage

There are only 5 things you have to implement:  

- Create data state management class
- Get data from local cache
- Save data to local cache
- Get data from remote server
- Whether the cache is valid

### 1. Create FlowableDataStateManager class

First, create a class that inherits [`FlowableDataStateManager<PARAM>`](storeflowable/src/main/java/com/kazakago/storeflowable/FlowableDataStateManager.kt).  
Put the type you want to use as a param in `<PARAM>`. If you don't need the param, put in the `Unit`.  

```kotlin
object UserStateManager : FlowableDataStateManager<UserId>()
```

[`FlowableDataStateManager<PARAM>`](storeflowable/src/main/java/com/kazakago/storeflowable/FlowableDataStateManager.kt) needs to be used in Singleton pattern, so please make it [`object class`](https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations).  

### 2. Create StoreFlowableFactory class

Next, create a class that implements [`StoreFlowableFactory<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowableFactory.kt).  
Put the type you want to use as a Data in `<DATA>`.  

An example is shown below.  

```kotlin
class UserFlowableFactory : StoreFlowableFactory<UserId, UserData> {

    private val userApi = UserApi()
    private val userCache = UserCache()

    // Create data state management class.
    override val flowableDataStateManager: FlowableDataStateManager<UserId> = UserStateManager

    // Get data from local cache.
    override suspend fun loadDataFromCache(param: UserId): UserData? {
        return userCache.load(param)
    }

    // Save data to local cache.
    override suspend fun saveDataToCache(data: UserData?, param: UserId) {
        userCache.save(param, data)
    }

    // Get data from remote server.
    override suspend fun fetchDataFromOrigin(param: UserId): UserData {
        return userApi.fetch(param)
    }

    // Whether the cache is valid.
    override suspend fun needRefresh(cachedData: UserData, param: UserId): Boolean {
        return cachedData.isExpired()
    }
}
```

You need to prepare the API access class and the cache access class.  
In this case, `UserApi` and `UserCache` classes.  

### 3. Create Repository class

After that, you can get the [`StoreFlowable<DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) class from the [`StoreFlowableFactory<PARAM, DATA>.create()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowableExtension.kt) method, and use it to build the Repository class.  
Be sure to go through the created [`StoreFlowable<DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) class when getting / updating data.  

```kotlin
class UserRepository {

    fun followUserData(userId: UserId): FlowLoadingState<UserData> {
        val userFlowable: StoreFlowable<UserData> = UserFlowableFactory().create(userId)
        return userFlowable.publish()
    }

    suspend fun updateUserData(userData: UserData) {
        val userFlowable: StoreFlowable<UserData> = UserFlowableFactory().create(userData.userId)
        userFlowable.update(userData)
    }
}
```

You can get the data in the form of [`FlowLoadingState<DATA>`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/FlowLoadingState.kt) (Same as `Flow<LoadingState<DATA>>`) by using the [`publish()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) method.  
[`LoadingState`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) class is a [Sealed Classes](https://kotlinlang.org/docs/reference/sealed-classes.html) that holds raw data.  

### 4. Use Repository class

You can observe the data by collecting [`Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).  
and branch the data state with [`doAction()`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) method or `when` statement.  

```kotlin
private fun subscribe(userId: UserId) = viewModelScope.launch {
    userRepository.followUserData(userId).collect {
        it.doAction(
            onLoading = { content: UserData? ->
                ...
            },
            onCompleted = { content: UserData, _, _ ->
                ...
            },
            onError = { exception: Exception ->
                ...
            }
        )
    }
}
```

On Android, it is recommended to pass the data to [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) or [`StateFlow`](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) with [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel) and display it on the UI.  

## Example

Refer to the [**example module**](example) for details. This module works as an Android app.  
See [GithubMetaFlowableFactory](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubMetaFlowableFactory.kt) and [GithubUserFlowableFactory](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubUserFlowableFactory.kt).  

This example accesses the [Github API](https://docs.github.com/en/free-pro-team@latest/rest).  

## Advanced Usage

### Get data without [LoadingState](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) class

If you don't need value flow and [`LoadingState`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) class, you can use [`requireData()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) or [`getData()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt).  
[`requireData()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) throws an Exception if there is no valid cache and fails to get new data.  
[`getData()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) returns null instead of Exception.  

```kotlin
interface StoreFlowable<DATA> {
    suspend fun getData(from: GettingFrom = GettingFrom.Both): DATA?
    suspend fun requireData(from: GettingFrom = GettingFrom.Both): DATA
}
```

[`GettingFrom`](storeflowable/src/main/java/com/kazakago/storeflowable/GettingFrom.kt) parameter specifies where to get the data.  

```kotlin
enum class GettingFrom {
    // Gets a combination of valid cache and remote. (Default behavior)
    Both,
    // Gets only remotely.
    Origin,
    // Gets only locally.
    Cache,
}
```

However, use [`requireData()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) or [`getData()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) only for one-shot data acquisition, and consider using [`publish()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) if possible.  

### Refresh data

If you want to ignore the cache and get new data, add `forceRefresh` parameter to [`publish()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt).  

```kotlin
interface StoreFlowable<DATA> {
    fun publish(forceRefresh: Boolean = false): FlowLoadingState<DATA>
}
```

Or you can use [`refresh()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) if you are already observing the `Flow`.  

```kotlin
interface StoreFlowable<DATA> {
    suspend fun refresh()
}
```

### Validate cache data

Use [`validate()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) if you want to verify that the local cache is valid.  
If invalid, get new data remotely.  

```kotlin
interface StoreFlowable<DATA> {
    suspend fun validate()
}
```

### Update cache data

If you want to update the local cache, use the [`update()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) method.  
`Flow` observers will be notified.  

```kotlin
interface StoreFlowable<DATA> {
    suspend fun update(newData: DATA?)
}
```

## Pagination support

This library includes pagination support.  

<img src="https://user-images.githubusercontent.com/7742104/100849417-e29be000-34c5-11eb-8dba-0149e07d5017.gif" width="280"> <img src="https://user-images.githubusercontent.com/7742104/100849432-e7f92a80-34c5-11eb-918f-377ac6c4eb9e.gif" width="280">

Inherit [`PaginationStoreFlowableFactory<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/oneway/PaginationStoreFlowableFactory.kt) instead of [`StoreFlowableFactory<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowableFactory.kt).  

An example is shown below.  

```kotlin
object UserListStateManager : FlowableDataStateManager<Unit>()
```
```kotlin
class UserListFlowableFactory : PaginationStoreFlowableFactory<Unit, List<UserData>> {

    private val userListApi = UserListApi()
    private val userListCache = UserListCache()

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = UserListStateManager

    override suspend fun loadDataFromCache(param: Unit): List<UserData>? {
        return userListCache.load()
    }

    override suspend fun saveDataToCache(newData: List<UserData>?, param: Unit) {
        userListCache.save(newData)
    }

    override suspend fun saveNextDataToCache(cachedData: List<UserData>, newData: List<UserData>, param: Unit) {
        userListCache.save(cachedData + newData)
    }

    override suspend fun fetchDataFromOrigin(param: Unit): Fetched<List<UserData>> {
        val fetchedData = userListApi.fetch(pageToken = null)
        return Fetched(data = fetchedData, nextKey = fetchedData.nextPageToken)
    }

    override suspend fun fetchNextDataFromOrigin(nextKey: String, param: Unit): Fetched<List<GithubOrg>> {
        val fetchedData = userListApi.fetch(pageToken = nextKey)
        return Fetched(data = fetchedData, nextKey = fetchedData.nextPageToken)
    }

    override suspend fun needRefresh(cachedData: List<UserData>, param: Unit): Boolean {
        return cachedData.last().isExpired()
    }
}
```

You need to additionally implements [`saveNextDataToCache()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/oneway/PaginationStoreFlowableFactory.kt) and [`fetchNextDataFromOrigin()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/oneway/PaginationStoreFlowableFactory.kt).  
When saving the data, combine the cached data and the new data before saving.  

And then, You can get the state of additional loading from the `next` parameter of `onCompleted {}`.  

```kotlin
val userFlowable = UserFlowableFactory().create(userId)
userFlowable.publish().collect {
    it.doAction(
        onLoading = { contents: List<UserData>? ->
            // Whole (Initial) data loading.
        },
        onCompleted = { contents: List<UserData>, next: AdditionalLoadingState, _ ->
            // Whole (Initial) data loading completed.
            next.doAction(
                onFixed = { canRequestAdditionalData: Boolean ->
                    // No additional processing.
                },
                onLoading = {
                    // Additional data loading.
                },
                onError = { exception: Exception ->
                    // Additional loading error.
                }
            )
        },
        onError = { exception: Exception ->
            // Whole (Initial) data loading error.
        }
    )
}
```

On Android, To display in the [`RecyclerView`](https://developer.android.com/jetpack/androidx/releases/recyclerview), Please use the difference update function. See also [`DiffUtil`](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil).  

### Request additional data

You can request additional data for paginating using the [`requestNextData()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/oneway/PaginationStoreFlowable.kt) method.

```kotlin
interface PaginationStoreFlowable<DATA> {
    suspend fun requestNextData(continueWhenError: Boolean = true)
}
```

## Pagination Example

The [GithubOrgsFlowableFactory](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubOrgsFlowableFactory.kt) and [GithubReposFlowableFactory](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubReposFlowableFactory.kt) classes in [**example module**](example) implement pagination.  

## Two-Way pagination support

This library also includes two-way pagination support.  

Inherit [`TwoWayPaginationStoreFlowableFactory<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/twoway/TwoWayPaginationStoreFlowableFactory.kt) instead of [`StoreFlowableFactory<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowableFactory.kt).  

### Request next & previous data

You can request additional data for paginating using the [`requestNextData()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/twoway/TwoWayPaginationStoreFlowable.kt), [`requestPrevData()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/twoway/TwoWayPaginationStoreFlowable.kt) method.  

```kotlin
interface TwoWayPaginationStoreFlowable<DATA> {
    suspend fun requestNextData(continueWhenError: Boolean = true)
    suspend fun requestPrevData(continueWhenError: Boolean = true)
}
```

## Two-Way pagination Example

The [GithubTwoWayReposFlowableFactory](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubTwoWayReposFlowableFactory.kt) classes in [**example module**](example) implement two-way pagination.  

## License

This project is licensed under the **Apache-2.0 License** - see the [LICENSE](LICENSE) file for details.  
