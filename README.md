## WeChat Moments

It's a skeleton application to fetch the moments of contact from WeChat. It could be used for backup, crawl and so on.

### Prerequisites

* Appium
* Android SDK
* Java 8

### How to build

```shell
cd WeChatMoments
./gradlew build
```

### How to run

1. run **Appium** to connect either actual Android device or emulator
1. run below command

```shell
export PATH=<adb path from Android SDK>:$PATH
java -jar build/libs/WeChatMoments-all-1.0.jar -c <wechat id>
```

#### Test Env
* Appium 1.4.1
* Wechat for Android 6.2
* Google Nexus 4(Android 5.1.1)
* Mac 10.10.3