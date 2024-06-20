package com.university.geekwayspb

import android.app.Dialog
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var mProgressDialog: Dialog

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore

    //Проверка подключения к интернету
    fun checkNetworkConnection() {
    val networkConnection = ConnectionLive(applicationContext)
        val dialogDisconnect = Dialog(this)
        dialogDisconnect.setCancelable(false)
        dialogDisconnect.setContentView(R.layout.dialog_disconnect)
        networkConnection.observe(this, Observer { isConnected ->
            if (isConnected) {
                dialogDisconnect.dismiss()
            } else {
                dialogDisconnect.show()
            }
        })
    }

    //Экрана загрузки (Dialog Progress)
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
    }

    //Исчезновение экрана загрузки (Dialog Progress Dismiss)
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    //Проверка на уже авторизованного пользователя
    fun checkUserLogin() {
        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        //Если пользователь уже авторизован ->
        if (user != null) {
            //Проверка прав доступа пользователя
            checkUserAccessLevel(auth.uid)
        } else {
            //Иначе открываем экран авторизации (EnterActivity)
            val enterActivity = Intent(this, EnterActivity::class.java)
            startActivity(enterActivity)
            finish()
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
}