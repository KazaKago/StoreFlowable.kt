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
- Save data to local cache
- Get data from local cache
- Get data from remote server
- Whether the cache is valid

### 1. Create FlowableDataStateManager class

First, create a class that inherits `FlowableDataStateManager<KEY>`.  
Put the type you want to use as a key in `<KEY>`. If you don't need the key, put in the `Unit`.  

```kotlin
object UserStateManager : FlowableDataStateManager<UserId>()
```

`FlowableDataStateManager<KEY>` needs to be used in Singleton pattern, so please make it `object class`.  

### 2. Create StoreFlowable class

Next, create a class that inherits `AbstractStoreFlowable<KEY, DATA>`.  
Put the type you want to use as a Data in `<DATA>`.  

An example is shown below.  

```kotlin
class UserFlowable(val userId: UserId) : AbstractStoreFlowable<UserId, UserData>(userId) {

    private val userApi = UserApi()
    private val userCache = UserCache()

    override val flowableDataStateManager: FlowableDataStateManager<UserId> = UserStateManager

    override suspend fun loadData(): UserData? {
        return userCache.load(userId)
    }

    override suspend fun saveData(data: UserData?) {
        userCache.save(userId, data)
    }

    override suspend fun fetchOrigin(): UserData {
        return userApi.fetchData(userId)
    }

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

Refer to the [sample module](https://github.com/KazaKago/StoreFlowable/tree/master/sample) for details. This module works as an Android app.  

## Advanced Usage

### Get data without [State](https://github.com/KazaKago/StoreFlowable/blob/master/library-core/src/main/java/com/kazakago/storeflowable/core/State.kt) class

[WIP]

### Request newest data

[WIP]

### Validate cache data

[WIP]

### Update cache data

[WIP]

### Paging support

[WIP]

## License

This project is licensed under the **Apache-2.0 License** - see the [LICENSE](LICENSE) file for details.  
