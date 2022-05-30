package com.example.boardgamecollector

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamecollector.databinding.ActivityMainBinding
import android.os.Bundle
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
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            navHostFragment?.findNavController()?.navigate(R.id.action_setupFragment_to_profileFragment)
        }

        override fun doInBackground(vararg args: String?): String {
            try {
                val url = URL("https://boardgamegeek.com/xmlapi2/collection?username=${args[0]}&stats=1")
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

            val fileName = "board_games.xml"
            val inDir = File(filesDir, "XML")

            if (inDir.exists()) {
                val file = File(inDir, fileName)
                if (file.exists()) {
                    val xmlDoc: Document =
                        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    for (i in 0 until items.length) {
                        val itemNode: Node = items.item(i)
                        if (itemNode.nodeType == Node.ELEMENT_NODE) {
                            val children = itemNode.childNodes
                            var currentName: String? = null
                            var currentYear: String? = null
                            val currentBggId =
                                itemNode.attributes.getNamedItem("objectid").textContent
                            var currentRank: String? = null
                            var currentBayesAverage: String? = null
                            var currentImageUrl: String? = null
                            for (j in 0 until children.length) {
                                val node = children.item(j)
                                if (node is Element) {
                                    when (node.nodeName) {
                                        "name" -> currentName = node.textContent
                                        "yearpublished" -> currentYear = node.textContent
                                        "image" -> currentImageUrl = node.textContent
                                        "stats" -> {
                                            val statsChildren = node.childNodes
                                            for (k in 0 until statsChildren.length) {
                                                val statsChild = statsChildren.item(k)
                                                if (statsChild.nodeName == "rating") {
                                                    val ratingChildren = statsChild.childNodes
                                                    for (l in 0 until ratingChildren.length) {
                                                        val ratingChild = ratingChildren.item(l)
                                                        if (ratingChild.nodeName == "ranks") {
                                                            val ranksChildren =
                                                                ratingChild.childNodes
                                                            for (m in 0 until ranksChildren.length) {
                                                                val rankChild =
                                                                    ranksChildren.item(m)
                                                                if (rankChild is Element) {
                                                                    val type =
                                                                        rankChild.attributes.getNamedItem("type")
                                                                    val name =
                                                                        rankChild.attributes.getNamedItem("name")

                                                                    if (type != null && type.textContent == "subtype" && name != null && name.textContent == "boardgame") {
                                                                        currentRank = rankChild.attributes.getNamedItem("value").textContent
                                                                        currentBayesAverage = rankChild.attributes.getNamedItem("bayesaverage").textContent
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (currentYear != null && currentName != null && currentBggId != null && currentRank != null) {
                                if (currentRank == "Not Ranked" && currentBayesAverage != "Not Ranked") {
                                    dlcList.add(
                                        DLC(
                                            null,
                                            currentName,
                                            currentYear.toInt(),
                                            currentBggId.toLong(),
                                            currentImageUrl
                                        )
                                    )
                                } else {
                                    boardGameList.add(
                                        BoardGame(
                                            null,
                                            currentName,
                                            currentYear.toInt(),
                                            currentBggId.toLong(),
                                            if (currentRank != "Not Ranked") currentRank.toInt() else 0,
                                            currentImageUrl
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            boardGameList.forEach { dbHandler.addBoardGame(it) }
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