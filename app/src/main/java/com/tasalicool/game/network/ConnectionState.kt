package com.tasalicool.game.network

/**
 * ConnectionState - حالات الاتصال
 * 
 * تمثل جميع حالات الاتصال الممكنة في اللعبة
 */
enum class ConnectionState {
    
    // ==================== DISCONNECTED ====================
    /**
     * DISCONNECTED - غير متصل
     * 
     * الحالة الأولية
     * لا توجد اتصالات نشطة
     */
    DISCONNECTED,
    
    // ==================== CONNECTING ====================
    /**
     * CONNECTING - جاري الاتصال
     * 
     * يحاول الاتصال بالسيرفر
     * ينتظر الموافقة
     */
    CONNECTING,
    
    // ==================== CONNECTED ====================
    /**
     * CONNECTED - متصل كعميل
     * 
     * متصل بالسيرفر بنجاح
     * يمكن استقبال وإرسال الرسائل
     */
    CONNECTED,
    
    // ==================== HOSTING ====================
    /**
     * HOSTING - استضافة كسيرفر
     * 
     * يعمل كسيرفر
     * ينتظر انضمام لاعبين آخرين
     */
    HOSTING,
    
    // ==================== ERROR ====================
    /**
     * ERROR - خطأ في الاتصال
     * 
     * فشل في الاتصال
     * يجب إعادة المحاولة
     */
    ERROR;
    
    // ==================== HELPER METHODS ====================
    
    /**
     * هل الاتصال نشط
     */
    fun isActive(): Boolean {
        return this == CONNECTED || this == HOSTING
    }
    
    /**
     * هل يحاول الاتصال
     */
    fun isConnecting(): Boolean {
        return this == CONNECTING
    }
    
    /**
     * هل متصل كعميل
     */
    fun isClient(): Boolean {
        return this == CONNECTED
    }
    
    /**
     * هل متصل كسيرفر
     */
    fun isServer(): Boolean {
        return this == HOSTING
    }
    
    /**
     * هل هناك خطأ
     */
    fun hasError(): Boolean {
        return this == ERROR
    }
    
    /**
     * هل متصل بشكل كامل
     */
    fun isFullyConnected(): Boolean {
        return this == CONNECTED || this == HOSTING
    }
    
    /**
     * الحصول على الوصف النصي
     */
    fun getDescription(): String {
        return when (this) {
            DISCONNECTED -> "Not Connected"
            CONNECTING -> "Connecting..."
            CONNECTED -> "Connected to Server"
            HOSTING -> "Hosting Game"
            ERROR -> "Connection Error"
        }
    }
    
    /**
     * الحصول على الرمز (Emoji)
     */
    fun getEmoji(): String {
        return when (this) {
            DISCONNECTED -> "❌"
            CONNECTING -> "⏳"
            CONNECTED -> "✅"
            HOSTING -> "🎮"
            ERROR -> "⚠️"
        }
    }
    
    /**
     * هل يمكن بدء اللعبة
     */
    fun canStartGame(): Boolean {
        return this == HOSTING || this == CONNECTED
    }
    
    /**
     * هل يمكن الاتصال
     */
    fun canConnect(): Boolean {
        return this == DISCONNECTED || this == ERROR
    }
}

/**
 * Extension للحصول على حالة الاتصال بصيغة صديقة للمستخدم
 */
fun ConnectionState.toUserFriendlyString(): String {
    return when (this) {
        ConnectionState.DISCONNECTED -> "اضغط للاتصال"
        ConnectionState.CONNECTING -> "جاري الاتصال..."
        ConnectionState.CONNECTED -> "متصل بالسيرفر"
        ConnectionState.HOSTING -> "استضيف اللعبة"
        ConnectionState.ERROR -> "خطأ في الاتصال"
    }
}

/**
 * Extension للحصول على اللون المناسب للحالة
 */
fun ConnectionState.getColorHex(): String {
    return when (this) {
        ConnectionState.DISCONNECTED -> "#FF6B6B"  // Red
        ConnectionState.CONNECTING -> "#FFC107"    // Yellow
        ConnectionState.CONNECTED -> "#4CAF50"     // Green
        ConnectionState.HOSTING -> "#2196F3"       // Blue
        ConnectionState.ERROR -> "#FF5252"         // Dark Red
    }
}

/**
 * Data class لتخزين معلومات حالة الاتصال الكاملة
 */
data class ConnectionStatus(
    val state: ConnectionState,
    val connectedPlayersCount: Int = 0,
    val totalPlayersNeeded: Int = 4,
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    
    fun isReady(): Boolean {
        return state.isFullyConnected() && connectedPlayersCount == totalPlayersNeeded
    }
    
    fun getRemainingPlayersNeeded(): Int {
        return (totalPlayersNeeded - connectedPlayersCount).coerceAtLeast(0)
    }
    
    fun getProgressPercentage(): Float {
        return (connectedPlayersCount.toFloat() / totalPlayersNeeded) * 100f
    }
    
    fun getStatusMessage(): String {
        return when {
            state == ConnectionState.ERROR && errorMessage != null -> 
                "خطأ: $errorMessage"
            state == ConnectionState.HOSTING -> 
                "استضيف اللعبة ($connectedPlayersCount/$totalPlayersNeeded)"
            state == ConnectionState.CONNECTED -> 
                "متصل بالسيرفر"
            state == ConnectionState.CONNECTING -> 
                "جاري الاتصال..."
            else -> 
                state.getDescription()
        }
    }
}
