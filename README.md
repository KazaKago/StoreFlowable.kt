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

## Get started

There are only 2 things you have to implement:

- Create a class to manage the in-app cache.
- Create a class to get data from origin server.

### 1. Create a class to manage the in-app cache

First, create a class that inherits [`Cacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/Cacher.kt).  
Put the type you want to use as a param in `<PARAM>`. If you don't need the param, put in the `Unit`.

```kotlin
object UserCacher : Cacher<UserId, UserData>()
```

[`Cacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/Cacher.kt) needs to be used in Singleton pattern, so please make it [`object class`](https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations).

### 2. Create a class to get data from origin server

Next, create a class that implements [`Fetcher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/fetcher/Fetcher.kt).  
Put the type you want to use as a param in `<PARAM>`. If you don't need the param, put in the `Unit`.

An example is shown below.

```kotlin
class UserFetcher : Fetcher<UserId, UserData> {

    private val userApi = UserApi()

    // Get data from remote server.
    override suspend fun fetch(param: UserId): UserData {
        return userApi.fetch(param)
    }
}
```

You need to prepare the API access class.  
In this case, `UserApi` classe.

### 3. Build StoreFlowable from Cacher & Fetcher class

After that, you can get the [`StoreFlowable<DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) class from the [`StoreFlowable<PARAM, DATA>.from(Cacher, Fetcher, PARAM)`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowableExtension.kt) method, and use it to build the Repository class.  
Be sure to go through the created [`StoreFlowable<DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) class when getting / updating data.

```kotlin
fun getUserDataFlow(userId: UserId): FlowLoadingState<UserData> {
    val userFlowable: StoreFlowable<UserData> = StoreFlowable.from(userCacher, userFetcher, userId)
    return userFlowable.publish()
}
```

You can get the data in the form of [`FlowLoadingState<DATA>`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/FlowLoadingState.kt) (Same as `Flow<LoadingState<DATA>>`) by using the [`publish()`](storeflowable/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) method.  
[`LoadingState`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) class is a [Sealed Classes](https://kotlinlang.org/docs/reference/sealed-classes.html) that holds raw data.

### 4. Subscribe `FlowLoadingState<DATA>`

You can observe the data by collecting [`Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).  
and branch the data state with [`doAction()`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/LoadingState.kt) method or `when` statement.

```kotlin
suspend fun subscribe(userId: UserId) {
    getUserDataFlow(userId).collect {
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
See [GithubMetaCacher](example/src/main/java/com/kazakago/storeflowable/example/cacher/GithubMetaCacher.kt) + [GithubMetaFetcher](example/src/main/java/com/kazakago/storeflowable/example/fetcher/GithubMetaFetcher.kt) or [GithubUserCacher](example/src/main/java/com/kazakago/storeflowable/example/cacher/GithubUserCacher.kt) + [GithubUserFetcher](example/src/main/java/com/kazakago/storeflowable/example/fetcher/GithubUserFetcher.kt).

This example accesses the [Github API](https://docs.github.com/en/free-pro-team@latest/rest).

## Other usage of `StoreFlowable<T>` class

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

## `FlowLoadingState<T>` operators

### Map `FlowLoadingState<T>`

Use [`mapContent(transform)`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/FlowLoadingStateMapper.kt) to transform content in `FlowLoadingStates<T>`.

```kotlin
val state: FlowLoadingState<Int> = ...
val mappedState: FlowLoadingState<String> = state.mapContent { value: Int ->
    value.toString()
}
```

### Combine multiple `FlowLoadingState<T>`

Use [`combineState(state, transform)`](storeflowable-core/src/main/java/com/kazakago/storeflowable/core/FlowLoadingStateCombiner.kt) to combine multiple `FlowLoadingStates<T>`.

```kotlin
val state1: FlowLoadingState<Int> = ...
val state2: FlowLoadingState<Int> = ...
val combinedState: FlowLoadingState<Int> = state1.combineState(state2) { value1: Int, value2: Int ->
    value1 + value2
}
```

## Manage Cache

### Manage cache expire time

You can easily set the cache expiration time. Override expireSeconds variable in your [`Cacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/Cacher.kt) class.
The default value is `Long.MAX_VALUE` (= will NOT expire).

```kotlin
object UserCacher : Cacher<UserId, UserData>() {
    override val expireSeconds = 60 * 30 // expiration time is 30 minutes.
}
```

### Persist data

