# Reglas R8 para la build release.

# Conserva números de línea para poder leer los stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class cl.therionlabs.captura.**$$serializer { *; }
-keepclassmembers class cl.therionlabs.captura.** {
    *** Companion;
}
-keepclasseswithmembers class cl.therionlabs.captura.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# DTOs de la API y rutas de navegación (se serializan por nombre de campo).
-keep class cl.therionlabs.captura.data.remote.dto.** { *; }
-keep class cl.therionlabs.captura.ui.navigation.** { *; }

# --- Retrofit ---
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# --- OkHttp ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
