<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

<!--
No CAMERA permission is required: the ML Kit Document Scanner runs inside
Google Play services and uses its own camera permission.
-->

<application
android:allowBackup="true"
android:label="DocScanner"
android:supportsRtl="true"
android:theme="@android:style/Theme.Material.Light.NoActionBar">

<activity
android:name=".MainActivity"
android:exported="true">
<intent-filter>
<action android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
</activity>

<!-- Lets us share exported PDF/ZIP/TXT files with other apps -->
<provider
android:name="androidx.core.content.FileProvider"
android:authorities="${applicationId}.fileprovider"
android:exported="false"
android:grantUriPermissions="true">
<meta-data
android:name="android.support.FILE_PROVIDER_PATHS"
android:resource="@xml/file_paths" />
</provider>
</application>
</manifest>