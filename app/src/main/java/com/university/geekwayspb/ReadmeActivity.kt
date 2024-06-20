package com.university.geekwayspb

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.Toolbar

class ReadmeActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var myToolbar: Toolbar
    private lateinit var btnReadmeOk: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_readme)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        btnReadmeOk = findViewById(R.id.buttonReadmeOk)

        //Кнопка "ОК" (закрывает ReadmeActivity )
        btnReadmeOk.setOnClickListener {
            onBackPressed()
        }
    }
}