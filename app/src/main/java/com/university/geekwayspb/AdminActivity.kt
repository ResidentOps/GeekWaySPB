package com.university.geekwayspb

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var adminName: TextView
    private lateinit var adminShowName: TextView
    private lateinit var adminEmail: TextView
    private lateinit var adminShowEmail: TextView
    private lateinit var btnAdminDataBase: Button
    private lateinit var btnAdminDBPlaces: Button
    private lateinit var btnAdminSettings: Button
    private lateinit var btnExitAdmin: Button

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        //Проверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        adminName = findViewById(R.id.textNameAdmin)
        adminShowName = findViewById(R.id.admin_name)
        adminEmail = findViewById(R.id.textEmailAdmin)
        adminShowEmail = findViewById(R.id.admin_email)
        btnAdminDataBase = findViewById(R.id.buttonAdminDataBase)
        btnAdminDBPlaces = findViewById(R.id.buttonAdminBDPlaces)
        btnAdminSettings = findViewById(R.id.buttonAdminSettings)
        btnExitAdmin = findViewById(R.id.buttonAdminExit)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("users").child(fbUser!!.uid)

        //Кнопка "Список городов" (открывает DataBaseActivity)
        btnAdminDataBase.setOnClickListener {
            val dbActivity = Intent(this, DataBaseActivity::class.java)
            startActivity(dbActivity)
        }

        //Кнопка "Список мест" (открывает CategoriesActivity)
        btnAdminDBPlaces.setOnClickListener {
            val categoriesActivity = Intent(this, CategoriesActivity::class.java)
            startActivity(categoriesActivity)
        }

        //Кнопка "Настройки" (открывает SettingsActivity)
        btnAdminSettings.setOnClickListener {
            val epActivity = Intent(this, SettingsActivity::class.java)
            startActivity(epActivity)
        }

        //Кнопка "Выйти" (выходит из авторизованного профиля и открывает EnterActivity)
        btnExitAdmin.setOnClickListener {
            auth.signOut()
            val enterActivity = Intent(this, EnterActivity::class.java)
            startActivity(enterActivity)
            finish()
        }

        //Отображение данных пользователя
        showData()
    }

    //Отображение данных пользователя
    private fun showData() {
        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: Users? = p0.getValue(Users::class.java)
                    adminShowName.text = user!!.getName()
                    adminShowEmail.text = user!!.getEmail()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}