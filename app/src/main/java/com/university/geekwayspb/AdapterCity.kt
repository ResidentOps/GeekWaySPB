package com.university.geekwayspb

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.university.geekwayspb.databinding.RowCitiesBinding

class AdapterCity : RecyclerView.Adapter<AdapterCity.HolderCity>, Filterable {

    private val context: Context
    public var cityArrayList: ArrayList<Cities>
    private var filterList: ArrayList<Cities>
    private var filter: FilterCity? = null
    private lateinit var binding: RowCitiesBinding

    constructor(context: Context, cityArrayList: ArrayList<Cities>) {
        this.context = context
        this.cityArrayList = cityArrayList
        this.filterList = cityArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCity {
        binding = RowCitiesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCity(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCity, position: Int) {
        val model = cityArrayList[position]
        //val id = model.id
        val cityId = model.id
        val cityname = model.cityname
        val citydescription = model.citydescription
        val cityImage = model.cityImage
        val uid = model.uid
        val timestamp = model.timestamp

        holder.cityTv.text = cityname

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditCityActivity::class.java)
            intent.putExtra("cityId", cityId)
            intent.putExtra("cityname", cityname)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CityDetailsActivity::class.java)
            intent.putExtra("cityId", cityId)
            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Удалить")
                .setMessage("Вы уверены что хотите удалить данную запись?")
                .setPositiveButton("Да") { a, d->
                    Toast.makeText(context, "Удаление...", Toast.LENGTH_SHORT).show()
                    deleteCity(model, holder)
                }
                .setNegativeButton("Отмена") { a, d ->
                    a.dismiss()
                }
                .show()
        }
        
    }

    private fun deleteCity(model: Cities, holder: HolderCity) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Запись удалена.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ошибка удаления!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return cityArrayList.size
    }

    inner class HolderCity(itemView: View): RecyclerView.ViewHolder(itemView) {
        var cityTv: TextView = binding.cityTv
        var btnDelete: ImageButton = binding.btnDelete
        var btnEdit: ImageButton = binding.btnEdit
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCity(filterList, this)
        }
        return filter as FilterCity
    }
}