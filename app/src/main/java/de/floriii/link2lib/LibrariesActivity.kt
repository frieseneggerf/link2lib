package de.floriii.link2lib

import android.os.Bundle
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import de.floriii.link2lib.databinding.ActivityLibrariesBinding
import org.json.JSONException
import org.json.JSONObject

class LibrariesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibrariesBinding
    private lateinit var librariesAdapter: SimpleAdapter
    private lateinit var listLibraries: MutableList<MutableMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityLibrariesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialToolbar5.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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
        binding.listViewLibraries.adapter = librariesAdapter

        binding.listViewLibraries.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
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
