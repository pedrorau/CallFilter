# Project-specific R8 rules.
# kotlinx-serialization, Compose, AndroidX and multiplatform-settings ship their own consumer rules.

# libphonenumber loads its per-country metadata as Java resources by path; keep the classes that read them.
-keep class com.google.i18n.phonenumbers.** { *; }
-dontwarn com.google.i18n.phonenumbers.**
