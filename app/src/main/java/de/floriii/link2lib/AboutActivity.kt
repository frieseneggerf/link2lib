package de.floriii.link2lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import de.floriii.link2lib.databinding.ActivityAboutBinding
import kotlin.random.Random
import kotlin.random.nextInt

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private lateinit var license: SpannableString
    private var eggCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialToolbar4.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        binding.textViewAppVersion.text = getString(R.string.version, versionName)

        binding.textViewLicense.movementMethod = ScrollingMovementMethod()
        binding.textViewLicense.setOnClickListener{
            val license = resources.openRawResource(R.raw.license).
                bufferedReader().readText().
                replace(Regex("(?<!\\n)\\n(?!\\n|\\s)|(?<=\\n)[ \\t]+"), " ") //remove single line breaks (except if followed by spaces) and leading spaces in lines
            showLicenseDialog(this@AboutActivity, license)
        }

        val websiteUrl = getString(R.string.website_url)
        val webForm = HtmlCompat.fromHtml(getString(R.string.button_website).format(websiteUrl), HtmlCompat.FROM_HTML_MODE_COMPACT)
        binding.textButtonWebsite.text = webForm
        binding.textButtonWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
            startActivity(intent)
        }
        binding.textButtonWebsite.setOnLongClickListener {
            val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Personal Website URL", websiteUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this@AboutActivity, getString(R.string.copied_url), Toast.LENGTH_SHORT).show()
            true
        }

        val sourceUrl = getString(R.string.source_url)
        val sourceForm = HtmlCompat.fromHtml(getString(R.string.button_source).format(sourceUrl), HtmlCompat.FROM_HTML_MODE_COMPACT)
        binding.textButtonSource.text = sourceForm
        binding.textButtonSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, sourceUrl.toUri())
            startActivity(intent)
        }
        binding.textButtonSource.setOnLongClickListener {
            val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("App Source Code URL", sourceUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this@AboutActivity, getString(R.string.copied_url), Toast.LENGTH_SHORT).show()
            true
        }

        binding.textButtonLibraries.text = HtmlCompat.fromHtml(getString(R.string.button_libraries), HtmlCompat.FROM_HTML_MODE_COMPACT)
        binding.textButtonLibraries.setOnClickListener {
            val intent = Intent(this@AboutActivity, LibrariesActivity::class.java)
            startActivity(intent)
        }

        license = SpannableString(getString(R.string.license))
        val words: MutableMap<Int, Int> = mutableMapOf()
        var lastPos = 0
        var pos = 0
        for (c in license) {
            if (c == Char(32)) {
                words[lastPos] = pos
                lastPos = pos+1
            }
            pos++
        }
        words[lastPos] = pos
        val colors = arrayOf(Color.RED, Color.BLUE, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.GREEN)

        binding.imageAppIcon.setOnClickListener {
            eggCounter++
            if (eggCounter >= 3 && words.isNotEmpty()) {
                val k = words.keys.random()
                license.setSpan(ForegroundColorSpan(colors[Random.nextInt(0..5)]), k, words[k]!!, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                words.remove(k)
                binding.textViewLicense.text = license
            }
        }
    }
}