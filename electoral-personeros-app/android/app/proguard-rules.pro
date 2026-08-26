# Proguard rules for electoral-personeros-app
# Keep Retrofit and Gson related classes
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep data models
-keep class com.electoral.personeros.data.models.** { *; }
