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
  implementation 'com.github.osocket:android-client:1.0.2'
}
```

# configuration

kotlin
```KOTLIN
var osocket = OpenSocket(this);

osocket.setProjectConfig(project_id, client_id)
osocket.setDeveloperConfig(developer_id)
osocket.connect()
```

java
```JAVA
OpenSocket osocket = new OpenSocket(this);

osocket.setProjectConfig(project_id, client_id);
osocket.setDeveloperConfig(developer_id);
osocket.connect();
```
