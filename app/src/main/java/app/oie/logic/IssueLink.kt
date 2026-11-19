package app.oie.logic

import java.net.URLEncoder

/**
 * The GitHub "new issue" address for a problem report (docs/flows/problem-report.md, v0.18).
 * The browser opens it filled in; the parent submits it as themselves. Oiê holds no token and
 * sends nothing on its own. A web address has a length limit, so when the report doesn't fit the
 * newest lines are kept and the body says so.
 */
object IssueLink {
    const val REPO = "gabrielbr/FamilyZap" // private: only the owner can file (owner's decision, 2026-11-19)
    /** Keeps the whole address well under the ~8k characters browsers and GitHub accept. */
    const val MAX_ENCODED_BODY = 6_000

    fun build(title: String, header: String, lines: List<String>, cutNote: (kept: Int, total: Int) -> String): String {
        fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
        fun body(kept: List<String>): String {
            val note = if (kept.size < lines.size) "\n\n_" + cutNote(kept.size, lines.size) + "_" else ""
            return header + note + "\n\n```\n" + kept.joinToString("\n") + "\n```"
        }
        var keep = lines.size
        var b = body(lines)
        while (keep > 0 && enc(b).length > MAX_ENCODED_BODY) {
            keep = if (keep > 20) (keep * 0.8).toInt() else keep - 1
            b = body(lines.takeLast(keep))
        }
        return "https://github.com/$REPO/issues/new?title=${enc(title)}&body=${enc(b)}"
    }
}
