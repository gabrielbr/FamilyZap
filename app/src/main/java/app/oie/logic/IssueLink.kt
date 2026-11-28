package app.oie.logic

import java.net.URLEncoder

/**
 * The GitHub "new issue" address for a problem report (docs/flows/problem-report.md, v0.18).
 * The browser opens it filled in; the parent submits it as themselves. Oiê holds no token and
 * sends nothing on its own. A web address has a length limit, so when the report doesn't fit the
 * newest lines are kept and the body says so.
 *
 * v0.58: the start of the last crash (what went wrong, its first lines, and each "Caused by") is kept
 * too, above the newest lines, even when it's older. Issue #88 lost it: only the bottom of a long
 * stack trace fitted, without the error itself.
 */
object IssueLink {
    const val REPO = "gabrielbr/oie-dev" // private: only the owner can file (owner's decision, 2026-11-19)
    /** Keeps the whole address well under the ~8k characters browsers and GitHub accept. */
    const val MAX_ENCODED_BODY = 6_000
    /** How much of the last crash is kept at most: its first lines and every "Caused by" with the line after it. */
    const val CRASH_HEAD = 6
    const val CRASH_KEPT_MAX = 24
    const val GAP = "…"

    /** The indexes of the last crash's opening lines, oldest first; empty when the report has no crash. */
    fun crashHead(lines: List<String>): List<Int> {
        val start = lines.indexOfLast { it.contains(" CRASH on ") }
        if (start < 0) return emptyList()
        val out = sortedSetOf<Int>()
        for (i in start until minOf(lines.size, start + CRASH_HEAD)) out += i
        for (i in start + 1 until lines.size) {
            if (lines[i].trimStart().startsWith("Caused by:")) { out += i; if (i + 1 < lines.size) out += i + 1 }
        }
        return out.take(CRASH_KEPT_MAX)
    }

    fun build(title: String, header: String, lines: List<String>, cutNote: (kept: Int, total: Int) -> String): String {
        fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
        val pinned = crashHead(lines)
        fun shown(tail: Int): List<String> {
            val from = lines.size - tail
            val keep = (pinned.filter { it < from } + (from until lines.size)).distinct().sorted()
            val out = mutableListOf<String>()
            keep.forEachIndexed { k, i -> if (k > 0 && i != keep[k - 1] + 1) out += GAP; out += lines[i] }
            return out
        }
        fun body(tail: Int): String {
            val kept = shown(tail)
            val count = kept.count { it != GAP }
            val note = if (count < lines.size) "\n\n_" + cutNote(count, lines.size) + "_" else ""
            return header + note + "\n\n```\n" + kept.joinToString("\n") + "\n```"
        }
        var tail = lines.size
        var b = body(tail)
        while (tail > 0 && enc(b).length > MAX_ENCODED_BODY) {
            tail = if (tail > 20) (tail * 0.8).toInt() else tail - 1
            b = body(tail)
        }
        return "https://github.com/$REPO/issues/new?title=${enc(title)}&body=${enc(b)}"
    }
}
