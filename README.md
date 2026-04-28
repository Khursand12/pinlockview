# PinLockView

Simple Android PIN entry `View` with:

- **Indicator dots** (fills as user types)
- **Numeric keypad** (0–9) + **delete**
- **Callbacks** for pin changes + completion
- **Optional** ripple + haptic feedback

## Screenshot / GIF

Add a screenshot or GIF here (highly recommended). Example:

```md
![PinLockView demo](docs/pinlockview.gif)
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
    implementation("com.github.Khursand12:<RepoName>:v1.0.0")
}
```

Replace `<RepoName>` with your GitHub repository name (example: `PinLockView`).

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

