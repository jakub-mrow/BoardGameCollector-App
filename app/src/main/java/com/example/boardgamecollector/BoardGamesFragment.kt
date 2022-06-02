package com.example.boardgamecollector

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.boardgamecollector.databinding.FragmentBoardGamesBinding
import com.squareup.picasso.Picasso

class BoardGamesFragment : Fragment() {
    private lateinit var binding: FragmentBoardGamesBinding
    private lateinit var dbHandler: DBHandler
    private lateinit var adapter: SimpleCursorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoardGamesBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener{
            findNavController().navigate(R.id.action_boardGamesFragment_to_profileFragment)
        }

        binding.listOfBoardGames.setOnItemClickListener { _, _, _, gameId ->
            val boardGame = dbHandler.findGame(gameId) ?: return@setOnItemClickListener
            val bundle = Bundle()
            bundle.putLong(RankingHistoryFragment.ID, gameId)
            findNavController().navigate(R.id.action_boardGamesFragment_to_rankingHistoryFragment, bundle)
        }

        binding.gameRank.setOnClickListener{
            binding.gameRank.setTypeface(null, Typeface.BOLD)

            binding.gameTitle.setTypeface(null, Typeface.NORMAL)
            binding.gameId.setTypeface(null, Typeface.NORMAL)
            binding.gameYear.setTypeface(null, Typeface.NORMAL)

            binding.listOfBoardGames.adapter = null
            setAdapter(requireContext(), dbHandler.findGameCursorSortRank())
            binding.listOfBoardGames.adapter = adapter
        }

        binding.gameTitle.setOnClickListener{
            binding.gameTitle.setTypeface(null, Typeface.BOLD)

            binding.gameRank.setTypeface(null, Typeface.NORMAL)
            binding.gameId.setTypeface(null, Typeface.NORMAL)
            binding.gameYear.setTypeface(null, Typeface.NORMAL)

            binding.listOfBoardGames.adapter = null
            setAdapter(requireContext(), dbHandler.findGameCursorSortTitle())
            binding.listOfBoardGames.adapter = adapter
        }

        binding.gameId.setOnClickListener{
            binding.gameId.setTypeface(null, Typeface.BOLD)

            binding.gameTitle.setTypeface(null, Typeface.NORMAL)
            binding.gameRank.setTypeface(null, Typeface.NORMAL)
            binding.gameYear.setTypeface(null, Typeface.NORMAL)

            binding.listOfBoardGames.adapter = null
            setAdapter(requireContext(), dbHandler.findGamesCursor())
            binding.listOfBoardGames.adapter = adapter
        }

        binding.gameYear.setOnClickListener{
            binding.gameYear.setTypeface(null, Typeface.BOLD)

            binding.gameTitle.setTypeface(null, Typeface.NORMAL)
            binding.gameId.setTypeface(null, Typeface.NORMAL)
            binding.gameRank.setTypeface(null, Typeface.NORMAL)

            binding.listOfBoardGames.adapter = null
            setAdapter(requireContext(), dbHandler.findGameCursorSortReleaseDate())
            binding.listOfBoardGames.adapter = adapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listOfBoardGames.adapter = adapter
        binding.gameId.setTypeface(null, Typeface.BOLD)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHandler = DBHandler(context, null)
        setAdapter(context, dbHandler.findGamesCursor())
    }

    private fun setAdapter(context: Context, cursor: Cursor) {
        val columns = arrayOf("_id", "title", "release_date", "rank", "image")
        val id = intArrayOf(R.id.gameId, R.id.gameTitle, R.id.gameYear, R.id.gameRank, R.id.gameImage)
        adapter = SimpleCursorAdapter(context, R.layout.game_boards_view_template, cursor, columns, id, 0)
        adapter.setViewBinder { view, dbCursor, column ->
            when (view.id) {
                R.id.gameId, R.id.gameTitle, R.id.gameYear, R.id.gameRank -> {
                    val textView = view as TextView
                    textView.text = dbCursor.getString(column)
                }
                R.id.gameImage -> {
                    val imageUrl = dbCursor.getString(column)
                    val imageView = view as ImageView
                    if (imageUrl != null){
                        Picasso.get().load(imageUrl).into(imageView)
                    }
                }
            }
            return@setViewBinder true
        }
    }
}