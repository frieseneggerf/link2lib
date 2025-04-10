package de.floriii.link2lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import androidx.activity.enableEdgeToEdge

class HistoryActivity : AppCompatActivity() {

    private lateinit var listViewHistory: ListView
    private lateinit var historyAdapter: SimpleAdapter
    private lateinit var listHistory: MutableList<MutableMap<String, String>>
    private lateinit var buttonClearHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listViewHistory = findViewById(R.id.listViewHistory)
        buttonClearHistory = findViewById(R.id.buttonClearHistory)

        val mapDateUrl: MutableMap<String, String> = mutableMapOf()
        val historyFile = File(filesDir, getString(R.string.url_history_file))
        if (historyFile.exists()) {
            openFileInput(getString(R.string.url_history_file)).bufferedReader().useLines { lines ->
                for (l in lines) {
                    mapDateUrl[l.split(Char(9))[0]] = l.split(Char(9))[1]
                }
            }
        }

        listHistory = mutableListOf()
        historyAdapter = SimpleAdapter(this, listHistory,
            R.layout.list_item_twoline, arrayOf("time", "target_url"), intArrayOf(
                R.id.textListitemMain,
                R.id.textListitemSub
            ))
        mapDateUrl.entries.forEach {
            val rowMap: MutableMap<String, String> = mutableMapOf()
            rowMap["time"] = it.key
            rowMap["target_url"] = it.value
            listHistory.add(rowMap)
        }

        listViewHistory.adapter = historyAdapter

        listViewHistory.onItemClickListener = OnItemClickListener {_, _, pos, _ ->
            Toast.makeText(this@HistoryActivity, getString(R.string.opening_from_history), Toast.LENGTH_SHORT).show()
            openUrlInBrowser(this@HistoryActivity, listHistory[pos]["target_url"].toString())
        }

        listViewHistory.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, pos, _ ->
                val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("URL from history", listHistory[pos]["target_url"])
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@HistoryActivity, getString(R.string.copied_url), Toast.LENGTH_SHORT).show()
                true
            }

        buttonClearHistory.setOnClickListener {
            openFileOutput(getString(R.string.url_history_file), Context.MODE_PRIVATE).use {
                it.write("".toByteArray())
            }
            listHistory.clear()
            historyAdapter.notifyDataSetChanged()
            Toast.makeText(this@HistoryActivity, getString(R.string.cleared_history), Toast.LENGTH_SHORT).show()
        }
    }
}