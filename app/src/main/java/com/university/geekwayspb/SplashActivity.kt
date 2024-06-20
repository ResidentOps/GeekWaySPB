package com.university.geekwayspb

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler

class SplashActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация сплэш-скрина
        handler = Handler()

        //Настройка сплэш-скрина
        handler.postDelayed( {
            //Проверка на уже авторизованного пользователя
            checkUserLogin()
            //Время сплэш-скрина - 1,5 секунды
        }, 1500)
    }
}