# Open Socket android-client
[![](https://jitpack.io/v/osocket/android-client.svg)](https://jitpack.io/#osocket/android-client)

## Step 1. Add the JitPack repository to your build file 
Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
   }
}
```

## Step 2. Add the dependency
```
dependencies {
  implementation 'com.github.osocket:android-client:1.0.11'
}
```

# configuration

kotlin
```KOTLIN
var osocket = OpenSocket(this);

osocket.setProjectConfig(project_id, client_id)
osocket.setDeveloperConfig(developer_id)
osocket.connect()

// You can change the priority of the notification
// osocket.setPriority(NotificationCompat.PRIORITY_HIGH)
```

java
```JAVA
OpenSocket osocket = new OpenSocket(this);

osocket.setProjectConfig(project_id, client_id);
osocket.setDeveloperConfig(developer_id);
osocket.connect();
```

### Methods:
onConnect, onReceiveToken, onReceiveMessage, onDisconnect, onConectionError

kotlin sample
```KOTLIN
 osocket.onReceiveToken = {
      Log.i("opensocket","service : onReceiveToken : $it")
 }

osocket.onConnect = {
     Log.i("opensocket","service : onConnect")
}

osocket.onReceiveMessage = {
      Log.i("opensocket", "message:$it")
}
```
