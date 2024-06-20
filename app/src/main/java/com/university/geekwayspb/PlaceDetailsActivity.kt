package com.university.geekwayspb

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.university.geekwayspb.databinding.ActivityPlaceDetailsBinding
import com.university.geekwayspb.databinding.DialogCommentAddBinding

class PlaceDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityPlaceDetailsBinding
    private var placeId = ""
    private lateinit var firebaseAuth: FirebaseAuth
    private var isInMyFavorite = false
    private lateinit var commentArrayList: ArrayList<Comments>
    private lateinit var adapterComment: AdapterComment
    private var originalPlaceDescriptionDB: String = ""
    private var originalPlaceCategotyDB: String = ""
    private var originalPlaceCityDB: String = ""
    private var originalPlaceTimeDB:String = ""
    private var originalPlaceAddressDB:String = ""
    lateinit var rusEngTranslator: com.google.mlkit.nl.translate.Translator
    lateinit var engRusTranslator: com.google.mlkit.nl.translate.Translator
    private var isTranslated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        placeId = intent.getStringExtra("placeId")!!

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
            }

        //Кнопка "Перевести описание"
        binding.buttonTranslate.setOnClickListener {
            if (isTranslated) {
                prepareOriginalText()
            } else {
                originalPlaceDescriptionDB = binding.textPlaceDescription.text.toString()
                originalPlaceCategotyDB = binding.textPlaceCategoryDB.text.toString()
                originalPlaceCityDB = binding.textPlaceCityDB.text.toString()
                originalPlaceTimeDB = binding.textPlaceTimeBD.text.toString()
                originalPlaceAddressDB = binding.textPlaceAddressBD.text.toString()
                prepareTranslateModel()
            }
        }

        //Кнопка "Назад к списку" (закрывает PlaceDetailsActivity)
        binding.buttonCancelPlaceDetails.setOnClickListener {
            onBackPressed()
        }

        //Кнопка-картинка "Добавить комментарий" (открывает CommentDialog)
        binding.btnAddComment.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Вы не авторизированны!", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }

        //Кнопка "Добавить/Убрать из избранного" (добавляет/убирает место из списка избранных мест пользователя)
        binding.buttonFavorite.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Вы не авторизированны!", Toast.LENGTH_SHORT).show()
            } else {
                if (isInMyFavorite) {
                    removeFromFavorite(this, placeId)
                } else {
                    addToFavorite()
                }
            }
        }

        //Загрузка и отображение информации о месте
        loadPlaceDetails()
        //Загрузка и отображение комментариев
        showComments()
    }

    private fun prepareOriginalText() {
        val options: TranslatorOptions = TranslatorOptions.Builder()
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
                binding.textPlaceDescription.text = "Error ${it.message}"
                binding.textPlaceCategoryDB.text = "Error ${it.message}"
                binding.textPlaceCityDB.text = "Error ${it.message}"
                binding.textPlaceTimeBD.text = "Error ${it.message}"
                binding.textPlaceAddressBD.text = "Error ${it.message}"
            }
    }

    private fun originalText() {
        engRusTranslator.translate(originalPlaceDescriptionDB).addOnSuccessListener {
            binding.textPlaceDescription.text = it
        }
            .addOnFailureListener {
                binding.textPlaceDescription.text = "Error ${it.message}"
            }
        engRusTranslator.translate(originalPlaceCategotyDB).addOnSuccessListener {
            binding.textPlaceCategoryDB.text = it
        }
            .addOnFailureListener {
                binding.textPlaceCategoryDB.text = "Error ${it.message}"
            }
        engRusTranslator.translate(originalPlaceCityDB).addOnSuccessListener {
            binding.textPlaceCityDB.text = it
        }
            .addOnFailureListener {
                binding.textPlaceCityDB.text = "Error ${it.message}"
            }
        engRusTranslator.translate(originalPlaceTimeDB).addOnSuccessListener {
            binding.textPlaceTimeBD.text = it
        }
            .addOnFailureListener {
                binding.textPlaceTimeBD.text = "Error ${it.message}"
            }
        engRusTranslator.translate(originalPlaceAddressDB).addOnSuccessListener {
            binding.textPlaceAddressBD.text = it
        }
            .addOnFailureListener {
                binding.textPlaceAddressBD.text = "Error ${it.message}"
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
                binding.textPlaceDescription.text = "Error ${it.message}"
                binding.textPlaceCategoryDB.text = "Error ${it.message}"
                binding.textPlaceCityDB.text = "Error ${it.message}"
                binding.textPlaceTimeBD.text = "Error ${it.message}"
                binding.textPlaceAddressBD.text = "Error ${it.message}"
            }
    }

    //Перевод
    private fun translateText() {
        rusEngTranslator.translate(originalPlaceDescriptionDB).addOnSuccessListener {
            binding.textPlaceDescription.text = it
        }
            .addOnFailureListener {
                binding.textPlaceDescription.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceCategotyDB).addOnSuccessListener {
            binding.textPlaceCategoryDB.text = it
        }
            .addOnFailureListener {
                binding.textPlaceCategoryDB.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceCityDB).addOnSuccessListener {
            binding.textPlaceCityDB.text = it
        }
            .addOnFailureListener {
                binding.textPlaceCityDB.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceTimeDB).addOnSuccessListener {
            binding.textPlaceTimeBD.text = it
        }
            .addOnFailureListener {
                binding.textPlaceTimeBD.text = "Error ${it.message}"
            }
        rusEngTranslator.translate(originalPlaceAddressDB).addOnSuccessListener {
            binding.textPlaceAddressBD.text = it
        }
            .addOnFailureListener {
                binding.textPlaceAddressBD.text = "Error ${it.message}"
            }
        isTranslated = true
    }

    //Загрузка и отображение комментариев
    private fun showComments() {
        commentArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Comments::class.java)
                            commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@PlaceDetailsActivity, commentArrayList)
                    binding.commentsRv.adapter = adapterComment
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private var comment = ""
    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.btnBack.setOnClickListener {
            alertDialog.dismiss()
        }
        commentAddBinding.buttonAddComment.setOnClickListener {
            comment = commentAddBinding.editComment.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(this, "Введите комментарий!!", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        val timestamp = "${System.currentTimeMillis()}"
        val hashMap = hashMapOf<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["placeId"] = "$placeId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Комментарий добавлен.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка добавления комментария!!", Toast.LENGTH_SHORT).show()

            }
    }

    //Загрузка и отображение информации о месте
    private fun loadPlaceDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val cityId = "${snapshot.child("cityId").value}"
                    val placename = "${snapshot.child("placename").value}"
                    val placedescription = "${snapshot.child("placedescription").value}"
                    val placeage = "${snapshot.child("placeAge").value}"
                    val placetime = "${snapshot.child("placeTime").value}"
                    val placeaddress = "${snapshot.child("placeAddress").value}"
                    val placepublic = "${snapshot.child("placePublic").value}"
                    val placeweb = "${snapshot.child("placeWeb").value}"
                    val placetelephone = "${snapshot.child("placeTelephone").value}"
                    val placeImage = "${snapshot.child("placeImage").value}"

                    loadCategory(categoryId, binding.textPlaceCategoryDB)
                    loadCity(cityId, binding.textPlaceCityDB)

                    binding.textPlaceName.text = placename
                    binding.textPlaceDescription.text = placedescription
                    //binding.textPlacePublicDB.text = placepublic
                    if (placepublic != "") {
                        binding.textPlacePublicDB.text = placepublic
                    } else {
                        binding.textPlacePublicDB.text = "не указано"
                    }
                    //binding.textPlaceWebBD.text =placeweb
                    if (placeweb != "") {
                        binding.textPlaceWebBD.text = placeweb
                    } else {
                        binding.textPlaceWebBD.text = "не указано"
                    }
                    //binding.textPlaceAddressBD.text = placeaddress
                    if (placeaddress != "") {
                        binding.textPlaceAddressBD.text = placeaddress
                    } else {
                        binding.textPlaceAddressBD.text = "не указано"
                    }
                    //binding.textPlaceTimeBD.text = placetime
                    if (placetime != "") {
                        binding.textPlaceTimeBD.text = placetime
                    } else {
                        binding.textPlaceTimeBD.text = "не указано"
                    }
                    //binding.textPlaceTelephoneBD.text = placetelephone
                    if (placetelephone != "") {
                        binding.textPlaceTelephoneBD.text = placetelephone
                    } else {
                        binding.textPlaceTelephoneBD.text = "не указано"
                    }
                    binding.textPlaceAgeDB.text = placeage

                    try {
                        Glide.with(this@PlaceDetailsActivity)
                            .load(placeImage)
                            .placeholder(R.drawable.ic_image)
                            .into(binding.imageViewPlacePhoto)
                    }
                    catch (e: Exception) {
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadCategory(categoryId: String, categoryTv: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryname = "${snapshot.child("categoryname").value}"
                    categoryTv.text = categoryname
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadCity(cityId: String, cityTv: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(cityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cityname = "${snapshot.child("cityname").value}"
                    cityTv.text = cityname
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addToFavorite() {
        val timestamp = System.currentTimeMillis()
        val hashMap = hashMapOf<String, Any>()
        hashMap["placeId"] = placeId
        hashMap["timestamp"] = timestamp
        val ref =FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Место добавлено в избранное.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка добавления в избранное!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorite(context: Context, placeId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(this, "Место удалено из избранных.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка удаления из избранного!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIsFavorite() {
        val ref =FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        binding.buttonFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_fullwhite, 0, 0)
                        binding.buttonFavorite.text = "Убрать из избранного"
                    } else {
                        binding.buttonFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0)
                        binding.buttonFavorite.text = "Добавить в избранное"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}