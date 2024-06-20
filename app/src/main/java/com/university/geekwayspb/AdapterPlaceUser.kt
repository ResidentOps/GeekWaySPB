package com.university.geekwayspb

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekwayspb.databinding.RowPlacesUserBinding

class AdapterPlaceUser : RecyclerView.Adapter<AdapterPlaceUser.HolderPlaceUser>, Filterable {

    private var context: Context
    public var placesArrayList: ArrayList<Places>
    private lateinit var binding: RowPlacesUserBinding
    public var filterList: ArrayList<Places>
    private var filter: FilterPlaceUser? = null

    constructor(context: Context, placesArrayList: ArrayList<Places>) : super() {
        this.context = context
        this.placesArrayList = placesArrayList
        this.filterList = placesArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlaceUser {
        binding = RowPlacesUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPlaceUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPlaceUser, position: Int) {
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

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceDetailsActivity::class.java)
            intent.putExtra("placeId", placeId)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPlaceUser(filterList, this)
        }
        return filter as FilterPlaceUser
    }

    override fun getItemCount(): Int {
        return placesArrayList.size
    }

    inner class HolderPlaceUser(itemView: View): RecyclerView.ViewHolder(itemView) {
        var placenameTv = binding.placenameTv
        var categoryTv = binding.categoryTv
        var cityTv = binding.cityTv
        var placeImage = binding.imageplaceTv
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
}