package de.floriii.link2lib

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.floriii.link2lib.databinding.ActivityMainBinding
import androidx.core.net.toUri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textProxy: TextView
    private lateinit var textViewGuide: TextView
    private lateinit var textButtonAddProxy: TextView
    private lateinit var textButtonHistory: TextView
    private lateinit var textButtonSettings: TextView
    private lateinit var textButtonAbout: TextView
    private lateinit var fabPaste: FloatingActionButton

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

        textProxy = findViewById(R.id.textProxy)
        fabPaste = findViewById(R.id.fabPaste)
        textViewGuide = findViewById(R.id.textViewGuide)
        textButtonAddProxy = findViewById(R.id.textButtonAddProxy)
        textButtonHistory = findViewById(R.id.textButtonHistory)
        textButtonSettings = findViewById(R.id.textButtonSettings)
        textButtonAbout = findViewById(R.id.textButtonAbout)

        textViewGuide.movementMethod = ScrollingMovementMethod()

        textButtonAddProxy.setOnClickListener {
            val intent = Intent(this@MainActivity, SetProxyActivity::class.java)
            startActivity(intent)
        }

        textButtonHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        textButtonSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 31) {
                val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, ("package:" + applicationContext.packageName).toUri())
                startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, ("package:" + applicationContext.packageName).toUri())
                startActivity(intent)
            }
        }

        textButtonAbout.setOnClickListener {
            val intent = Intent(this@MainActivity, AboutActivity::class.java)
            startActivity(intent)
        }

        fabPaste.setOnClickListener{
            getClipboardText(applicationContext)?.let{ clipText ->
                logUrl(applicationContext, clipText)
                openUrlInBrowser(this@MainActivity, clipText)
            }?: Toast.makeText(this@MainActivity, getString(R.string.no_text_in_clipboard), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        textProxy.text = getString(R.string.proxy_prefix).format(getActiveProxyDesc(this))
    }
}

/**
 * Appends a url to the currently active proxy url and opens it in the web browser
 * @param urlString the url of the resource to be accessed via the proxy
 */
fun openUrlInBrowser(context: Context, urlString: String) {
    val activeProxyUrl = getActiveProxyUrl(context)
    var targetUrl = activeProxyUrl.plus(urlString)
    if (activeProxyUrl == context.getString(R.string.default_proxy_url)) {
        Toast.makeText(context, context.getString(R.string.no_proxy_selected), Toast.LENGTH_SHORT).show()
    }
    if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
        targetUrl = "http://$targetUrl"
    }
    val browserIntent = Intent(Intent.ACTION_VIEW, targetUrl.toUri())
    context.startActivity(browserIntent)
}

/**
 * Retrieves the currently active proxy url from storage
 * @return the url of the currently active proxy
 */
fun getActiveProxyUrl(context: Context): String {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.active_proxy_file_key), Context.MODE_PRIVATE)
    val activeProxyUrl = sharedPref.getString(context.getString(R.string.active_proxy_url), context.getString(
        R.string.default_proxy_url
    ))
    return activeProxyUrl?: return context.getString(R.string.default_proxy_url)
}

/**
 * Retrieves the currently active proxy description from storage
 * @return the description of the currently active proxy
 */
fun getActiveProxyDesc(context: Context): String {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.active_proxy_file_key), Context.MODE_PRIVATE)
    val activeProxyDesc = sharedPref.getString(context.getString(R.string.active_proxy_desc), context.getString(
        R.string.default_proxy_desc
    ))
    return activeProxyDesc?: return context.getString(R.string.no_proxy_selected)
}

/**
 * Writes a string as currently active proxy url to storage
 * @param url the url of the currently active proxy
 */
fun setActiveProxy(context: Context, url: String?, description: String?) {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.active_proxy_file_key), Context.MODE_PRIVATE)
    with (sharedPref.edit()) {
        putString(context.getString(R.string.active_proxy_url), url)
        putString(context.getString(R.string.active_proxy_desc), description)
        apply()
    }
}

/**
 * Writes a url with the current time to the log file
 * @param url the url to be logged
 */
fun logUrl(context: Context, url: String) {
    val newLine: String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
        Char(9) +
        url.replace(Char(9).toString(), "%09") +
        "\n"
    context.openFileOutput(context.getString(R.string.url_history_file), Context.MODE_APPEND).use {
        it.write(newLine.toByteArray())
    }
}

fun getClipboardText(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
        clipboard.primaryClip?.getItemAt(0)?.text?.let {
            return it.toString()
        }?: return null
    } else {
        return null
    }
}
