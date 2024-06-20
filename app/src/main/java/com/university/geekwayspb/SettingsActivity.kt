package com.university.geekwayspb

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SettingsActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var textSettings: TextView
    private lateinit var etUserName: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etPassConfSettings: EditText
    private lateinit var textResetPass: TextView
    private lateinit var btnSaveSettings: Button
    private lateinit var btnCancelSettings: Button

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    private val userCollectionRef = Firebase.firestore.collection("users")
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        textSettings = findViewById(R.id.textSettings)
        etUserName = findViewById(R.id.editSettingsUserName)
        etUserEmail = findViewById(R.id.editSettingsEmail)
        etPassConfSettings = findViewById(R.id.editSettingsPassConf)
        textResetPass = findViewById(R.id.textResetPasswordSettings)
        btnSaveSettings = findViewById(R.id.buttonSaveSettings)
        btnCancelSettings = findViewById(R.id.buttonCancelSettings)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("users").child(fbUser!!.uid)

        //Кнопка "Сохранить" (сохраняет данные пользователя и открывает AdminActivity)
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        //Текст "Изменить пароль" (открывает ResetPasswordActivity)
        textResetPass.setOnClickListener {
            val resetPasswordActivity = Intent(this, ResetPasswordActivity::class.java)
            startActivity(resetPasswordActivity)
            finish()
        }

        //Кнопка "Отмена" (закрывает SettingsActivity)
        btnCancelSettings.setOnClickListener {
            onBackPressed()
        }

        //Отображение данных пользователя
        showUserDataET()
    }

    //Отображение данных пользователя
    private fun showUserDataET() {
        val user = auth.currentUser
        val ref = db.getReference("users")
        ref.child(auth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("Name").value}"
                    etUserName.setText(name)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        if (user != null) {
            etUserEmail.setText(user.email)
        }
    }

    //Сохранение данных пользователя
    private fun saveSettings() {
        val passConf = etPassConfSettings.text.toString()
        val userName = etUserName.text.toString()
        val userEmail = etUserEmail.text.toString()

        //Проверка правильности написания имени
        if (userName.length > 20) {
            Toast.makeText(this, "Имя слишком длинное!", Toast.LENGTH_SHORT).show()
            return
        }
        if (userName.length < 2) {
            Toast.makeText(this, "Имя слишком короткое!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!userName.matches("[A-Z,a-z,А-Я,а-я]*".toRegex())) {
            Toast.makeText(this, "Имя должно содержать только буквы! (пробелы и специальные символы не допускаются)", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка правильности написания Email
        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Проверьте правильность написания E-mail адреса!", Toast.LENGTH_SHORT).show()
            return
        }

        //Проверка заполнения поля с паролем для потверждения
        if (passConf.isBlank()) {
            Toast.makeText(this, "Введите ваш пароль для сохранения!", Toast.LENGTH_SHORT).show()
            return
        }

        //Сохранение данных пользователя
        saveUserData()
    }

    //Сохранение данных пользователя
    private fun saveUserData() {
        val passConf = etPassConfSettings.text.toString()
        val userName = etUserName.text.toString()
        val userEmail = etUserEmail.text.toString()
        val user = auth.currentUser
        if (user != null && user.email != null) {

            //Загрузка
            showProgressDialog(resources.getString(R.string.text_progress))

            val userCredential = EmailAuthProvider
                .getCredential(user.email!!, passConf)
            user?.reauthenticate(userCredential)
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        user?.updateEmail(userEmail)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val hashMap: HashMap<String, Any> = HashMap()
                                    hashMap["Name"] = "$userName"
                                    val refNa = db.getReference("users")
                                    refNa.child(auth.uid!!)
                                        .updateChildren(hashMap)
                                        .addOnCompleteListener {
                                        }
                                    refUsers?.child("Email")?.setValue(userEmail)
                                    Toast.makeText(this, "Настройки успешно сохранены.", Toast.LENGTH_SHORT).show()
                                    val adminActivity = Intent(this, AdminActivity::class.java)
                                    startActivity(adminActivity)
                                    finish()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Введен неверный пароль!", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //Завершение загрузки
        hideProgressDialog()
    }
}