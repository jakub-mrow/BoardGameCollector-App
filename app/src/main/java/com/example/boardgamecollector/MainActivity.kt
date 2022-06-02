package com.example.boardgamecollector

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamecollector.databinding.ActivityMainBinding
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        dbHandler = DBHandler(this, null)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

//        dbHandler.deleteUsers()
//        dbHandler.deleteBoardGames()
//        dbHandler.deleteDLC()

        if (dbHandler.userExists()){
            navController.navigate(R.id.action_setupFragment_to_profileFragment)
        }

    }

    @Suppress("DEPRECATION")
    private inner class BoardGamesDownloader: AsyncTask<String, Int, String>(){
        override fun onPreExecute() {
            super.onPreExecute()

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            navHostFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                if (fragment is SetupFragment) {
                    fragment.updateFragment()
                }

                if (fragment is SyncFragment) {
                    fragment.updateFragment()
                }
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            navHostFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                if (fragment is SetupFragment){
                    navHostFragment?.findNavController()?.navigate(R.id.action_setupFragment_to_profileFragment)
                }
                if (fragment is SyncFragment) {
                    navHostFragment?.findNavController()?.navigate(R.id.action_syncFragment_to_profileFragment)
                }
            }

        }

        override fun doInBackground(vararg args: String?): String {
            try {
                val url = URL("https://boardgamegeek.com/xmlapi2/collection?username=${args[0]}&stats=1&subtype=boardgame&excludesubtype=boardgameexpansion&own=1")
                var lengthOfFile = 0
                for (i in 0..4) {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    if (connection.responseCode != HttpURLConnection.HTTP_ACCEPTED) {
                        lengthOfFile = connection.contentLength
                        break
                    }
                    connection.disconnect()
                    Thread.sleep(3000)
                }
                val stream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/board_games.xml")
                val data = ByteArray(1024)
                var total: Long = 0
                var progress = 0
                var count = stream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100 / lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data, 0, count)
                    count = stream.read(data)
                }
                stream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                return "Wrong URL!"
            } catch (e: FileNotFoundException) {
                return "File not found!"
            } catch (e: IOException) {
                return "IO Exception!"
            }

            val boardGameList = ArrayList<BoardGame>()
            val dlcList = ArrayList<DLC>()

            var fileName = "board_games.xml"
            var inDir = File(filesDir, "XML")

            var file = File(inDir, fileName)

            var xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            xmlDoc.documentElement.normalize()
            if (xmlDoc.childNodes.item(0).nodeName == "message"){
                return  "fail"
            }
            var items: NodeList = xmlDoc.getElementsByTagName("item")

            for (i in 0 until items.length){
                val item : Node = items.item(i)
                if (item.nodeType == Node.ELEMENT_NODE){
                    val elem = item as Element
                    val children = elem.childNodes

                    var bggId: Long = 0
                    var title = "N/A"
                    var releaseYear: Int = 0
                    var imageUrl = "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.pngitem.com%2Fso%2Fboard-games%2F&psig=AOvVaw2W7UIk3Rmy7_g6ziNUPjOv&ust=1654205726550000&source=images&cd=vfe&ved=0CAwQjRxqFwoTCJDekdqajfgCFQAAAAAdAAAAABAJ"
                    var ranking: Int = 0

                    bggId = elem.getAttribute("objectid").toLong()
                    val rankingTags =  elem.getElementsByTagName("rank")

                    for (j in 0 until children.length){
                        val node = children.item(j)
                        if (node is Element){
                            when (node.nodeName){
                                "yearpublished" -> {
                                    releaseYear = node.textContent.toInt()
                                }
                                "name" -> {
                                    title = node.textContent.toString()
                                }
                                "thumbnail" -> {
                                    imageUrl = node.textContent.toString()
                                }
                            }
                        }
                    }

                    for (j in 0 until rankingTags.length) {
                        val item2: Node = rankingTags.item(j)
                        if (item2.nodeType == Node.ELEMENT_NODE) {
                            val elem2 = item2 as Element
                            if (elem2.getAttribute("friendlyname") == "Board Game Rank") {
                                if (elem2.getAttribute("value").toString() == "Not Ranked") {
                                    ranking = 0
                                } else {
                                    ranking = elem2.getAttribute("value").toInt()
                                }
                            }
                        }
                    }
                    val game = BoardGame(null,title,releaseYear,bggId,ranking,imageUrl)
                    boardGameList.add(game)
                }

            }

            boardGameList.forEach { dbHandler.addBoardGame(it) }

            val url = URL("https://boardgamegeek.com/xmlapi2/collection?username=${args[0]}&stats=1&subtype=boardgameexpansion&own=1")
            try {

                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream= url.openStream()

                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()

                val fos = FileOutputStream( "$testDirectory/dlc.xml")
                val data = ByteArray ( 1024)
                var count=0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() *100/ lengthOfFile
                    if (progress_temp %10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data,  0,count)
                    count =  isStream.read(data)
                }
                isStream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                return "Wrong URL!"
            } catch (e: FileNotFoundException) {
                return "File not found!"
            } catch (e: IOException) {
                return "IO Exception!"
            }

            fileName = "dlc.xml"
            inDir = File(filesDir, "XML")

            file = File(inDir, fileName)

            xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            xmlDoc.documentElement.normalize()
            if (xmlDoc.childNodes.item(0).nodeName == "message"){
                return  "fail"
            }
            items= xmlDoc.getElementsByTagName("item")

            for (i in 0 until items.length){
                try {
                    val item: Node = items.item(i)
                    if (item.nodeType == Node.ELEMENT_NODE) {
                        val elem = item as Element
                        val children = elem.childNodes


                        var bggId: Long = 0
                        var title = "N/A"
                        var releaseYear: Int = 0
                        var imageUrl = "https://i.imgur.com/4ma0EnA.png"
                        var ranking: Int = 0

                        bggId = elem.getAttribute("objectid").toLong()
                        val rankingTags = elem.getElementsByTagName("rank")

                        for (j in 0 until children.length) {
                            val node = children.item(j)
                            if (node is Element) {
                                when (node.nodeName) {
                                    "yearpublished" -> {
                                        releaseYear = node.textContent.toInt()
                                    }
                                    "name" -> {
                                        title = node.textContent.toString()
                                    }
                                    "thumbnail" -> {
                                        imageUrl = node.textContent.toString()
                                    }
                                }
                            }
                        }
                        Log.e("title", title)
                        val dlc = DLC(null, title, releaseYear, bggId, imageUrl)
                        dlcList.add(dlc)
                    }
                }
                catch(e: Exception){
                    continue
                }
            }

            dlcList.forEach { dbHandler.addDlc(it) }

            return "success"
        }
    }

    @Suppress("DEPRECATION")
    fun downloadData() {
        val userName = dbHandler.getName()
        if (userName != null){
            val gamesDownloader = BoardGamesDownloader()
            gamesDownloader.execute(userName)
        }
    }

}