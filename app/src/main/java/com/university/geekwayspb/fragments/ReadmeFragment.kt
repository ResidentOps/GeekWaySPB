package com.university.geekwayspb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toolbar
import com.university.geekwayspb.HOME
import com.university.geekwayspb.R

class ReadmeFragment : Fragment() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var myToolbar: Toolbar
    private lateinit var btnReadmeOk: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_readme, container, false)

        //Инициализация виджетов, текстов и кнопок
        btnReadmeOk = root.findViewById(R.id.buttonReadmeOkFragment)

        //Кнопка "ОК" (закрывает ReadmeActivity )
        btnReadmeOk.setOnClickListener {
            HOME.navController.navigate(R.id.action_readmeFragment_to_profileFragment)
        }

        return root
    }
}