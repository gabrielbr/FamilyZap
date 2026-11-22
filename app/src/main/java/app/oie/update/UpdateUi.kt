package app.oie.update

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.oie.update.Updater.State
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** The "Update available" sheet. While downloading, Back and taps outside do nothing; Cancel stops it. */
@Composable
fun UpdateSheet(updater: Updater) {
    val c = OieTheme.colors
    val tx = str
    val st = updater.state
    val offer = when (st) {
        is State.Available -> st.offer.takeIf { it.version != updater.dismissed }
        is State.Downloading -> st.offer
        is State.Failed -> st.offer
        else -> null
    } ?: return
    Box(Modifier.fillMaxSize().background(c.scrim).clickable(enabled = st !is State.Downloading) { if (st is State.Available) updater.later(offer) }, contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.width(OTokens.Size.contentMax).clip(androidx.compose.material3.MaterialTheme.shapes.extraLarge.copy(bottomStart = androidx.compose.foundation.shape.ZeroCornerSize, bottomEnd = androidx.compose.foundation.shape.ZeroCornerSize))
                .background(c.surface).clickable(enabled = false) {}.padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text(tx.updateAvailable(offer.version), style = OieType.title, color = c.ink)
            if (offer.notes.isNotBlank()) Text(
                offer.notes, style = OieType.body, color = c.ink,
                modifier = Modifier.heightIn(max = OTokens.Size.contentMax / 2).verticalScroll(rememberScrollState()),
            )
            when (st) {
                is State.Downloading -> {
                    Text(tx.downloading(st.percent), style = OieType.label, color = c.ink)
                    Box(Modifier.fillMaxWidth().height(OTokens.Size.frame).background(c.line)) {
                        Box(Modifier.fillMaxWidth(st.percent / 100f).height(OTokens.Size.frame).background(c.mint))
                    }
                    QuietButton(tx.cancel, { updater.cancel() }, Modifier.height(OTokens.Size.button))
                }
                else -> {
                    if (st is State.Failed) Text(
                        when (st.problem) {
                            UpdateRules.Problem.CHECKSUM, UpdateRules.Problem.NO_SUM -> tx.checksumFailed
                            UpdateRules.Problem.SIGNATURE -> tx.signatureFailed
                            else -> tx.downloadFailed
                        },
                        style = OieType.body, color = c.coralText,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                        PrimaryButton(tx.updateButton(UpdateRules.size(offer.apk.size, tx.decimalComma)), { updater.update(offer) })
                        QuietButton(tx.updateLater, { updater.later(offer) }, Modifier.height(OTokens.Size.button))
                    }
                }
            }
        }
    }
}

/** The About rows: version, then "Check for updates" (or why a debug copy can't). */
@Composable
fun UpdateAboutRows(updater: Updater) {
    val c = OieTheme.colors
    val tx = str
    Text(tx.versionLine(updater.versionName, updater.versionCode), style = OieType.body, color = c.ink)
    if (!updater.isReleaseBuild) {
        Text(tx.releaseOnly, style = OieType.caption, color = c.muted)
        return
    }
    QuietButton(tx.checkForUpdates, { updater.check(manual = true) })
    when (val st = updater.state) {
        State.Checking -> Text(tx.checking, style = OieType.body, color = c.muted)
        is State.UpToDate -> Text(tx.latest(st.version), style = OieType.body, color = c.ink)
        is State.CouldNotCheck -> Text(tx.couldNotCheck(st.reason), style = OieType.body, color = c.coralText)
        State.NeedsInstallPermission -> Text(tx.allowInstalls, style = OieType.body, color = c.ink)
        else -> {}
    }
}
