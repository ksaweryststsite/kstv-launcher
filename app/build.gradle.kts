plugins { id("com.android.application") }
android {
 namespace = "pl.ksawery.kscreen.tv"
 compileSdk = 35
 defaultConfig {
  applicationId = "pl.ksawery.kscreen.tv"
  minSdk = 26
  targetSdk = 32
  versionCode = 2
  versionName = "0.2.0-clean"
 }
 compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
 }
 buildTypes { getByName("release") { isMinifyEnabled = false } }
}
