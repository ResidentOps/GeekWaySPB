package com.university.geekwayspb

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.university.geekwayspb.databinding.ActivityCityDetailsBinding

class CityDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityCityDetailsBinding
    private lateinit var cityName: TextView
    private lateinit var cityDescription: TextView
    private lateinit var cityPhoto: ImageView
    private var cityId = ""
    private var originalCityName: String = ""
    private var originalCityDescription: String = ""
    lateinit var rusEngTranslator: com.google.mlkit.nl.translate.Translator
    lateinit var engRusTranslator: com.google.mlkit.nl.translate.Translator
    private var isTranslated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        cityId = intent.getStringExtra("cityId")!!

        cityName = findViewById(R.id.textCityName)
        cityDescription = findViewById(R.id.textCityDescription)
        cityPhoto = findViewById(R.id.image_viewCityPhoto)

        //Кнопка "Перевести"
        binding.buttonTranslate.setOnClickListener {
            if (isTranslated) {
                prepareOriginalText()
            } else {
                originalCityName = cityName.text.toString()
                originalCityDescription = cityDescription.text.toString()
                prepareTranslateModel()
            }
        }

        //Кнопка "Назад к списку" (закрывает CityDetailsActivity)
        binding.buttonCancelCityDetails.setOnClickListener {
            onBackPressed()
        }

        //Загрузка и отображение информации о городе
        loadCityDetails()
    }

    private fun prepareOriginalText() {
        val options:TranslatorOptions=TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        engRusTranslator = Translation.getClient(options)

        //Загрузка
        showProgressDialog(resources.getString(R.string.text_progress))

        engRusTranslator.downloadModelIfNeeded().addOnSuccessListener {

            //Завершение загрузки
            hideProgressDialog()

            //Перевод
            originalText()
        }
            .addOnFailureListener {
                cityDescription.text = "Error ${it.message}"
                cityName.text = "Error ${it.message}"
            }
    }

    private fun originalText() {
        engRusTranslator.translate(originalCityDescription).addOnSuccessListener {
            cityDescription.text = it
        }
            .addOnFailureListener {
                cityDescription.text = "Error ${it.message}"
            }
        engRusTranslator.translate(originalCityName).addOnSuccessListener {
            cityName.text = it
        }
            .addOnFailureListener {
                cityName.text = "Error ${it.message}"
            }
        isTranslated = false
    }

    //Модуль перевода текста
    private fun prepareTranslateModel() {
        val options:TranslatorOptions=TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.RUSSIAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        rusEngTranslator = Translation.getClient(options)

        //Загрузка
        showProgressDialog(resources.getString(R.string.text_progress))

        rusEngTranslator.downloadModelIfNeeded().addOnSuccessListener {

            //Завершение загрузки
            hideProgressDialog()

            //Перевод
            translateText()
        }
            .addOnFailureListener {
                cityDescription.text = "Error ${it.message}"
                cityName.text = "Error ${it.message}"
            }
    }

    //Перевод
    private fun translateText() {
        rusEngTranslator.translate(originalCityDescription).addOnSuccessListener {
            cityDescription.text = it
        }
            .addOnFailureListener {
                cityDescription.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalCityName).addOnSuccessListener {
            cityName.text = it
        }
            .addOnFailureListener {
                cityName.text = "Error ${it.message}"
            }
        isTranslated = true
    }

    //Загрузка и отображение информации о городе
    private fun loadCityDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val cityname = "${snapshot.child("cityname").value}"
                            val citydescription = "${snapshot.child("citydescription").value}"
                            val cityImage = "${snapshot.child("cityImage").value}"

                            cityName.text = cityname
                            cityDescription.text = citydescription

                            try {
                                Glide.with(this@CityDetailsActivity)
                                    .load(cityImage)
                                    .placeholder(R.drawable.ic_image)
                                    .into(cityPhoto)
                            }
                            catch (e: Exception) {
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}