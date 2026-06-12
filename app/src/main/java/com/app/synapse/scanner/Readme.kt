# DocScanner — a vFlat-style document scanner (Google libraries only)

A Kotlin + Jetpack Compose skeleton for a document-scanning app: scan pages,
organize them into folders, and export to **PDF**, **ZIP**, or **TXT** (via OCR).
It mirrors the vFlat flow in your screenshots (a Library of folders + a "More"
sheet of actions) **using only free, first-party libraries** — no third-party code.

## Dependency map (everything here is Google / AndroidX / Java stdlib)

| Need | What it uses | Who owns it |
|------|--------------|-------------|
| Language | Kotlin | JetBrains (toolchain) |
| UI | Jetpack Compose + Material 3 | AndroidX |
| Scanning, edge detection, cleanup | ML Kit Document Scanner API | Google |
| OCR (Create TXT) | ML Kit Text Recognition (on-device) | Google |
| Folder/page storage | Room | AndroidX |
| PDF export | `android.graphics.pdf.PdfDocument` | Android SDK (built-in) |
| ZIP export | `java.util.zip` | Java standard library |
| Image loading | `BitmapFactory` (no Coil/Glide) | Android SDK (built-in) |
| Sharing exports | `androidx.core.content.FileProvider` | AndroidX |

The only "external" pieces are the build toolchain (Kotlin, Android Gradle Plugin,
KSP) which every Android project needs — there are **no third-party runtime
libraries**.

## How to use these files

1. In Android Studio, create a new project → **Empty Activity** (the Compose
template), package name `com.example.docscanner`, min SDK 24+ (works down to 21).
2. Copy the files in this folder over the generated ones, keeping the same paths
under `app/src/main/`.
3. Open `app/build.gradle.kts` and make sure these are present: the
`com.google.devtools.ksp` plugin (match its version to your Kotlin version),
the Room dependencies, the ML Kit dependencies, and `material-icons-extended`.
4. Let Android Studio bump the listed versions to the latest stable on Gradle sync
(the numbers here are a known-good starting point, not necessarily the newest).
5. Run on a real device or an emulator with Google Play services.

## Things worth knowing

- **Device requirements:** the ML Kit Document Scanner needs API 21+ and at least
1.7 GB of total RAM, otherwise it returns an `UNSUPPORTED` error.
- **No camera permission:** scanning runs inside Google Play services, so the app
needs no `CAMERA` permission of its own.
- **First-run download:** the scanner's models/UI (~300 KB) download via Play
services on first use, then work offline.
- The scanner UI itself is Google's standard flow (you can't fully restyle it).
Everything around it — the Library, folders, the More sheet, exports — is yours.

## How it maps to the screenshots

- **Library grid of folders** → `LibraryScreen` + Room (`observeFolders`).
- **"More" sheet (Import, Create PDF/ZIP/TXT, Delete, Rename)** → the
`ModalBottomSheet` in `LibraryScreen`, wired to `LibraryViewModel`.
- **Capture / Import** → ML Kit scanner launched from the sheet.

## File layout

```
app/
build.gradle.kts
src/main/
AndroidManifest.xml
res/xml/file_paths.xml
java/com/example/docscanner/
MainActivity.kt
data/      Entities.kt, LibraryDao.kt, AppDatabase.kt
scan/      DocumentScannerHelper.kt
export/    Exporters.kt   (PDF / ZIP / TXT)
ui/        LibraryViewModel.kt, LibraryScreen.kt
```

## Next step (optional, the "ambitious" route)

To get vFlat's *custom* capture screen (your own toolbar, auto-capture, etc.)
you'd replace the ML Kit scanner with a CameraX viewfinder and your own edge
detection. That's the one part that typically needs a non-Google library
(OpenCV), so it's intentionally left out of this Google-only version.