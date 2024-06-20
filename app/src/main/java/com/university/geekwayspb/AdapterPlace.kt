package com.university.geekwayspb

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.university.geekwayspb.databinding.RowPlacesBinding

class AdapterPlace : RecyclerView.Adapter<AdapterPlace.HolderPlace>, Filterable {

    private var context: Context
    public var placesArrayList: ArrayList<Places>
    private var filterList: ArrayList<Places>
    private var filter: FilterPlace? = null
    private lateinit var binding: RowPlacesBinding

    constructor(context: Context, placesArrayList: ArrayList<Places>) : super() {
        this.context = context
        this.placesArrayList = placesArrayList
        this.filterList = placesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterPlace.HolderPlace {
        binding = RowPlacesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPlace(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPlace, position: Int) {
        val model = placesArrayList[position]
        val placeId = model.id
        val categoryId = model.categoryId
        val cityId = model.cityId
        val placename = model.placename
        val placeImage = model.placeImage
        val placedescription = model.placedescription
        //val imageUrl = model.placeUrl
        val timestamp = model.timestamp

        holder.placenameTv.text = placename
        loadCategory(categoryId, holder.categoryTv)
        loadCity(cityId, holder.cityTv)
        holder.placeImage.loadImage(model.placeImage)

        holder.btnMore.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceDetailsActivity::class.java)
            intent.putExtra("placeId", placeId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: Places, holder: AdapterPlace.HolderPlace) {
        val placeId = model.id
        //val placeUrl = model.placeUrl
        val placeImage = model.placeImage
        val placename = model.placename
        val options = arrayOf("Изменить", "Удалить")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выбирите опцию")
            .setItems(options) {dialog, position ->
                if (position == 0) {
                    val intent = Intent(context, EditPlaceActivity::class.java)
                    intent.putExtra("placeId", placeId)
                    context.startActivity(intent)
                } else if (position == 1) {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Удалить")
                        .setMessage("Вы уверены что хотите удалить данную запись?")
                        .setPositiveButton("Да") { a, d->
                            Toast.makeText(context, "Удаление...", Toast.LENGTH_SHORT).show()
                            deletePlace(context, placeImage, placeId, placename)
                        }
                        .setNegativeButton("Отмена") { a, d ->
                            a.dismiss()
                        }
                        .show()
                }
            }
            .show()
    }

    private fun deletePlace(context: Context, placeImage: String, placeId: String, placename: String) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(placeImage)
        storageReference.delete()
            .addOnSuccessListener {
                val ref = FirebaseDatabase.getInstance().getReference("Places")
                ref.child(placeId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Место успешно удалено", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Ошибка удаления места!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ошибка удаления места!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return placesArrayList.size
    }

    inner class HolderPlace(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placenameTv = binding.placenameTv
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv
        var placeImage = binding.imageplaceTv
        var btnMore = binding.btnMore
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

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPlace(filterList, this)
        }
        return filter as FilterPlace
    }
}