If you want to make the cached data persistent, override the method of your [`Cacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/Cacher.kt) class.

```kotlin
object UserCacher : Cacher<UserId, UserData>() {

    override val expireSeconds = 60 * 30 // expiration time is 30 minutes.

    // Save the data for each parameter in any store.
    override suspend fun saveData(data: UserData?, param: UserId) {
        ...
    }

    // Get the data from the store for each parameter.
    override suspend fun loadData(param: UserId): UserData? {
        ...
    }

    // Save the epoch time for each parameter to manage the expiration time.
    // If there is no expiration time, no override is needed.
    override suspend fun saveDataCachedAt(epochSeconds: Long, param: UserId) {
        ...
    }

    // Get the date for managing the expiration time for each parameter.
    // If there is no expiration time, no override is needed.
    override suspend fun loadDataCachedAt(param: UserId): Long? {
        ...
    }
}
```

## Pagination support

This library includes pagination support.

<img src="https://user-images.githubusercontent.com/7742104/100849417-e29be000-34c5-11eb-8dba-0149e07d5017.gif" width="280"> <img src="https://user-images.githubusercontent.com/7742104/100849432-e7f92a80-34c5-11eb-918f-377ac6c4eb9e.gif" width="280">

Inherit [`PaginationCacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/PaginationCacher.kt) & [`PaginationFetcher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/fetcher/PaginationFetcher.kt) instead of [`Cacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/Cacher.kt) & [`Fetcher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/fetcher/Fetcher.kt).

An example is shown below.

```kotlin
object UserListCacher : PaginationCacher<Unit, UserData>()

class GithubOrgsFetcher : PaginationFetcher<Unit, UserData> {

    private val userListApi = UserListApi()

    override suspend fun fetch(param: Unit): PaginationFetcher.Result<UserData> {
        val fetched = userListApi.fetch(null, 20)
        return PaginationFetcher.Result(data = fetched.data, nextRequestKey = fetched.nextPageToken)
    }

    override suspend fun fetchNext(nextKey: String, param: Unit): PaginationFetcher.Result<UserData> {
        val fetched = userListApi.fetch(nextKey.toLong(), 20)
        return PaginationFetcher.Result(data = fetched.data, nextRequestKey = fetched.nextPageToken)
    }
}
```

You need to additionally implements [`fetchNext(nextKey: String, param: Unit)`](storeflowable/src/main/java/com/kazakago/storeflowable/fetcher/PaginationFetcher.kt).  

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

The [GithubOrgsCacher](example/src/main/java/com/kazakago/storeflowable/example/cacher/GithubOrgsCacher.kt) + [GithubOrgsFetcher](example/src/main/java/com/kazakago/storeflowable/example/fetcher/GithubOrgsFetcher.kt) or [GithubReposCacher](example/src/main/java/com/kazakago/storeflowable/example/cacher/GithubReposCacher.kt) + [GithubReposFetcher](example/src/main/java/com/kazakago/storeflowable/example/fetcher/GithubReposFetcher.kt) classes in [**example module**](example) implement pagination.

## Two-Way pagination support

This library also includes two-way pagination support.

Inherit [`TwoWayPaginationCacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/TwoWayPaginationCacher.kt) & [`TwoWayPaginationFetcher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/fetcher/TwoWayPaginationFetcher.kt) instead of [`Cacher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/cacher/TwoWayPaginationCacher.kt) & [`Fetcher<PARAM, DATA>`](storeflowable/src/main/java/com/kazakago/storeflowable/fetcher/TwoWayPaginationFetcher.kt).

### Request next & previous data

You can request additional data for paginating using the [`requestNextData()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/twoway/TwoWayPaginationStoreFlowable.kt), [`requestPrevData()`](storeflowable/src/main/java/com/kazakago/storeflowable/pagination/twoway/TwoWayPaginationStoreFlowable.kt) method.

```kotlin
interface TwoWayPaginationStoreFlowable<DATA> {
    suspend fun requestNextData(continueWhenError: Boolean = true)
    suspend fun requestPrevData(continueWhenError: Boolean = true)
}
```

## Two-Way pagination Example

The [GithubTwoWayReposCacher](example/src/main/java/com/kazakago/storeflowable/example/cacher/GithubTwoWayReposCacher.kt) + [GithubTwoWayReposFetcher](example/src/main/java/com/kazakago/storeflowable/example/fetcher/GithubTwoWayReposFetcher.kt) classes in [**example module**](example) implement two-way pagination.

## License

This project is licensed under the **Apache-2.0 License** - see the [LICENSE](LICENSE) file for details.  
