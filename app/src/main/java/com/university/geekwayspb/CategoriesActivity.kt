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
import com.university.geekwayspb.databinding.ActivityCategoriesBinding
import java.lang.Exception

class CategoriesActivity : AppCompatActivity() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var categoriesArrayList: ArrayList<Categories>
    private lateinit var adapterCategory: AdapterCategory
    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Кнопка "Добавить категорию" (открывает AddCategoryActivity)
        binding.buttonAddCategory.setOnClickListener {
            val addcategoryActivity = Intent(this, AddCategoryActivity::class.java)
            startActivity(addcategoryActivity)
        }

        //Кнопка "Добавить место" (открывает AddPlaceActivity)
        binding.buttonAddPlace.setOnClickListener {
            val addplaceActivity = Intent(this, AddPlaceActivity::class.java)
            startActivity(addplaceActivity)
        }

        //Кнопка "Назад" (закрывает CategoriesActivity)
        binding.buttonCancelCategories.setOnClickListener {
            onBackPressed()
        }

        //Поле "Поиск категории"
        binding.editCategoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        loadCategories()
    }

    private var cityname = ""
    private fun loadCategories() {
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        //ref.orderByChild("categoryname:").equalTo(cityname)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Categories::class.java)
                    categoriesArrayList.add(model!!)
                }
                adapterCategory = AdapterCategory(this@CategoriesActivity, categoriesArrayList)
                binding.categoriesRv.adapter = adapterCategory
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}