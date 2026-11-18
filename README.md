# YouTube AutoSkip — Samsung Galaxy A52s

Android Accessibility helper prototype for Samsung Galaxy A52s / Android 14 / One UI 6.1. It targets the official YouTube app and Chrome. No root access, network permission, analytics, or screen recording is used.

## What it does

- Looks for a visible, labelled YouTube skip-ad control and activates it.
- Supports common English and Latvian labels.
- Can close an ad overlay only when both an ad marker and an explicit ad-close label are exposed in the accessibility tree.
- Runs locally as an Android Accessibility Service.

## Important limitations

- This is a prototype, not a guaranteed ad blocker. It does not skip non-skippable ads and does not remove ads; it only clicks a visible skip control.
- Chrome support depends on the website/player exposing the skip control in Chrome's accessibility tree. Some embedded/video ad players do not expose it.
- YouTube and Chrome change their interface and labels. Other interface languages may need additional labels.
- The close-ad option is deliberately conservative. A generic X / Close button is not clicked because it could close an unrelated dialog or interface element.
- Android/One UI may stop background activity according to device settings. Accessibility services are managed by Android; battery settings cannot guarantee uninterrupted operation.

## Build APK

1. Open this project folder in Android Studio (Android Studio Ladybug or newer recommended).
2. Allow Gradle sync. Install Android SDK Platform 35 if Android Studio asks.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

The project uses Android Gradle Plugin 8.7.3 and Kotlin 2.0.21. Java 17 is recommended for Gradle builds.

## Install and enable on Samsung One UI 6.1

1. Transfer `app-debug.apk` to the phone and install it. If prompted, allow installation from that source.
2. Open **YouTube AutoSkip** and enable the desired switches.
3. Tap **Atvērt pieejamības iestatījumus**.
4. In **Settings > Accessibility > Installed apps** (wording may vary), open **YouTube AutoSkip** and enable its accessibility service.
5. If Samsung later stops the service, check **Settings > Apps > YouTube AutoSkip > Battery** and select **Unrestricted**, if that option is available. You can also add it to **Never auto sleeping apps** under **Battery and device care > Battery > Background usage limits**. These settings may improve persistence but do not guarantee it.

## Privacy

The service reads the accessibility tree only while YouTube or Chrome is the active app. It does not request Internet access, record the screen, or transmit accessibility content.

## Build in GitHub Actions (without installing Android Studio locally)

This repository includes `.github/workflows/android-apk.yml`. To use GitHub's hosted Android build machine:

1. Create a new repository on GitHub and upload the contents of this ZIP to it.
2. Open the repository's **Actions** tab and enable Actions if prompted.
3. Choose **Build Android APK** and run it with **Run workflow**, or push to `main`/`master`.
4. When the workflow finishes, open its run and download the `YouTubeAutoSkip-debug-apk` artifact.
5. Extract the artifact; it contains `app-debug.apk` for installation on the phone.

The APK produced this way is a debug build and is not Play Store-signed. Android may ask you to allow installation from the browser or file manager used to open it.
