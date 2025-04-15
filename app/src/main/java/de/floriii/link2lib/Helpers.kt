package de.floriii.link2lib

import android.app.AlertDialog
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

/**
 * Return the clip board content if it is text, or null
 * @return Text from the clip board
 */
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

/**
 * Show a dialog containing the license text with an optional link to the website
 * @param license The text of the license to be displayed
 * @param website Optionally the url of a website to be linked
 */
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
        .also {
            if (website != null) {
                it.setNegativeButton(context.getString(R.string.website)) { _, _ ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, website)
                    context.startActivity(browserIntent)
                }
            }
        }
        .show()
}

/**
 * Save the list of available proxies to proxy_list_file as defined in strings.xml
 * @param listProxyList a list of maps containing one Description-URL-pair each
 */
fun saveProxyList(context: Context, listProxyList: MutableList<MutableMap<String, String>>) {
    var toSave = ""
    for (p in listProxyList) {
        toSave = toSave.plus(p["description"] + Char(9) + p["proxy_url"] + "\n")
    }
    context.openFileOutput(context.getString(R.string.proxy_list_file), Context.MODE_PRIVATE).use {
        it.write(toSave.toByteArray())
    }
}