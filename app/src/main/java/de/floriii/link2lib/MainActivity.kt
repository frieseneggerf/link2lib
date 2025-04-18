package de.floriii.link2lib

import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import de.floriii.link2lib.databinding.ActivityMainBinding
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        //Handle shared urls without showing ui
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                logUrl(applicationContext, sharedText)
                openUrlInBrowser(this, sharedText)
            }
            finish()
        }
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data.let { sharedURI ->
                if (sharedURI != null) {
                    logUrl(applicationContext, sharedURI.toString())
                    openUrlInBrowser(this, sharedURI.toString())
                }
            }
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textButtonAddProxy.setOnClickListener {
            val intent = Intent(this@MainActivity, SetProxyActivity::class.java)
            startActivity(intent)
        }

        binding.textButtonHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.textButtonSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 31) {
                val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, ("package:" + applicationContext.packageName).toUri())
                startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, ("package:" + applicationContext.packageName).toUri())
                startActivity(intent)
            }
        }

        binding.textButtonAbout.setOnClickListener {
            val intent = Intent(this@MainActivity, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.fabPaste.setOnClickListener{
            getClipboardText(applicationContext)?.let{ clipText ->
                logUrl(applicationContext, clipText)
                openUrlInBrowser(this@MainActivity, clipText)
            }?: Toast.makeText(this@MainActivity, getString(R.string.no_text_in_clipboard), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.textProxy.text = getString(R.string.proxy_prefix).format(getActiveProxyDesc(this))
    }
}
