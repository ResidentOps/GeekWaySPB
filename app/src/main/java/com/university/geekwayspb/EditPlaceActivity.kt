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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.university.geekwayspb.databinding.ActivityEditPlaceBinding

class EditPlaceActivity : BaseActivity() {

    private lateinit var binding: ActivityEditPlaceBinding
    private var placeId = ""
    private var imageUri: Uri? = null
    private lateinit var categoryTitleArrayList: ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>
    private lateinit var cityTitleArrayList: ArrayList<String>
    private lateinit var cityIdArrayList: ArrayList<String>

    private companion object {
       private const val TAG = "PLACE_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Прверка подключения к интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        placeId = intent.getStringExtra("placeId")!!

        binding.buttonCancelEditPlace.setOnClickListener {
            onBackPressed()
        }

        //Список категорий
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //Список городов
        binding.cityTv.setOnClickListener {
            cityDialog()
        }

        //Фото места
        binding.imageView.setOnClickListener {
            addImage()
        }

        //Кнопка "Сохранить"
        binding.buttonSavePlace.setOnClickListener {
            saveData()
        }

        loadCategories()
        loadCities()
        loadPlaceInfo()
    }

    private fun loadPlaceInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    selectedCityId = snapshot.child("cityId").value.toString()
                    val placedescription = snapshot.child("placedescription").value.toString()
                    val placename = snapshot.child("placename").value.toString()
                    val placepublic = snapshot.child("placePublic").value.toString()
                    val placeweb = snapshot.child("placeWeb").value.toString()
                    val placeaddress = snapshot.child("placeAddress").value.toString()
                    val placetime = snapshot.child("placeTime").value.toString()
                    val placeage = snapshot.child("placeAge").value.toString()
                    val placetelephone = snapshot.child("placeTelephone").value.toString()
                    val placeLat = snapshot.child("placeLat").value.toString()
                    val placeLng = snapshot.child("placeLng").value.toString()
                    val placeImage = "${snapshot.child("placeImage").value}"

                    binding.editPlaceName.setText(placename)
                    binding.editPlaceDescription.setText(placedescription)
                    binding.editPlacePublic.setText(placepublic)
                    binding.editPlaceWeb.setText(placeweb)
                    binding.editPlaceAddress.setText(placeaddress)
                    binding.editPlaceTime.setText(placetime)
                    binding.editPlaceTelephone.setText(placetelephone)
                    binding.editPlaceAge.setText(placeage)
                    binding.editPlaceLat.setText(placeLat)
                    binding.editPlaceLng.setText(placeLng)

                    try {
                        Glide.with(this@EditPlaceActivity)
                            .load(placeImage)
                            .placeholder(R.drawable.ic_image)
                            .into(binding.imageView)
                    }
                    catch (e: Exception) {
                    }

                    val refPlaceCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refPlaceCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val categoryname = snapshot.child("categoryname").value
                                binding.categoryTv.text = categoryname.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    val refPlaceCity = FirebaseDatabase.getInstance().getReference("Cities")
                    refPlaceCity.child(selectedCityId)
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

    private val IMAGE_PICK_CODE = 1000
    private val PERMISSION_CODE = 1001
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
        startActivityForResult(intent, AddPlaceActivity.IMAGE_PICK_CODE)
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
            binding.imageView.setImageURI(data?.data)
            Toast.makeText(this, "Фото добавлено.", Toast.LENGTH_SHORT).show()
        }
    }

    private var placename = ""
    private var placedescription = ""
    private var placeweb = ""
    private var placepublic = ""
    private var placeaddress = ""
    private var placetime = ""
    private var placetelephone = ""
    private var placeage = ""
    private var placeLat = ""
    private var placeLng = ""
    private fun saveData() {
        placename = binding.editPlaceName.text.toString().trim()
        placedescription = binding.editPlaceDescription.text.toString().trim()

        selectedCityTitle = binding.cityTv.text.toString().trim()
        selectedCategoryTitle = binding.categoryTv.text.toString().trim()

        placepublic = binding.editPlacePublic.text.toString().trim()
        placeweb = binding.editPlaceWeb.text.toString().trim()
        placeaddress = binding.editPlaceAddress.text.toString().trim()
        placetime = binding.editPlaceTime.text.toString().trim()
        placetelephone = binding.editPlaceTelephone.text.toString().trim()
        placeage = binding.editPlaceAge.text.toString().trim()
        placeLat = binding.editPlaceLat.text.toString().trim()
        placeLng = binding.editPlaceLng.text.toString().trim()

        //Проверка добавления фото и заполнения полей
        if (placename.isEmpty()) {
            Toast.makeText(this, "Добавьте имя места!", Toast.LENGTH_SHORT).show()
        } else if (placedescription.isEmpty()) {
            Toast.makeText(this, "Добавьте описание места!", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Укажите категорию места!", Toast.LENGTH_SHORT).show()
        } else if (selectedCityId.isEmpty()) {
            Toast.makeText(this, "Укажите город места!", Toast.LENGTH_SHORT).show()
        } else if (placeage.isEmpty()) {
            Toast.makeText(this, "Укажите возрастное ограничение!", Toast.LENGTH_SHORT).show()
        } else {
            //updateImagePlaceStorage()
            if (imageUri == null) {
                updatePlace("")
            } else {
                updateImagePlaceStorage()
            }
        }
    }

    private fun updatePlace(uploadedImagePlaceUrl: String) {
        Log.d(TAG, "validateData: validating")
        val hashMap = hashMapOf<String, Any>()
        hashMap["placename"] = "$placename"
        hashMap["placedescription"] = "$placedescription"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["categoryname"] = "$selectedCategoryTitle"
        hashMap["cityId"] = "$selectedCityId"
        hashMap["cityname"] = "$selectedCityTitle"
        if (imageUri != null) {
            hashMap["placeImage"] = uploadedImagePlaceUrl
        }
        hashMap["placePublic"] = "$placepublic"
        hashMap["placeWeb"] = "$placeweb"
        hashMap["placeAddress"] = "$placeaddress"
        hashMap["placeTime"] = "$placetime"
        hashMap["placeTelephone"] = "$placetelephone"
        hashMap["placeAge"] = "$placeage"
        hashMap["placeLat"] = "$placeLat"
        hashMap["placeLng"] = "$placeLng"
        //hashMap["placeUrl"] = "$uploadedImagePlaceUrl"

        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                //imageUri = null
                Toast.makeText(this, "Место успешно изменено.", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка изменения места!", Toast.LENGTH_SHORT).show()
            }
    }

    //Добавление картинки места в БД Storage
    private fun updateImagePlaceStorage() {
        val filePathAndName = "Places/$placename"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImagePlaceUrl = "${uriTask.result}"
                updatePlace(uploadedImagePlaceUrl)
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadImagePlace: Fail")
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryDialog() {
        val categoriesArrayList = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArrayList[i] = categoryTitleArrayList[i]
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выбирите категорию")
            .setItems(categoriesArrayList) { dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
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

    private fun loadCategories() {
        Log.d(TAG, "Load categories: loading categories")
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()
                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val categoryname = "${ds.child("categoryname").value}"
                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(categoryname)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadCities() {
        Log.d(TAG, "Load cities: loading cities")
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