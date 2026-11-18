package app.oie.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.oie.BuildConfigInfo
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.logic.Contact
import app.oie.logic.Pin
import app.oie.ui.common.Bar
import app.oie.ui.common.Initial
import app.oie.ui.common.OIcon
import app.oie.ui.common.PinPad
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.setup.QuietList
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import kotlinx.coroutines.delay

/** The parent area closes after 2 minutes without a touch (owner, 2026-11-18). */
const val IDLE_MS = 120_000L

/** Prototype screen 09. Flow: contacts-quiet-hours.md, steps 1–2. */
@Composable
fun ParentPin(state: AppState) {
    val c = OieTheme.colors
    var entered by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Digite o seu PIN.") }
    Column(Modifier.fillMaxSize()) {
        Bar {
            QuietButton(null, { state.route = Route.KidHome }, icon = R.drawable.gg_ic_back, contentDescription = "Voltar")
            Text("Área dos pais", style = OieType.title, color = c.ink)
        }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4, Alignment.CenterVertically)) {
            Text(message, style = OieType.body, color = c.muted)
            PinPad(entered) { v ->
                entered = v
                if (v.length == Pin.LENGTH) {
                    val now = System.currentTimeMillis()
                    when {
                        state.checkPin(v, now) -> state.route = Route.Parent(0)
                        state.pinGuard.lockedFor(now) > 0 -> message = "Muitas tentativas. Espere 1 minuto."
                        else -> message = "PIN errado. Tente de novo."
                    }
                    entered = ""
                }
            }
            // v0.1: forgetting the PIN means relinking WhatsApp (flow), which the sample link can't do yet.
            TextLink("Esqueci o PIN", { state.route = Route.Setup(2) })
        }
    }
}

