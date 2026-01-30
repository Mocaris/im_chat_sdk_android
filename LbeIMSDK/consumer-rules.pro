-keep class com.lbe.imsdk.repository.model.* {
    *;
}
-keep class com.lbe.imsdk.repository.remote.models.* {
    *;
}
-keep class com.lbe.imsdk.repository.db.entry.* {
    *;
}
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}