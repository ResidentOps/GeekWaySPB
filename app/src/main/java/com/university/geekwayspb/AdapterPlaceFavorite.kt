package com.university.geekwayspb

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekwayspb.databinding.RowPlacesFavoriteBinding

class AdapterPlaceFavorite : RecyclerView.Adapter<AdapterPlaceFavorite.HolderPlaceFavorite>, Filterable {

    private val context: Context
    var placesArrayList: ArrayList<Places>
    private lateinit var binding: RowPlacesFavoriteBinding
    public lateinit var filterList: ArrayList<Places>
    private var filter: FilterFavorite? = null

    constructor(context: Context, placesArrayList: ArrayList<Places>) {
        this.context = context
        this.placesArrayList = placesArrayList
        this.filterList = placesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlaceFavorite {
        binding = RowPlacesFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPlaceFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPlaceFavorite, position: Int) {
        val model = placesArrayList[position]
        val placeId = model.id
        val categoryId = model.categoryId
        val placename = model.placename
        val placeImage = model.placeImage
        val placedescription = model.placedescription
        //val imageUrl = model.placeUrl
        val timestamp = model.timestamp

        loadPlaceDetails(model, holder)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceDetailsActivity::class.java)
            intent.putExtra("placeId", model.id)
            context.startActivity(intent)
        }

        holder.btnFavorite.setOnClickListener {
            removeFromFavorite(context, model.id)
        }

    }

    private fun loadPlaceDetails(model: Places, holder: AdapterPlaceFavorite.HolderPlaceFavorite) {
        val placeId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.child(placeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val categoryname = "${snapshot.child("categoryname").value}"
                    val cityId = "${snapshot.child("cityId").value}"
                    val cityname = "${snapshot.child("cityname").value}"
                    val id = "${snapshot.child("id").value}"
                    val placeAddress = "${snapshot.child("placeAddress").value}"
                    val placeAge = "${snapshot.child("placeAge").value}"
                    val placename = "${snapshot.child("placename").value}"
                    val placeImage = "${snapshot.child("placeImage").value}"

                    model.isFavorite = true
                    model.placename = placename
                    model.placeImage = placeImage
                    model.categoryId = categoryId
                    model.cityId = cityId

                    holder.placenameTv.text = placename
                    loadCategory(categoryId, holder.categoryTv)
                    loadCity(cityId, holder.cityTv)
                    holder.placeImage.loadImage(model.placeImage)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterFavorite(filterList, this)
        }
        return filter as FilterFavorite
    }

    override fun getItemCount(): Int {
        return placesArrayList.size
    }

    inner class HolderPlaceFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placenameTv = binding.placenameTv
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv
        var placeImage = binding.imageplaceTv
        var btnFavorite = binding.btnFavorite
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

    private fun removeFromFavorite(context: Context, placeId: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(placeId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(context, "Место удалено из избранных.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ошибка удаления из избранного!", Toast.LENGTH_SHORT).show()
            }
    }
}