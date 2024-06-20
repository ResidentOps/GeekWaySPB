package com.university.geekwayspb

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EnterActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnEnter: Button
    private lateinit var textHaveNotAccount: TextView
    private lateinit var textFP: TextView
    private lateinit var textReadme: TextView

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        btnEnter = findViewById(R.id.buttonEnter)
        etEmail = findViewById(R.id.editTextEmailEnter)
        etPass = findViewById(R.id.editTextPasswordEnter)
        textHaveNotAccount = findViewById(R.id.textHaveNotAccount)
        textReadme = findViewById(R.id.textReadme)
        textFP = findViewById(R.id.textForgotPassword)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()

        //Кнопка "Войти" (авторизирует пользователя и открывает HomeActivity/AdminActivity)
        btnEnter.setOnClickListener {
            login()
        }

        //Текст "О приложении" (открывает ReadmeActivity)
        textReadme.setOnClickListener {
            val readmeActivity = Intent(this, ReadmeActivity::class.java)
            startActivity(readmeActivity)
        }

        //Текст "Забыли пароль?" (открывает ForgotPasswordActivity)
        textFP.setOnClickListener {
            val forgotPasswordActivity = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordActivity)
        }

        //Кнопка "Регистрация" (открывает RegistrationActivity)
        val buttonRegistration: Button = findViewById (R.id.buttonRegistration)
        buttonRegistration.setOnClickListener {
            val registrationActivity = Intent(this, RegistrationActivity::class.java)
            startActivity(registrationActivity)
        }
    }

    //Авторизация пользователя
    private fun login() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        Log.d("TAG", "onClick" + etEmail.text.toString())

        //Проверка заполнения полей
        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Проверьте правильность написания E-mail адреса!", Toast.LENGTH_SHORT).show()
            return
        }

        //Загрузка
        showProgressDialog(resources.getString(R.string.text_progress))

        //Процесс авторизации пользователя
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {

            //Проверка на зарегестрированного пользователя
            checkUserExists()

            if (it.isSuccessful) {
                //Проверка прав доступа пользователя
                checkUserAccessLevel(auth.uid)
            } else {
                Toast.makeText(this, "E-mail или пароль введены не верно!", Toast.LENGTH_SHORT).show()
            }

            //Завершение загрузки
            hideProgressDialog()
        }
    }

    //Проверка прав доступа пользователя
    private fun checkUserAccessLevel(uid: String?) {
        auth.currentUser?.let {
            fs.collection("users") .document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("TAG", "DocumentSnapshot data: ${document.data}")

                        if (document.getString("isAdmin") != null) {
                            val adminActivity = Intent(this, AdminActivity::class.java)
                            startActivity(adminActivity)
                            finish()
                        }
                        if (document.getString("isUser") != null) {
                            val homeActivity = Intent(this, HomeActivity::class.java)
                            startActivity(homeActivity)
                            finish()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Error getting documents.", exception)
                }
        }
    }

    //Проверка на зарегестрированного пользователя
    private fun checkUserExists() {
        val user = auth.currentUser
            if (user === null) {
                Toast.makeText(this, "Авторизация не выполнена!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "С возвращением!", Toast.LENGTH_SHORT).show()
            }
    }
}