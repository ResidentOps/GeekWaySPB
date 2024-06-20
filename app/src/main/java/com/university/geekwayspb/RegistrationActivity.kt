package com.university.geekwayspb

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var etUserName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etConfPass: EditText
    private lateinit var btnRegAcc: Button
    private lateinit var textHaveAccount: TextView

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    private lateinit var rf: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        etUserName = findViewById(R.id.editTextUserName)
        etEmail = findViewById(R.id.editTextEmail)
        etPass = findViewById(R.id.editTextPassword)
        etConfPass = findViewById(R.id.editTextPasswordConfirm)
        btnRegAcc = findViewById(R.id.buttonRegistrationAccount)
        textHaveAccount = findViewById(R.id.textHaveAccount)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db= FirebaseDatabase.getInstance()
        rf = db.getReference()

        //Кнопка "Создать профиль" (регистрирует пользователя, добавляет его данные в Authentication и Firestore, открывает EnterActivity)
        btnRegAcc.setOnClickListener {
            signUpUser()
        }

        //Кнопка "Войти" (закрывает RegistrationActivity)
        val buttonEnterAccount: Button = findViewById (R.id.buttonEnterAccount)
        buttonEnterAccount.setOnClickListener {
            onBackPressed()
        }
    }

    //Регистрация нового пользователя
    private fun signUpUser() {
        val username = etUserName.text.toString()
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val confirmPassword = etConfPass.text.toString()

        //Проверка заполнения полей
        if (username.isBlank() ||email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания имени
        if (username.length > 20) {
            Toast.makeText(this, "Имя слишком длинное!", Toast.LENGTH_SHORT).show()
            return
        }
        if (username.length < 2) {
            Toast.makeText(this, "Имя слишком короткое!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!username.matches("[A-Z,a-z,А-Я,а-я]*".toRegex())) {
            Toast.makeText(this, "Имя должно содержать только буквы! (пробелы и специальные символы не допускаются)", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Проверьте правильность написания E-mail адреса!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания пароля
        if (pass.length < 8) {
            Toast.makeText(this, "Пароль должен содержать минимум 8 символов!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[A-Z].*".toRegex())) {
            Toast.makeText(this, "Пароль должен содержать минимум 1 заглавный символ!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[a-z].*".toRegex())) {
            Toast.makeText(this, "Пароль должен содержать минимум 1 строчный символ!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[0-9].*".toRegex())) {
            Toast.makeText(this, "Пароль должен содержать минимум 1 цифру!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!pass.matches(".*[!@#\$%^&_+=].*".toRegex())) {
            Toast.makeText(this, "Пароль должен содержать минимум 1 специальный символ! (!@#\$%^&_+=)", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка совпадения паролей
        if (pass != confirmPassword) {
            Toast.makeText(this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show()
            return
        }

        //Загрузка
        showProgressDialog(resources.getString(R.string.text_progress))

        //Процесс регистрации пользователя
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {

            //Проверка на уже зарегестрированный Email
            checkEmailExists()

            if (it.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, "Регистрация успешно завершена.", Toast.LENGTH_SHORT).show()
                val userInfo = hashMapOf(
                    //Назначение прав доступа обычного пользователя
                    "isUser" to "1"
                )

                //Создание коллекции "users" в БД FireStore
                if (user != null) {
                    fs.collection("users").document(user.uid)
                        .set(userInfo)
                        .addOnSuccessListener {
                            Log.d("SuccessListener", "DocumentSnapshot added. ")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FailureListener", "Error adding document.", e)
                        }
                }

                //Добавление данных пользователя в ДБ Realtime Database
                val userData = hashMapOf(
                    "Name" to username,
                    "Email" to email,
                    //"Password" to pass
                )

                //Создание дочерней записи "users" в БД Realtime Database
                if (user != null) {
                    rf.child("users")
                        .child(user.uid)
                        .setValue(userData)
                        .addOnSuccessListener {
                            Log.d("SuccessListener", "DocumentData added: ")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FailureListener", "Error adding document data.", e)
                        }
                }

                //Открытие HomeActivity после успешной регистрации пользователя
                val homeActivity = Intent(this, HomeActivity::class.java)
                startActivity(homeActivity)
                finish()
            } else {
                //Если пользователь уже существует - ошибка незавершения регистрации
                Toast.makeText(this, "Регистрация не завершена!", Toast.LENGTH_SHORT).show()
            }

            //Завершение загрузки
            hideProgressDialog()
        }
    }

    //Проверка на уже зарегестрированный Email
    private fun checkEmailExists() {
        val email = etEmail.text.toString()
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener() {
                val checkEmail = auth.currentUser?.email?.isEmpty()
                if (checkEmail === false) {
                    Toast.makeText(this, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Введенный E-mail уже зарегестрирован!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}