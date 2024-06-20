package com.university.geekwayspb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekwayspb.databinding.ActivityDataBaseBinding
import java.lang.Exception

class DataBaseActivity : AppCompatActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var citiesArrayList: ArrayList<Cities>
    private lateinit var adapterCity: AdapterCity
    private lateinit var binding: ActivityDataBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Кнопка "Добавить город" (открывает AddCityActivity)
        binding.buttonAddCity.setOnClickListener {
            val addcityActivity = Intent(this, AddCityActivity::class.java)
            startActivity(addcityActivity)
            finish()
        }

        //Кнопка "Назад" (закрывает DataBaseActivity)
        binding.buttonCancelDataBase.setOnClickListener {
            onBackPressed()
        }

        //Поле "Поиск города"
        binding.editCityName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCity.filter.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //Загрузка и отображение городов
        loadCities()
    }

    //Загрузка и отображение городов
    private fun loadCities() {
        citiesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                citiesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Cities::class.java)
                    citiesArrayList.add(model!!)
                }
                adapterCity = AdapterCity(this@DataBaseActivity, citiesArrayList)
                binding.citiesRv.adapter = adapterCity
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}