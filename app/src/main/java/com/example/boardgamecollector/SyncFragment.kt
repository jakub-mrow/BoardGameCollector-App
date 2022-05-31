package com.example.boardgamecollector

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.boardgamecollector.databinding.FragmentSyncBinding

class SyncFragment : Fragment() {
    private lateinit var binding: FragmentSyncBinding
    private lateinit var dbHandler: DBHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSyncBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener{
            findNavController().navigate(R.id.action_syncFragment_to_profileFragment)
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHandler = DBHandler(context, null)

    }

}