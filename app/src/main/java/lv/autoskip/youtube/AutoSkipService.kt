package lv.autoskip.youtube

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale

/**
 * Local-only accessibility helper. It only interacts with the YouTube and Chrome
 * accessibility trees and never captures or uploads screen contents.
 */
class AutoSkipService : AccessibilityService() {
    private var lastActionAt = 0L
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName != YOUTUBE_PACKAGE && packageName != CHROME_PACKAGE) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastActionAt < ACTION_COOLDOWN_MS) return

        val root = rootInActiveWindow ?: return
        try {
            if (prefs.getBoolean("skip", true) && clickFirstMatch(root, ::isSkipLabel)) {
                lastActionAt = now
                return
            }

            // Closing is intentionally strict: generic "Close" / "X" controls are
            // not tapped, because they could dismiss unrelated YouTube dialogs.
            if (prefs.getBoolean("close", true) && hasAdMarker(root) &&
                clickFirstMatch(root, ::isExplicitAdCloseLabel)) {
                lastActionAt = now
            }
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    private fun isSkipLabel(node: AccessibilityNodeInfo): Boolean {
        val value = label(node)
        return value in setOf(
            "skip ad", "skip ads", "skip",
            "izlaist reklāmu", "izlaist reklamu", "izlaist reklāmas",
            "izlaist reklamas", "izlaist reklāmu tagad"
        )
    }

    private fun isExplicitAdCloseLabel(node: AccessibilityNodeInfo): Boolean {
        val value = label(node)
        return value in setOf(
            "close ad", "dismiss ad", "close advertisement", "dismiss advertisement",
            "aizvērt reklāmu", "aizvert reklamu", "aizvērt reklāmu", "noņemt reklāmu"
        )
    }

    private fun hasAdMarker(root: AccessibilityNodeInfo): Boolean = walk(root) { node ->
        val value = label(node)
        value == "ad" || value == "sponsored" || value == "advertisement" ||
            value == "reklāma" || value == "reklama" || value.contains("sponsored")
    }

    private fun clickFirstMatch(root: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): Boolean {
        var clicked = false
        walk(root) { node ->
            if (predicate(node) && clickNodeOrParent(node)) {
                clicked = true
                true
            } else false
        }
        return clicked
    }

    /** Depth-first traversal; stops as soon as a match/action is found. */
    private fun walk(root: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): Boolean {
        fun visit(node: AccessibilityNodeInfo, depth: Int): Boolean {
            if (depth > MAX_TREE_DEPTH) return false
            if (predicate(node)) return true
            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                val matched = visit(child, depth + 1)
                @Suppress("DEPRECATION")
                child.recycle()
                if (matched) return true
            }
            return false
        }
        return visit(root, 0)
    }

    private fun label(node: AccessibilityNodeInfo): String {
        val text = node.text?.toString().orEmpty()
        val description = node.contentDescription?.toString().orEmpty()
        return (if (description.isNotBlank()) description else text)
            .trim().lowercase(Locale.ROOT)
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(node)
        repeat(MAX_PARENT_STEPS) {
            val candidate = current ?: return false
            if (candidate.isClickable && candidate.isEnabled) {
                val success = candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                @Suppress("DEPRECATION")
                candidate.recycle()
                return success
            }
            val parent = candidate.parent
            @Suppress("DEPRECATION")
            candidate.recycle()
            current = parent
        }
        current?.let {
            @Suppress("DEPRECATION")
            it.recycle()
        }
        return false
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val CHROME_PACKAGE = "com.android.chrome"
        private const val ACTION_COOLDOWN_MS = 500L
        private const val MAX_TREE_DEPTH = 35
        private const val MAX_PARENT_STEPS = 7
    }
}