/** Prototype screens 07, 08, 15. */
@Composable
fun ParentArea(state: AppState, section: Int) {
    val c = OieTheme.colors
    var lastTouch by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lastTouch) { delay(IDLE_MS); state.route = Route.KidHome }
    Column(Modifier.fillMaxSize().clickable(enabled = false) {}) {
        Bar {
            Text("Área dos pais", style = OieType.title, color = c.ink)
            Box(Modifier.weight(1f))
            QuietButton("Sair", { state.route = Route.KidHome }, icon = R.drawable.gg_ic_close)
        }
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(OTokens.Size.contentMax / 2).fillMaxHeight().padding(OTokens.Spacing.space4), verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space1)) {
                listOf("Contatos" to R.drawable.gg_ic_people, "Horário de silêncio" to R.drawable.gg_ic_clock, "Conexão" to R.drawable.gg_ic_link, "Sobre" to R.drawable.gg_ic_info)
                    .forEachIndexed { i, (label, ic) ->
                        val on = i == section
                        Row(
                            Modifier.fillMaxWidth().height(OTokens.Size.rowMin).clip(RoundedCornerShape(OTokens.Radius.radiusS))
                                .background(if (on) c.selected else c.page)
                                .clickable(role = Role.Tab) { lastTouch = System.currentTimeMillis(); state.route = Route.Parent(i) }
                                .padding(horizontal = OTokens.Spacing.space4),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
                        ) { OIcon(ic, tint = if (on) c.onSelected else c.ink); Text(label, style = OieType.label, color = if (on) c.onSelected else c.ink) }
                    }
            }
            Box(Modifier.width(OTokens.Size.hairline).fillMaxHeight().background(c.line))
            Column(
                Modifier.widthIn(max = OTokens.Size.contentMax * 3 / 2).padding(horizontal = OTokens.Spacing.space12, vertical = OTokens.Spacing.space8),
                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) {
                when (section) {
                    0 -> Contacts(state) { lastTouch = System.currentTimeMillis() }
                    1 -> {
                        Text("Horário de silêncio", style = OieType.title, color = c.ink)
                        Text("Nesse horário ${state.kidName} lê e ouve, mas não manda mensagens.", style = OieType.caption, color = c.muted)
                        QuietList(state)
                        Text("Podem falar mesmo assim: " + state.allowed.filter { it.quietException }.joinToString { it.name }.ifEmpty { "ninguém" }, style = OieType.body, color = c.ink)
                    }
                    2 -> {
                        Text("Conexão", style = OieType.title, color = c.ink)
                        Text(if (state.link.connected) "Conectado ao WhatsApp de ${state.parentName}." else "O Oiê se desconectou do seu WhatsApp.", style = OieType.body, color = c.ink)
                        if (state.link.connected) TextLink("Simular queda da conexão (teste)", { state.link.pretendDropped(); state.route = Route.KidHome })
                        else PrimaryButton("Conectar de novo", { state.route = Route.Setup(2) })
                    }
                    else -> {
                        Text("Sobre", style = OieType.title, color = c.ink)
                        Text("Oiê ${BuildConfigInfo.VERSION}. Versão de teste com dados de exemplo; ainda sem conexão real com o WhatsApp.", style = OieType.body, color = c.ink)
                        Text("Fonte Nunito, SIL Open Font License 1.1.", style = OieType.caption, color = c.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun Contacts(state: AppState, touched: () -> Unit) {
    val c = OieTheme.colors
    var removed by remember { mutableStateOf<Pair<Int, Contact>?>(null) }
    LaunchedEffect(removed) { if (removed != null) { delay(5_000); removed = null } }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text("Quem ${state.kidName} pode chamar", style = OieType.title, color = c.ink)
            Text("${state.allowed.count { !it.isGroup }} pessoas, ${state.allowed.count { it.isGroup }} grupo", style = OieType.caption, color = c.muted)
        }
    }
    state.allowed.toList().forEachIndexed { i, ct ->
        Row(Modifier.fillMaxWidth().height(OTokens.Size.rowMin), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
            Initial(ct.name, OTokens.Size.avatar)
            Text(ct.name, style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
            if (ct.quietException) Text("Pode falar no silêncio", style = OieType.caption, color = c.muted)
            Switch(
                modifier = Modifier.semantics { contentDescription = "${ct.name} pode falar no horário de silêncio" },
                checked = ct.quietException,
                onCheckedChange = { touched(); state.allowed[i] = ct.copy(quietException = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = c.ink, checkedThumbColor = c.page, uncheckedTrackColor = c.line, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.muted),
            )
            // Undo instead of confirm (flow step 6).
            TextLink("Remover", { touched(); state.allowed.removeAt(i); removed = i to ct })
        }
    }
    removed?.let { (i, ct) ->
        Row(
            Modifier.padding(top = OTokens.Spacing.space4).clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.snackbar).padding(start = OTokens.Spacing.space6),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
        ) {
            Text("${ct.name} saiu da lista", style = OieType.body, color = c.page)
            Box(Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) { state.allowed.add(i.coerceAtMost(state.allowed.size), ct); removed = null }.padding(horizontal = OTokens.Spacing.space3), contentAlignment = Alignment.Center) {
                Text("Desfazer", style = OieType.label, color = c.page)
            }
        }
    }
    val addable = state.link.contacts().filter { a -> state.allowed.none { it.id == a.id } }
    if (addable.isNotEmpty()) {
        Text("Adicionar", style = OieType.label, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
        addable.forEach { ct ->
            Row(
                Modifier.fillMaxWidth().height(OTokens.Size.rowMin).clickable(role = Role.Button) { touched(); state.allowed.add(ct) },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
            ) {
                Initial(ct.name, OTokens.Size.avatar)
                Text(ct.name, style = OieType.label, color = c.ink)
                if (ct.isGroup) Text("Grupo · todos do grupo veem as mensagens", style = OieType.caption, color = c.muted)
                Box(Modifier.weight(1f))
                OIcon(R.drawable.gg_ic_plus, contentDescription = "Adicionar ${ct.name}")
            }
        }
    }
}
