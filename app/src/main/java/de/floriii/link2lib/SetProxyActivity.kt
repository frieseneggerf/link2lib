package de.floriii.link2lib

import android.app.AlertDialog
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import de.floriii.link2lib.databinding.ActivitySetProxyBinding
import de.floriii.link2lib.databinding.AddProxyDialogBinding
import java.io.File


class SetProxyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetProxyBinding
    private lateinit var proxyAdapter: SimpleAdapter
    private lateinit var listProxyList: MutableList<MutableMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivitySetProxyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialToolbar2.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonNewProxy.setOnClickListener {
            val dialogBinding = AddProxyDialogBinding.inflate(layoutInflater)
            AlertDialog.Builder(this@SetProxyActivity)
                .setTitle(getString(R.string.add_new_proxy))
                .setView(dialogBinding.root)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val proxyDesc = dialogBinding.editTextProxyDesc.text.toString().filterNot{ char -> char == Char(9)}
                    val proxyUrl = dialogBinding.editTextProxyUrl.text.toString().replace(Char(9).toString(), "%09")
                    val rowMap: MutableMap<String, String> = mutableMapOf()
                    rowMap["description"] = proxyDesc
                    rowMap["proxy_url"] = proxyUrl
                    listProxyList.add(rowMap)
                    proxyAdapter.notifyDataSetChanged()

                    saveProxyList(applicationContext, listProxyList)
                    Toast.makeText(this@SetProxyActivity, getString(R.string.added_proxy), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel)) {_, _ -> }
                .setOnDismissListener {
                    Toast.makeText(this@SetProxyActivity, getString(R.string.canceled), Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        val mapDescrUrl: MutableMap<String, String> = mutableMapOf()
        val proxyFile = File(filesDir, getString(R.string.proxy_list_file))
        if (!proxyFile.exists()) {
            resources.openRawResource(R.raw.default_proxies).bufferedReader().useLines { lines ->
                for (l in lines) {
                    mapDescrUrl[l.split(Char(9))[0]] = l.split(Char(9))[1]
                }
            }
        } else {
            openFileInput(getString(R.string.proxy_list_file)).bufferedReader().useLines { lines ->
                for (l in lines) {
                    mapDescrUrl[l.split(Char(9))[0]] = l.split(Char(9))[1]
                }
            }
        }

        listProxyList = mutableListOf()
        proxyAdapter = SimpleAdapter(this, listProxyList,
            R.layout.list_item_twoline, arrayOf("description", "proxy_url"), intArrayOf(
                R.id.textListitemMain,
                R.id.textListitemSub
            ))
        mapDescrUrl.entries.forEach {
            val rowMap: MutableMap<String, String> = mutableMapOf()
            rowMap["description"] = it.key
            rowMap["proxy_url"] = it.value
            listProxyList.add(rowMap)
        }
        binding.listViewProxies.adapter = proxyAdapter

        binding.listViewProxies.onItemClickListener = OnItemClickListener {_, _, pos, _ ->
            val selectedProxy = listProxyList[pos]
            if (selectedProxy["proxy_url"] != null) {
                Toast.makeText(this@SetProxyActivity, getString(R.string.selected_new_proxy).format(selectedProxy["description"]), Toast.LENGTH_SHORT).show()
                setActiveProxy(this@SetProxyActivity, selectedProxy["proxy_url"], selectedProxy["description"])
                binding.textSelectedProxy.text = getString(R.string.proxy_prefix).format(getActiveProxyDesc(this@SetProxyActivity))
            }
        }

        binding.listViewProxies.onItemLongClickListener = OnItemLongClickListener {_, _, pos, _ ->
            listProxyList.removeAt(pos)
            saveProxyList(applicationContext, listProxyList)
            proxyAdapter.notifyDataSetChanged()
            true
        }


        binding.textSelectedProxy.text = getString(R.string.proxy_prefix).format(getActiveProxyDesc(this))
    }
}
