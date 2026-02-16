package com.tarneeb.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

/**
 * MyApp - تطبيق Tarneeb
 * 
 * هذا الملف مطلوب لتهيئة MultiDex
 * يسمح بتحميل جميع dex files (classes.dex, classes2-5.dex)
 * 
 * بدونه: فقط classes.dex يُحمّل → ClassNotFoundException
 */
class MyApp : Application() {
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        
        // ✅ تهيئة MultiDex
        // هذا يضمن تحميل جميع الـ dex files قبل بدء أي شيء آخر
        MultiDex.install(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // يمكنك إضافة initialization code هنا إذا احتاج
        // مثلاً: Timber.plant(DebugTree())
    }
}
