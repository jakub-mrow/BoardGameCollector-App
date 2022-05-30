package com.example.boardgamecollector

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.boardgamecollector.databinding.FragmentSetupBinding
import java.time.LocalDateTime

class SetupFragment : Fragment() {
    private lateinit var binding: FragmentSetupBinding
    private lateinit var dbHandler: DBHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startAppBtn.setOnClickListener {
            dbHandler.addUser(binding.enterUsername.text.toString(), convertDate(LocalDateTime.now()))
            val mainActivity = activity as MainActivity
            mainActivity.downloadData()
            //findNavController().navigate(R.id.action_setupFragment_to_profileFragment)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHandler = DBHandler(context, null)

    }

    fun updateFragment(){
        binding.progressBar.visibility = View.VISIBLE
    }

    fun convertDate(date: LocalDateTime): String {
        return date.toString().split("T").joinToString(" ")
    }

}