package de.floriii.link2lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.enableEdgeToEdge
import de.floriii.link2lib.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: SimpleAdapter
    private lateinit var listHistory: MutableList<MutableMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialToolbar3.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val historyFile = File(filesDir, getString(R.string.url_history_file))
        listHistory = mutableListOf()
        if (historyFile.exists()) {
            openFileInput(historyFile.name).bufferedReader().useLines { lines ->
                for (l in lines) {
                    val rowMap: MutableMap<String, String> = mutableMapOf()
                    val data = l.split(Char(9))
                    rowMap["time"] = data[0]
                    rowMap["target_url"] = data[1]
                    listHistory.add(0, rowMap)
                }
            }
        }
        historyAdapter = SimpleAdapter(this, listHistory,
            R.layout.list_item_twoline, arrayOf("time", "target_url"), intArrayOf(
                R.id.textListitemMain,
                R.id.textListitemSub
            ))
        binding.listViewHistory.adapter = historyAdapter

        binding.listViewHistory.onItemClickListener = OnItemClickListener {_, _, pos, _ ->
            Toast.makeText(this@HistoryActivity, getString(R.string.opening_from_history), Toast.LENGTH_SHORT).show()
            openUrlInBrowser(this@HistoryActivity, listHistory[pos]["target_url"].toString())
        }

        binding.listViewHistory.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, pos, _ ->
                val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("URL from history", listHistory[pos]["target_url"])
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@HistoryActivity, getString(R.string.copied_url), Toast.LENGTH_SHORT).show()
                true
            }

        binding.buttonClearHistory.setOnClickListener {
            openFileOutput(getString(R.string.url_history_file), Context.MODE_PRIVATE).use {
                it.write("".toByteArray())
            }
            listHistory.clear()
            historyAdapter.notifyDataSetChanged()
            Toast.makeText(this@HistoryActivity, getString(R.string.cleared_history), Toast.LENGTH_SHORT).show()
        }
    }
}