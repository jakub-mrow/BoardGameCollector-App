package com.example.boardgamecollector

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.boardgamecollector.databinding.FragmentRankingHistoryBinding
import com.squareup.picasso.Picasso

class RankingHistoryFragment : Fragment() {
    private lateinit var binding: FragmentRankingHistoryBinding
    private lateinit var dbHandler: DBHandler
    private lateinit var adapter: SimpleCursorAdapter
    private var gameIdNew: Long? = null

    companion object {
        const val ID = "boardGameId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            gameIdNew = it.getLong(ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRankingHistoryBinding.inflate(inflater, container, false)

        dbHandler = DBHandler(requireContext(), null)
        setAdapter(requireContext(), dbHandler.findGameCursor(gameIdNew!!))

        setRankingsAdapter(requireContext(), dbHandler.findRankingsCursor(gameIdNew!!))

        binding.backBtn.setOnClickListener{
            findNavController().navigate(R.id.action_rankingHistoryFragment_to_boardGamesFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.boardGame.adapter = adapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
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

    private fun setRankingsAdapter(context: Context, cursor: Cursor){
        val columns = arrayOf("date", "rank")
        val id = intArrayOf(R.id.rankDate, R.id.rankPosition)
        adapter = SimpleCursorAdapter(context, R.layout.rankings_view_template, cursor, columns, id, 0)
        adapter.setViewBinder { view, dbCursor, column ->
            when (view.id) {
                R.id.rankPosition -> {
                    val textView = view as TextView
                    textView.text = dbCursor.getString(column)
                }
                R.id.rankDate -> {
                    val textView = view as TextView
                    textView.text =  dbCursor.getString(column)
                }
            }
            return@setViewBinder true
        }
    }
}