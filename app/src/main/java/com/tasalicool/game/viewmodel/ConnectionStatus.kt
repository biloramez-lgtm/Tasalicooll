package com.tasalicool.game.viewmodel

enum class ConnectionStatus {
    IDLE,                // Multiplayer غير مفعّل
    CREATING_ROOM,       // Host ينشئ غرفة
    WAITING_FOR_PLAYERS, // Host ينتظر
    CONNECTING,          // Client يحاول الاتصال
    CONNECTED,           // تم الاتصال
    SYNCING,             // مزامنة اللعبة
    ERROR                // خطأ اتصال
}
