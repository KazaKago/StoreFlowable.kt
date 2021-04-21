# StoreFlowable.kt

[![Download](https://api.bintray.com/packages/kazakago/maven/storeflowable/images/download.svg)](https://bintray.com/kazakago/maven/storeflowable/_latestVersion)
[![Test](https://github.com/KazaKago/StoreFlowable.kt/workflows/Test/badge.svg)](https://github.com/KazaKago/StoreFlowable.kt/actions?query=workflow%3ATest)
[![license](https://img.shields.io/github/license/kazakago/storeflowable.kt.svg)](LICENSE)

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

![https://user-images.githubusercontent.com/7742104/103459432-bbe5f900-4d52-11eb-829c-fba62cb0ea11.jpg](https://user-images.githubusercontent.com/7742104/103459432-bbe5f900-4d52-11eb-829c-fba62cb0ea11.jpg)

The following is an example of screen display using [`State`](library-core/src/main/java/com/kazakago/storeflowable/core/State.kt).

![https://user-images.githubusercontent.com/7742104/103459431-bab4cc00-4d52-11eb-983a-2087dd3100a0.jpg](https://user-images.githubusercontent.com/7742104/103459431-bab4cc00-4d52-11eb-983a-2087dd3100a0.jpg)

## Install

Add the following gradle dependency exchanging x.x.x for the latest release.  

```groovy
implementation 'com.kazakago.storeflowable:storeflowable:x.x.x'
```

Optional: if you use [`State`](library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class and related functions only.  
```groovy
implementation 'com.kazakago.storeflowable:storeflowable-core:x.x.x'
```

## Basic Usage

There are only 5 things you have to implement:  

- Create data state management class
- Get data from local cache
- Save data to local cache
- Get data from remote server
- Whether the cache is valid

### 1. Create FlowableDataStateManager class

First, create a class that inherits [`FlowableDataStateManager<KEY>`](library/src/main/java/com/kazakago/storeflowable/FlowableDataStateManager.kt).  
Put the type you want to use as a key in `<KEY>`. If you don't need the key, put in the `Unit`.  

```kotlin
object UserStateManager : FlowableDataStateManager<UserId>()
```

[`FlowableDataStateManager<KEY>`](library/src/main/java/com/kazakago/storeflowable/FlowableDataStateManager.kt) needs to be used in Singleton pattern, so please make it [`object class`](https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations).  

### 2. Create StoreFlowableCallback class

Next, create a class that implements [`StoreFlowableCallback<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/StoreFlowableCallback.kt).
Put the type you want to use as a Data in `<DATA>`.  

An example is shown below.  

```kotlin
class UserFlowableCallback(userId: UserId) : StoreFlowableCallback<UserId, UserData> {

    private val userApi = UserApi()
    private val userCache = UserCache()

    // Set the key for input / output of data. If you don't need the key, put in the Unit.
    override val key: UserId = userId

    // Create data state management class.
    override val flowableDataStateManager: FlowableDataStateManager<UserId> = UserStateManager

    // Get data from local cache.
    override suspend fun loadDataFromCache(): UserData? {
        return userCache.load(key)
    }

    // Save data to local cache.
    override suspend fun saveDataToCache(data: UserData?) {
        userCache.save(key, data)
    }

    // Get data from remote server.
    override suspend fun fetchDataFromOrigin(): FetchingResult<UserData> {
        val data = userApi.fetch(key)
        return FetchingResult(data = data)
    }

    // Whether the cache is valid.
    override suspend fun needRefresh(cachedData: UserData): Boolean {
        return cachedData.isExpired()
    }
}
```

You need to prepare the API access class and the cache access class.  
In this case, `UserApi` and `UserCache` classes.  

### 3. Create Repository class

After that, you can get the [`StoreFlowable<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) class from the [`StoreFlowableCallback<KEY, DATA>.create()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowableExtension.kt) method, and use it to build the Repository class.  
Be sure to go through the created [`StoreFlowable<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) class when getting / updating data.  

```kotlin
class UserRepository {

    fun followUserData(userId: UserId): FlowableState<UserData> {
        val userFlowable: StoreFlowable<UserId, UserData> = UserFlowableCallback(userId).create()
        return userFlowable.publish()
    }

    suspend fun updateUserData(userData: UserData) {
        val userFlowable: StoreFlowable<UserId, UserData> = UserFlowableCallback(userData.userId).create()
        userFlowable.update(userData)
    }
}
```

You can get the data in the form of `FlowableState<DATA>` (Same as `Flow<State<DATA>>`) by using the [`publish()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) method.  
[`State`](library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class is a [Sealed Classes](https://kotlinlang.org/docs/reference/sealed-classes.html) that holds raw data.

### 4. Use Repository class

You can observe the data by collecting [`Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).  
and branch the data state with `doAction()` method or `when` statement.  

```kotlin
private fun subscribe(userId: UserId) = viewModelScope.launch {
    userRepository.followUserData(userId).collect {
        it.doAction(
            onFixed = {
                ...
            },
            onLoading = {
                ...
            },
            onError = { exception ->
                ...
            }
        )
        it.content.doAction(
            onExist = { userData ->
                ...
            },
            onNotExist = {
                ...
            }
        )
    }
}
```

On Android, it is recommended to pass the data to [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) with [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel) and display it on the UI.  

## Example

Refer to the [**example module**](example) for details. This module works as an Android app.  
See [GithubMetaFlowableCallback](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubMetaFlowableCallback.kt) and [GithubUserFlowableCallback](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubUserFlowableCallback.kt).

This example accesses the [Github API](https://docs.github.com/en/free-pro-team@latest/rest).  

## Advanced Usage

### Get data without [State](library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class

If you don't need value flow and [`State`](library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class, you can use [`requireData()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) or [`getData()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt).  
[`requireData()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) throws an Exception if there is no valid cache and fails to get new data.  
[`getData()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) returns null instead of Exception.  

```kotlin
interface StoreFlowable<KEY, DATA> {
    suspend fun get(type: AsDataType = AsDataType.Mix): DATA
}
```

[`GettingFrom`](library/src/main/java/com/kazakago/storeflowable/GettingFrom.kt) parameter specifies where to get the data.  

```kotlin
enum class GettingFrom {
    // Gets a combination of valid cache and remote. (Default behavior)
    Mix,
    // Gets only remotely.
    FromOrigin,
    // Gets only locally.
    FromCache,
}
```

However, use [`requireData()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) or [`getData()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) only for one-shot data acquisition, and consider using [`publish()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) if possible.  

### Refresh data

If you want to ignore the cache and get new data, add `forceRefresh` parameter to [`publish()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt).  

```kotlin
interface StoreFlowable<KEY, DATA> {
    fun publish(forceRefresh: Boolean = false): Flow<State<DATA>>
}
```

Or you can use [`refresh()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) if you are already observing the `Flow`.  

```kotlin
interface StoreFlowable<KEY, DATA> {
    suspend fun refresh(clearCacheWhenFetchFails: Boolean = true, continueWhenError: Boolean = true)
}
```

### Validate cache data

Use [`validate()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) if you want to verify that the local cache is valid.  
If invalid, get new data remotely.  

```kotlin
interface StoreFlowable<KEY, DATA> {
    suspend fun validate()
}
```

### Update cache data

If you want to update the local cache, use the [`update()`](library/src/main/java/com/kazakago/storeflowable/StoreFlowable.kt) method.  
`Flow` observers will be notified.  

```kotlin
interface StoreFlowable<KEY, DATA> {
    suspend fun update(newData: DATA?)
}
```

### Pagination support

This library includes pagination support.  

<img src="https://user-images.githubusercontent.com/7742104/100849417-e29be000-34c5-11eb-8dba-0149e07d5017.gif" width="280"> <img src="https://user-images.githubusercontent.com/7742104/100849432-e7f92a80-34c5-11eb-918f-377ac6c4eb9e.gif" width="280">

Inherit [`PaginatingStoreFlowableCallback<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/pagination/PaginatingStoreFlowableCallback.kt) instead of [`StoreFlowableCallback<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/StoreFlowableCallback.kt).

An example is shown below.  

```kotlin
object UserListStateManager : FlowableDataStateManager<Unit>()
```
```kotlin
class UserListFlowableCallback : PagingStoreFlowableCallback<Unit, List<UserData>> {

    private val userListApi = UserListApi()
    private val userListCache = UserListCache()

    override val key: Unit = Unit

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = UserListStateManager

    override suspend fun loadDataFromCache(): List<UserData>? {
        return userListCache.load()
    }

    override suspend fun saveDataToCache(newData: List<UserData>?) {
        userListCache.save(newData)
    }

    override suspend fun saveAdditionalDataToCache(cachedData: List<UserData>?, newData: List<UserData>) {
        val mergedData = (cachedData ?: emptyList()) + newData
        userListCache.save(mergedData)
    }

    override suspend fun fetchDataFromOrigin(): FetchingResult<List<UserData>> {
        val fetchedData = userListApi.fetch(1)
        return FetchingResult(data = fetchedData, noMoreAdditionalData = fetchedData.isEmpty())
    }

    override suspend fun fetchAdditionalDataFromOrigin(cachedData: List<GithubOrg>?): FetchingResult<List<GithubOrg>> {
        val page = (cachedData?.size ?: 0) / 10 + 1
        val fetchedData = userListApi.fetch(page)
        return FetchingResult(data = fetchedData, noMoreAdditionalData = fetchedData.isEmpty())
    }

    override suspend fun needRefresh(cachedData: List<UserData>): Boolean {
        return cachedData.last().isExpired()
    }
}
```

You need to additionally implements `saveAdditionalDataToCache()` and `fetchAdditionalDataFromOrigin()`.  
When saving the data, combine the cached data and the new data before saving.  

The [GithubOrgsFlowableCallback](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubOrgsFlowableCallback.kt) and [GithubReposFlowableCallback](example/src/main/java/com/kazakago/storeflowable/example/flowable/GithubReposFlowableCallback.kt) classes in [**example module**](example) implement paging.

## License

This project is licensed under the **Apache-2.0 License** - see the [LICENSE](LICENSE) file for details.  
