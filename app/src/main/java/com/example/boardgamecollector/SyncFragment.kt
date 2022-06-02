package com.example.boardgamecollector

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.boardgamecollector.databinding.FragmentSyncBinding
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.system.exitProcess

class SyncFragment : Fragment() {
    private lateinit var binding: FragmentSyncBinding
    private lateinit var dbHandler: DBHandler

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSyncBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener{
            findNavController().navigate(R.id.action_syncFragment_to_profileFragment)
        }

        binding.syncUserBtn.setOnClickListener{
            val lastSync = dbHandler.getLastSyncDate()
            val convertedLastSync = lastSync!!.splitToSequence(" ").joinToString(separator = "T")
            val localDateTime = LocalDateTime.parse(convertedLastSync)
            val subtractedCurrentTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS)

            if (subtractedCurrentTime.isBefore(localDateTime)) {
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setMessage("Do you want to refresh the ranking? \nOnly 24h period between refresh times will apply any changes.")
                    .setCancelable(false)
                    .setPositiveButton("Yes", DialogInterface.OnClickListener{
                        dialog, id -> dbHandler.deleteDLC()
                                    dbHandler.deleteBoardGames()
                                    val mainActivity = activity as MainActivity
                                    mainActivity.downloadData()
                                    dbHandler.setLastSyncDate(convertDate(LocalDateTime.now()))
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener{
                        dialog, id -> dialog.cancel()
                    })
                val alert = alertDialogBuilder.create()
                alert.setTitle("Sync user update")
                alert.show()
            } else {
                dbHandler.deleteDLC()
                dbHandler.deleteBoardGames()
                val mainActivity = activity as MainActivity
                mainActivity.downloadData()
                dbHandler.setLastSyncDate(convertDate(LocalDateTime.now()))
            }
        }

        binding.newSyncBtn.setOnClickListener{
            dbHandler.bigDelete()
            dbHandler.deleteRanks()
            dbHandler.addUser(binding.enterNewUsername.text.toString(), convertDate(LocalDateTime.now()))
            val mainActivity = activity as MainActivity
            mainActivity.downloadData()
            dbHandler.setLastSyncDate(convertDate(LocalDateTime.now()))
        }

        binding.wipeDataBtn.setOnClickListener{
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setMessage("Do you want to wipe data and exit application?")
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener{
                        dialog, id -> dbHandler.deleteUsers()
                    dbHandler.deleteBoardGames()
                    dbHandler.deleteDLC()
                    dbHandler.deleteRanks()
                    activity?.finish()
                    exitProcess(0)
                })
                .setNegativeButton("No", DialogInterface.OnClickListener{
                        dialog, id -> dialog.cancel()
                })
            val alert = alertDialogBuilder.create()
            alert.setTitle("Wipe data")
            alert.show()
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHandler = DBHandler(context, null)
    }

    fun convertDate(date: LocalDateTime): String {
        val datePart = date.toString().split("T")[0]
        val timePart = date.toString().split("T")[1]
        val timeWithoutMiliSeconds = timePart.split(":").slice(0..1).joinToString(":")
        return "$datePart $timeWithoutMiliSeconds"
    }

    fun updateFragment(){
        binding.progressBar.visibility = View.VISIBLE
    }

}