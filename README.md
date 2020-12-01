# StoreFlowable

[![Download](https://api.bintray.com/packages/kazakago/maven/storeflowable/images/download.svg)](https://bintray.com/kazakago/maven/storeflowable/_latestVersion)
[![Build Status](https://app.bitrise.io/app/ab3f1be16f6a6b8b/status.svg?token=XFKB2sqF4-hFdTIyzpganQ&branch=master)](https://app.bitrise.io/app/ab3f1be16f6a6b8b)
[![license](https://img.shields.io/github/license/kazakago/storeflowable.svg)](LICENSE)

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

![https://user-images.githubusercontent.com/7742104/100694803-2235cf80-33d3-11eb-917c-a2ad24bd1c32.jpg](https://user-images.githubusercontent.com/7742104/100694803-2235cf80-33d3-11eb-917c-a2ad24bd1c32.jpg)

The following is an example of screen display using [`State`](https://github.com/KazaKago/StoreFlowable/blob/master/library-core/src/main/java/com/kazakago/storeflowable/core/State.kt).

![https://user-images.githubusercontent.com/7742104/100700024-c9206880-33df-11eb-8026-8d0ff3b42c7b.jpg](https://user-images.githubusercontent.com/7742104/100700024-c9206880-33df-11eb-8026-8d0ff3b42c7b.jpg)

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

[`FlowableDataStateManager<KEY>`](library/src/main/java/com/kazakago/storeflowable/FlowableDataStateManager.kt) needs to be used in Singleton pattern, so please make it `object class`.  

### 2. Create StoreFlowable class

Next, create a class that inherits [`AbstractStoreFlowable<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/AbstractStoreFlowable.kt).  
Put the type you want to use as a Data in `<DATA>`.  

An example is shown below.  

```kotlin
class UserFlowable(val userId: UserId) : AbstractStoreFlowable<UserId, UserData>(userId) {

    private val userApi = UserApi()
    private val userCache = UserCache()

    // Create data state management class.
    override val flowableDataStateManager: FlowableDataStateManager<UserId> = UserStateManager

    // Get data from local cache.
    override suspend fun loadData(): UserData? {
        return userCache.load(userId)
    }

    // Save data to local cache.
    override suspend fun saveData(data: UserData?) {
        userCache.save(userId, data)
    }

    // Get data from remote server.
    override suspend fun fetchOrigin(): UserData {
        return userApi.fetch(userId)
    }

    // Whether the cache is valid.
    override suspend fun needRefresh(data: UserData): Boolean {
        return data.isExpired()
    }
}
```

You need to prepare the API access class and the cache access class.  
In this case, `UserAPI` and `UserCache` classes.  

### 3. Create Repository class

After that, create the Repository class as usual.  
Be sure to go through the created `StoreFlowable` class when getting / updating data.  


```kotlin
class UserRepository {

    fun followUserData(userId: UserId): Flow<State<UserData>> {
        return UserFlowable(userId).asFlow()
    }
    
    suspend fun updateUserData(userData: UserData) {
        UserFlowable(userData.userId).update(userData)
    }
}
```

You can get the data in the form of `Flow<State<DATA>>` by using the `asFlow()`.  
[`State`](https://github.com/KazaKago/StoreFlowable/blob/master/library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class is a [Sealed Classes](https://kotlinlang.org/docs/reference/sealed-classes.html) that holds raw data.  

### 4. Use Repository class

You can observe the data by collecting [`Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).  
and branch the data state with `doAction()` method or `when` statement.  

```kotlin
private fun subscribe() = viewModelScope.launch {
    userRepository.followUserData().collect {
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

Refer to the [**sample module**](https://github.com/KazaKago/StoreFlowable/tree/master/sample) for details. This module works as an Android app.  
See [GithubMetaFlowable](sample/src/main/java/com/kazakago/storeflowable/sample/flowable/GithubMetaFlowable.kt) and [GithubUserFlowable](sample/src/main/java/com/kazakago/storeflowable/sample/flowable/GithubUserFlowable.kt).  

This example accesses the [Github API](https://docs.github.com/en/free-pro-team@latest/rest).  

## Advanced Usage

### Get data without [State](https://github.com/KazaKago/StoreFlowable/blob/master/library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class

If you don't need [State](https://github.com/KazaKago/StoreFlowable/blob/master/library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class, you can use `get()` or `getOrNull()`.  
`get()` throws an Exception if there is no valid cache and fails to get new data.  
`getOrNull()` returns null instead of Exception.  

```kotlin
class StoreFlowable {
    suspend fun get(type: AsDataType = AsDataType.Mix): DATA
    suspend fun getOrNull(type: AsDataType = AsDataType.Mix): DATA?
}
```

`AsDataType` parameter specifies where to get the data.  

```kotlin
enum class AsDataType {
    // Gets a combination of valid cache and remote. (Default behavior)
    Mix,
    // Gets only remotely.
    FromOrigin,
    // Gets only locally.
    FromCache,
}
```

However, use `get()` or `getOrNull()` only for one-shot data acquisition, and consider using `asFlow()` if possible.  

### Request newest data

If you want to ignore the cache and get new data, add `forceRefresh` parameter to `asFlow()`.  

```kotlin
class StoreFlowable {
    fun asFlow(forceRefresh: Boolean = false): Flow<State<List<DATA>>>
}
```

Or you can use `request()` if you are already observing the `Flow`.  

```kotlin
class StoreFlowable {
    suspend fun request()
}
```

### Validate cache data

Use `validate()` if you want to verify that the local cache is valid.  
If invalid, get new data remotely.  

```kotlin
class StoreFlowable {
    suspend fun validate()
}
```

### Update cache data

If you want to update the local cache, use the `update()` method.  
`Flow` observers will be notified.  

```kotlin
class StoreFlowable {
    suspend fun update(newData: DATA?)
}
```

### Paging support

This library includes Paging support.  
Inherit [`AbstractPagingStoreFlowable<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/paging/AbstractPagingStoreFlowable.kt) instead of [`AbstractStoreFlowable<KEY, DATA>`](library/src/main/java/com/kazakago/storeflowable/AbstractStoreFlowable.kt).  

An example is shown below.  

```kotlin
object UserListStateManager : FlowableDataStateManager<Unit>()
```
```kotlin
class UserListFlowable : AbstractPagingStoreFlowable<Unit, User>(Unit) {

    private val userListApi = UserListApi()
    private val userListCache = UserListCache()

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = UserListStateManager

    override suspend fun loadData(): List<User>? {
        return userListCache.load()
    }

    override suspend fun saveData(data: List<User>?, additionalRequest: Boolean) {
        userListCache.save(data)
    }

    override suspend fun fetchOrigin(data: List<User>?, additionalRequest: Boolean): List<GithubRepo> {
        val page = if (additionalRequest) ((data?.size ?: 0) / 10 + 1) else 1
        return userListApi.fetch(page)
    }

    override suspend fun needRefresh(data: List<User>): Boolean {
        return data.last().isExpired()
    }
}
```

You can have the data in a list. The retrieved remote data will be merged automatically.  
`additionalRequest: Boolean` parameter indicates whether to load additionally. use if necessary.  

The [GithubOrgsFlowable](https://github.com/KazaKago/StoreFlowable/blob/master/sample/src/main/java/com/kazakago/storeflowable/sample/flowable/GithubOrgsFlowable.kt) and [GithubReposFlowable](https://github.com/KazaKago/StoreFlowable/blob/master/sample/src/main/java/com/kazakago/storeflowable/sample/flowable/GithubReposFlowable.kt) classes in [**sample module**](https://github.com/KazaKago/StoreFlowable/tree/master/sample) implement paging.  

## License

This project is licensed under the **Apache-2.0 License** - see the [LICENSE](LICENSE) file for details.  
