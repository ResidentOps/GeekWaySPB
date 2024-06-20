package com.university.geekwayspb

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekwayspb.databinding.ActivityEditCategoryBinding

class EditCategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityEditCategoryBinding
    private var categoryId = ""
    private lateinit var cityTitleArrayList: ArrayList<String>
    private lateinit var cityIdArrayList: ArrayList<String>

    private companion object {
        private const val TAG = "CATEGORY_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        categoryId = intent.getStringExtra("categoryId")!!

        binding.buttonCancelEditCategory.setOnClickListener {
            onBackPressed()
        }

        //Список городов
        binding.cityTv.setOnClickListener {
            cityDialog()
        }

        //Кнопка "Сохранить"
        binding.buttonSaveCategory.setOnClickListener {
            saveData()
        }

        loadCities()
        loadCategoryInfo()
    }

    private fun loadCategoryInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCityId = snapshot.child("cityId").value.toString()
                    val categoryname = snapshot.child("categoryname").value.toString()

                    binding.editCategoryName.setText(categoryname)

                    val refCategoryCity = FirebaseDatabase.getInstance().getReference("Cities")
                    refCategoryCity.child(selectedCityId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val cityname = snapshot.child("cityname").value
                                binding.cityTv.text = cityname.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private var categoryname = ""
    private fun saveData() {
        categoryname = binding.editCategoryName.text.toString().trim()

        selectedCityTitle = binding.cityTv.text.toString().trim()

        //Проверка заполнения полей
        if (categoryname.isEmpty()) {
            Toast.makeText(this, "Добавьте название категории!", Toast.LENGTH_SHORT).show()
        } else if (selectedCityId.isEmpty()) {
            Toast.makeText(this, "Укажите город категории!", Toast.LENGTH_SHORT).show()
        } else {
                updateCategory()
            }
        }

    private fun updateCategory() {
        Log.d(EditCategoryActivity.TAG, "validateData: validating")
        val hashMap = hashMapOf<String, Any>()
        hashMap["categoryname"] = "$categoryname"
        hashMap["cityId"] = "$selectedCityId"
        hashMap["cityname"] = "$selectedCityTitle"
        //hashMap["placeUrl"] = "$uploadedImagePlaceUrl"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                //imageUri = null
                Toast.makeText(this, "Категория успешно изменена.", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка изменения категории!", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCityId = ""
    private var selectedCityTitle = ""
    private fun cityDialog() {
        val citiesArrayList = arrayOfNulls<String>(cityTitleArrayList.size)
        for (i in cityTitleArrayList.indices) {
            citiesArrayList[i] = cityTitleArrayList[i]
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выбирите город")
            .setItems(citiesArrayList) { dialog, position ->
                selectedCityId = cityIdArrayList[position]
                selectedCityTitle = cityTitleArrayList[position]
                binding.cityTv.text = selectedCityTitle
            }
            .show()
    }

    private fun loadCities() {
        Log.d(EditCategoryActivity.TAG, "Load cities: loading cities")
        cityTitleArrayList = ArrayList()
        cityIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cityTitleArrayList.clear()
                cityIdArrayList.clear()
                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val cityname = "${ds.child("cityname").value}"
                    cityIdArrayList.add(id)
                    cityTitleArrayList.add(cityname)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}