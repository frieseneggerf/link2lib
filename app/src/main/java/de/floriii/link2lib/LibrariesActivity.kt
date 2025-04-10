package de.floriii.link2lib

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject

class LibrariesActivity : AppCompatActivity() {

    private lateinit var listViewLibraries: ListView
    private lateinit var librariesAdapter: SimpleAdapter
    private lateinit var listLibraries: MutableList<MutableMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_libraries)

        listViewLibraries = findViewById(R.id.listViewLibraries)

        listLibraries = mutableListOf()
        librariesAdapter = SimpleAdapter(this, listLibraries,
            R.layout.library_info_card, arrayOf("name", "org", "license", "version"), intArrayOf(
                R.id.textViewName,
                R.id.textViewOrg,
                R.id.textViewLic,
                R.id.textViewVersion
            ))

        val jsonData = JSONObject(resources.openRawResource(R.raw.aboutlibraries).bufferedReader().readText())
        val licenses = jsonData.getJSONObject("licenses")
        val libs = jsonData.getJSONArray("libraries")
        for (i in 0 until libs.length()) {
            val lib = libs.getJSONObject(i)
            val rowMap: MutableMap<String, String> = mutableMapOf()
            try {
                rowMap["name"] = lib.getString("name")
            } catch (e: JSONException) {
                rowMap["name"] = ""
            }
            try {
                rowMap["org"] = lib.getJSONArray("developers").getJSONObject(0).getString("name")
            } catch (e: JSONException) {
                rowMap["org"] = ""
            }
            try {
                rowMap["license"] = licenses.getJSONObject(lib.getJSONArray("licenses").getString(0)).getString("name")
            } catch (e: JSONException) {
                rowMap["license"] = ""
            }
            try {
                rowMap["version"] = lib.getString("artifactVersion")
            } catch (e: JSONException) {
                rowMap["version"] = ""
            }
            listLibraries.add(rowMap)
        }
        listViewLibraries.adapter = librariesAdapter

        listViewLibraries.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
            val lib = libs.getJSONObject(pos)
            showLicenseDialog(this@LibrariesActivity, licenses.getJSONObject(lib.getJSONArray("licenses").getString(0)).getString("content"))
        }

    }
}

fun showLicenseDialog(context: Context, license: String) {
    val tv = TextView(context).apply {
        text = license
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        textSize = 11F
        setPadding(8, 8, 8, 8)
        isVerticalScrollBarEnabled = true
        movementMethod = ScrollingMovementMethod()
    }
    AlertDialog.Builder(context)
        .setView(tv)
        .show()
}