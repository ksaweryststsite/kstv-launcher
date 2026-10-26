# KScreen TV 0.2 Clean Rebuild

A new independent standalone Android TV launcher, not KSTV Launcher.
Visual inspiration: Google TV Home 2026. No KSTV views, widgets, settings or assets.

UI: local HTML/CSS/JS in Android WebView, native Java bridge discovers and launches
installed Android TV apps. A cinema/demo content row and wallpaper are included
as original artwork to avoid pretending to have Google's proprietary content feeds.

Android TV 8+ and Android System WebView required. This is a visual alpha,
not an official Google product. 

Install: adb install -r app-debug.apk
Test: adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -p pl.ksawery.kscreen.tv
Do not set as default until tested. No root or reset required.
