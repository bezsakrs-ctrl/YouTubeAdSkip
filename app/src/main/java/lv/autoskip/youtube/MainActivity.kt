package lv.autoskip.youtube

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(TextView(this).apply {
            text = "YouTube AutoSkip"
            textSize = 26f
        })
        root.addView(TextView(this).apply {
            text = "Automātiski izlaiž reklāmas, tiklīdz parādās izlaišanas poga. Atbalsts: YouTube un Chrome."
            textSize = 16f
            setPadding(0, pad / 2, 0, pad)
        })
        val skip = Switch(this).apply {
            text = "Automātiski izlaist reklāmas"
            textSize = 17f
            isChecked = prefs.getBoolean("skip", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("skip", checked).apply() }
        }
        root.addView(skip)
        val close = Switch(this).apply {
            text = "Aizvērt reklāmas uznirstošos logus"
            textSize = 17f
            isChecked = prefs.getBoolean("close", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("close", checked).apply() }
        }
        root.addView(close)
        root.addView(Button(this).apply {
            text = "Atvērt pieejamības iestatījumus"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = pad })
        root.addView(TextView(this).apply {
            text = "Lai aplikācija darbotos, Android iestatījumos jāieslēdz YouTube AutoSkip pieejamības pakalpojums. Pakalpojums darbojas tikai YouTube un Chrome logos."
            textSize = 13f
            setPadding(0, pad, 0, 0)
        })
        setContentView(root)
    }
}
