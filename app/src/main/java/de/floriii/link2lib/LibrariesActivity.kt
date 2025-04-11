package de.floriii.link2lib

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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
            try {
                val lib = libs.getJSONObject(pos)
                val text = licenses.getJSONObject(lib.getJSONArray("licenses").getString(0)).getString("content")
                val website = try {
                    lib.getString("website").toUri()
                } catch (e: JSONException) {
                    null
                }
                showLicenseDialog(this@LibrariesActivity, text, website)
            } catch (e: JSONException) {
                Toast.makeText(this@LibrariesActivity, getString(R.string.no_text), Toast.LENGTH_SHORT).show()
            }
        }

    }
}

fun showLicenseDialog(context: Context, license: String, website: Uri? = null) {
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
        .setPositiveButton(context.getString(R.string.close)) { _, _ ->}
        .let {
            if (website != null) {
                it.setNegativeButton(context.getString(R.string.website)) { _, _ ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, website)
                    context.startActivity(browserIntent)
                }
            } else {
                it
            }
        }
        .show()
}