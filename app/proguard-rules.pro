# ===================================================================
# 1. TVŮJ KÓD A LOGIKA WIDGETU
# ===================================================================
-keep class com.czczypsoson.datausagewidget.** { *; }

# ===================================================================
# 2. GLANCE & APPWIDGET ARCHITEKTURA
# ===================================================================
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }

-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# ===================================================================
# 3. WORKMANAGER & ROOM & INPUT MERGERS (Klíčové pro funkčnost)
# ===================================================================
-dontwarn androidx.work.impl.**
-dontwarn androidx.room.**

# Zachování InputMergeru (OPRAVA pro chybu v logcatu)
-keep class * extends androidx.work.InputMerger {
    <init>();
}

-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    void clearAllTables();
}

-keep class androidx.work.impl.WorkDatabase** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ===================================================================
# 4. KORUTINY & DATASTORE
# ===================================================================
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**