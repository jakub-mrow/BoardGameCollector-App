package com.example.boardgamecollector

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.boardgamecollector.databinding.FragmentProfileBinding
import java.time.LocalDateTime

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var dbHandler: DBHandler

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.profileNameTxt.text = dbHandler.getName().toString()
        binding.gamesCollectionText.text = "Games collection: " + dbHandler.countGames().toString()
        binding.dlcCollectionText.text = "DLC collection: " + dbHandler.countDLC().toString()
        binding.lastSyncText.text = "Last sync: "+ "\n" + convertDate(LocalDateTime.now())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.exitBtn.setOnClickListener {
            dbHandler.deleteUsers()
            dbHandler.deleteBoardGames()
            dbHandler.deleteDLC()
        }

        binding.listGamesbtn.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_boardGamesFragment)
        }

        binding.listDLCbtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_dlcFragment)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHandler = DBHandler(context, null)

    }

    fun convertDate(date: LocalDateTime): String {
        val datePart = date.toString().split("T")[0]
        val timePart = date.toString().split("T")[1]
        val timeWithoutMiliSeconds = timePart.split(":").slice(0..1).joinToString(":")
        //return date.toString().split("T").joinToString(" ")
        return "$datePart $timeWithoutMiliSeconds"
    }

}