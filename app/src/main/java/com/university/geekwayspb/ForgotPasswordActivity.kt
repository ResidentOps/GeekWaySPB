package com.university.geekwayspb

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var textForgotPassword: TextView
    private lateinit var textEnterEmailFP1: TextView
    private lateinit var textEnterEmailFP2: TextView
    private lateinit var etEmailFP: EditText
    private lateinit var btnSubmitFP: Button

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    private lateinit var rf: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        textForgotPassword = findViewById(R.id.textForgotPassword)
        textEnterEmailFP1 = findViewById(R.id.textEnterEmailForgotPassword1)
        textEnterEmailFP2 = findViewById(R.id.textEnterEmailForgotPassword2)
        etEmailFP = findViewById(R.id.editEmailForgotPassword)
        btnSubmitFP = findViewById(R.id.buttonSubmitForgotPassword)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        rf = db.getReference()

        //Кнопка "Отправить" (отправляет ссылку на введенный Email на восстановление пароля пользователя)
        btnSubmitFP.setOnClickListener {
            submitFP()
        }

        //Кнопка "Отмена" (закрывает Fo)
        val buttonCancelFP: Button = findViewById(R.id.buttonCancelForgotPassword)
        buttonCancelFP.setOnClickListener {
            onBackPressed()
        }
    }

    //Восстановление пароля пользователя
    private fun submitFP() {
        val email = etEmailFP.text.toString()

        //Проверка заполнения поля
        if (email.isBlank()) {
            Toast.makeText(this, "Поле должно быть заполнено!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Проверьте правильность написания E-mail адреса!", Toast.LENGTH_SHORT).show()
            return
        }

        //Вызов загрузги
        showProgressDialog(resources.getString(R.string.text_progress))

        //Процесс запроса для отправки ссылки
        auth.sendPasswordResetEmail(email).addOnCompleteListener(this) {

            //Проверка на зарегестрированного пользователя
            checkUserExistsFP()

            if (it.isSuccessful) {
                auth.signOut()
                Toast.makeText(this,"Откройте полученное письмо в введеном E-mail.", Toast.LENGTH_SHORT).show()
                val enterActivity = Intent(this, EnterActivity::class.java)
                startActivity(enterActivity)
                finish()
            } else {
                Toast.makeText(this, "Введенный E-mail не зарегестрирован или был удален!", Toast.LENGTH_SHORT).show()
            }

            //Завершение загрузки
            hideProgressDialog()
        }
    }

    //Проверка на зарегестрированного пользователя
    private fun checkUserExistsFP() {
        val user = auth.currentUser
        if (user === null) {
            Toast.makeText(this, "Ошибка отправки ссылки для восстановления пароля!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ссылка для восстановления пароля успешно отправлена.", Toast.LENGTH_SHORT).show()
        }
    }
}