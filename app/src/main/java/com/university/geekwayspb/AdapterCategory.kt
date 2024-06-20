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
import com.university.geekwayspb.databinding.RowCategoriesBinding

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<Categories>
    private var filterList: ArrayList<Categories>
    private var filter: FilterCategory? = null
    private lateinit var binding: RowCategoriesBinding

    constructor(context: Context, categoryArrayList: ArrayList<Categories>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterCategory.HolderCategory {
        binding = RowCategoriesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterCategory.HolderCategory, position: Int) {
        val model = categoryArrayList[position]
        val id = model.id
        val categoryname = model.categoryname
        val uid = model.uid
        val timestamp = model.timestamp

        holder.categoryTv.text = categoryname

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditCategoryActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("cityname", categoryname)
            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Удалить")
                .setMessage("Вы уверены что хотите удалить данную запись?")
                .setPositiveButton("Да") { a, d->
                    Toast.makeText(context, "Удаление...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Отмена") { a, d ->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlacesActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("categoryname", categoryname)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: Categories, holder: AdapterCategory.HolderCategory) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
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
        return categoryArrayList.size
    }

    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView) {
        var categoryTv: TextView = binding.categoryTv
        var btnDelete: ImageButton = binding.btnDelete
        var btnEdit: ImageButton = binding.btnEdit
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}