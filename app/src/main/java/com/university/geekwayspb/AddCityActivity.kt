package com.university.geekwayspb

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.university.geekwayspb.databinding.ActivityAddCityBinding

class AddCityActivity : BaseActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var binding: ActivityAddCityBinding
    private lateinit var textAddCity: TextView
    private lateinit var etCityName: EditText
    private lateinit var etCityDescription: EditText
    private lateinit var btnAdd: Button
    private lateinit var imageBtn: ImageView
    private lateinit var btnCancelAddCity: Button
    private var imageUri: Uri? = null

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Инициализация виджетов, текстов и кнопок
        textAddCity = findViewById(R.id.textAddCity)
        etCityName = findViewById(R.id.editCityName)
        etCityDescription = findViewById(R.id.editCityDescription)
        btnAdd = findViewById(R.id.buttonAdd)
        imageBtn = findViewById(R.id.image_viewCity)
        btnCancelAddCity = findViewById(R.id.buttonCancelAddCity)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()

        //Фото места
        imageBtn.setOnClickListener {
            addImage()
        }

        //Кнопка "Отмена" (закрывает AddCityActivity)
        btnCancelAddCity.setOnClickListener {
            onBackPressed()
        }

        //Кнопка "Добавить" (добавляет город в БД и открывает DataBaseActivity)
        btnAdd.setOnClickListener {
            addCity()
        }
    }

    private fun addImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                pickImageFromGallery()
            }
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Разрешение отклонено!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data!!
            binding.imageViewCity.setImageURI(data?.data)
            Toast.makeText(this, "Фото добавлено.", Toast.LENGTH_SHORT).show()
        }
    }

    //Добавление картинки города в БД Storage
    private fun addImageCityStorage() {
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Cities/$cityname"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImagePlaceUrl = "${uriTask.result}"
                addCityFirebase(uploadedImagePlaceUrl, timestamp)
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadImagePlace: Fail")
            }
    }

    private var cityname = ""
    private var citydescription = ""
    private fun addCity() {
        cityname = this.etCityName.text.toString().trim()
        citydescription = this.etCityDescription.text.toString().trim()
        if (cityname.isEmpty() || citydescription.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show()
        } else if ( imageUri == null) {
            Toast.makeText(this, "Добавьте фотографию города!", Toast.LENGTH_SHORT).show()
        } else {
            //addCityFirebase()
            addImageCityStorage()
        }
    }

    private fun addCityFirebase(uploadedImageCityUrl: String, timestamp: Long) {
        val timestamp = System.currentTimeMillis()
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["cityname"] = cityname
        hashMap["citydescription"] = citydescription
        hashMap["cityImage"] = "$uploadedImageCityUrl"
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${auth.uid}"

        //Проверка правильности написания названия города
        if (!cityname.matches("^[a-zA-Zа-яА-Я\\- ]+\$".toRegex())) {
            Toast.makeText(this, "Проверьте правильность написания названия города!", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Город успешно добавлен.", Toast.LENGTH_SHORT).show()
                val dbActivity = Intent(this, DataBaseActivity::class.java)
                startActivity(dbActivity)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка добавления города!", Toast.LENGTH_SHORT).show()
            }
    }
}