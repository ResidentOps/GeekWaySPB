package com.university.geekwayspb

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekwayspb.databinding.RowCommentsBinding

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment> {

    val context: Context
    val commentArrayList: ArrayList<Comments>
    private lateinit var binding: RowCommentsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<Comments>) {
        this.context = context
        this.commentArrayList = commentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = commentArrayList[position]
        val id = model.id
        val placeId = model.placeId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        holder.commentTv.text = comment
        loadUserDetails(model, holder)

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }
        }
    }

    private fun deleteCommentDialog(model: Comments, holder: HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удалить комментарий")
            .setMessage("Вы увеерены, что хотите удалить этот комментарий?")
            .setPositiveButton("Да") {d,e ->
                val placeId = model.placeId
                val commentId = model.id
                val ref = FirebaseDatabase.getInstance().getReference("Places")
                ref.child(placeId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Комментарий успешно удален.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Ошибка удаления комментария!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Нет") {d,e ->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: Comments, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("Name").value}"
                    holder.nameTv.text = name
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }


    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTv = binding.nameTv
        val commentTv = binding.commentTv
    }
}