# PinLockView

Simple Android PIN entry `View` with:

- **Indicator dots** (fills as user types)
- **Numeric keypad** (0–9) + **delete**
- **Callbacks** for pin changes + completion
- **Optional** ripple + haptic feedback

## Screenshot / GIF

<img width="225" height="500" alt="Screenrecorder-2026-04-28-16-14-44-511-ezgif com-video-to-gif-converter" src="https://github.com/user-attachments/assets/c4b31fe4-409e-4af4-9f91-72a9053c93f4" />

```

## Installation (JitPack)

1) Add JitPack repository:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

2) Add dependency:

```kotlin
dependencies {
    implementation("com.simple.pinlockview:pinlockview:v1.0.0")
}
```

## Usage

### XML

```xml
<com.simple.pinlockview.PinLockView
    android:id="@+id/pinLock"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:enableRipple="true"
    app:enableHaptic="true"
    app:buttonColor="@android:color/white"
    app:buttonTextColor="@android:color/black"
    app:buttonStrokeColor="@android:color/darker_gray"
    app:rippleColor="@android:color/darker_gray" />
```

### Kotlin

```kotlin
import com.simple.pinlockview.PinLockView

val pinLock = findViewById<PinLockView>(R.id.pinLock)
pinLock.maxLength = 4
pinLock.setPinLockListener(object : PinLockView.PinLockListener {
    override fun onPinChanged(pin: String) {
        // called on each digit
    }

    override fun onComplete(pin: String) {
        // called when pin length == maxLength
    }
})
```

