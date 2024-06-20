package com.university.geekwayspb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekwayspb.databinding.ActivityPlacesBinding
import java.lang.Exception

class PlacesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlacesBinding
    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterPlace: AdapterPlace

    private companion object {
        const val TAG = "PLACES_LIST_TAG"
    }

    private var categoryId = ""
    private var categoryname = ""
    private var cityId = ""
    private var cityname = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Кнопка "Назад" (закрывает PlacesActivity)
        binding.buttonCancelPlaces.setOnClickListener {
            onBackPressed()
        }

        //Поле "Поиск места"
        binding.editPlaceName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPlace.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //Загрузка и отображение мест
        loadPlaces()
    }

    //Загрузка и отображение мест
    private fun loadPlaces() {
        val intent = intent
        categoryId= intent.getStringExtra("categoryId")!!
        categoryname= intent.getStringExtra("categoryname")!!
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref?.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    placesArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Places::class.java)
                        if (model != null) {
                            placesArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.placename} ${model.categoryname} ${model.placeImage}")
                        }
                    }
                    adapterPlace = AdapterPlace(this@PlacesActivity, placesArrayList)
                    binding.placesRv.adapter = adapterPlace
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}