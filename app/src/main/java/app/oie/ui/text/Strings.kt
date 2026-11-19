package app.oie.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Every word the app shows (01 Brand/voice.md). Written in Brazilian Portuguese first; English is
 * translated from it. A typed interface instead of strings.xml so a missing translation doesn't
 * compile and names slot into sentences safely. Words with no names in them are plain properties.
 */
interface Strings {
    // Common
    val back: String; val cont: String; val cancel: String; val undo: String; val later: String; val skip: String
    val send: String; val save: String; val add: String; val remove: String; val groupWarning: String
    fun stepOf(n: Int): String

    // Setup
    val welcomeBody: String; val start: String
    val riskTitle: String; val riskBody: String; val riskAccept: String; val riskDecline: String
    val linkTitle: String; val linkSteps: List<String>; val linkPlaceholder: String; val linkSimulate: String
    val namesTitle: String; val yourName: String; val kidNameLabel: String
    fun yourNameHelp(kid: String, parent: String): String
    fun kidNameHelp(kid: String): String
    val pinCreate: String; val pinAgain: String; val pinCreateHelp: String; val pinEasy: String; val pinMismatch: String
    fun canTalkTo(kid: String): String
    fun continueWith(n: Int): String
    val quietTitle: String
    fun quietSetupBody(kid: String): String
    val done: String
    fun handOver(kid: String): String
    fun openFor(kid: String): String

    // Kid side
    val parentButton: String
    fun hello(kid: String): String
    val whoToTalk: String
    fun nobodyYet(parent: String): String
    val askToAdd: String
    fun askedToAdd(parent: String): String
    fun kidAsked(kid: String): String
    fun newCount(n: Int): String
    val noticeTitle: String
    fun noticeBody(parent: String): String
    val understood: String
    fun linkDropped(parent: String): String
    fun quietUntil(hour: Int): String
    fun quietUntilShort(hour: Int): String
    fun firstHi(name: String): String
    val sending: String
    fun sentAt(time: String): String
    fun askReconnect(parent: String): String
    val recording: String; val releaseToSend: String
    fun writeTo(name: String): String
    fun sendingTo(name: String): String
    val photoError: String
    val recordA11y: String; val recordingA11y: String
    fun stickerA11y(word: String): String
    fun sendSticker(word: String): String
    fun onlyInWhatsApp(name: String): String
    val stickers: String; val photo: String; val camera: String; val gallery: String
    fun photoFor(name: String): String
    val photoPreviewA11y: String; val swap: String
    fun listenA11y(seconds: Int): String
    val photoButton: String; val stickerButton: String

    // Parent area
    val parentArea: String; val enterPin: String; val tooMany: String; val wrongPin: String; val forgotPin: String
    val leave: String; val sectionContacts: String; val sectionQuiet: String; val sectionConnection: String; val sectionHelp: String
    val connection: String
    fun connectedTo(parent: String): String
    val disconnected: String; val reconnect: String
    fun whoCanCall(kid: String): String
    fun peopleAndGroups(people: Int, groups: Int): String
    val canTalkInQuiet: String
    fun canTalkInQuietA11y(name: String): String
    fun leftTheList(name: String): String
    fun addA11y(name: String): String
    fun quietParentBody(kid: String): String
    fun noQuiet(kid: String): String
    fun editA11y(name: String): String
    val paused: String
    fun switchA11y(name: String, on: Boolean): String
    fun deleted(name: String): String
    fun talkAnyway(names: String): String
    val nobodyChooseInContacts: String
    val addPeriod: String; val newPeriod: String; val editPeriod: String; val name: String; val nameHelp: String; val days: String
    fun startsAt(t: String): String
    fun endsAt(t: String): String
    val nextDay: String; val deletePeriod: String
    val dayShort: List<String>
    val everyDay: String; val weekdays: String; val weekend: String; val noDays: String
    fun hours(start: String, end: String, overnight: Boolean): String
    fun clock(hour: Int, minute: Int): String
    fun pinDots(n: Int, of: Int): String
    val erase: String
    val help: String; val about: String
    fun helpItems(kid: String, parent: String): List<Pair<String, String>>
    fun aboutVersion(v: String): String
    val notWhatsApp: String; val credits: String
    val theKid: String
    // Receiving (docs/flows/kid-receives-messages.md)
    val notifChannel: String; val notifVoice: String; val notifPhoto: String; val notifSticker: String
    fun quietSummary(n: Int): String
    val deletedMessage: String; val allowNotifications: String
    val notifLinkChannel: String; val notifLinkOn: String
    val names: String
    // Family stickers (v0.10)
    fun newStickersFrom(parent: String): String
    val keepSticker: String; val sendFamilySticker: String; val familyStickers: String
    val micError: String
    // The real WhatsApp on the kid's side (v0.8)
    val linkConnect: String; val linkWaiting: String
    val notSent: String; val tryAgain: String; val unlinkReal: String
    fun pickRealContacts(kid: String): String

    // Linking, contacts and the ban (v0.12)
    val linkQrA11y: String; val linkUnavailable: String
    fun bannedFor(hours: Int): String
    val search: String; val searchHelp: String
    val addByNumber: String; val numberLabel: String; val numberHelp: String; val contactNameLabel: String
    val notOnWhatsApp: String; val lookUpFailed: String; val looking: String
    fun unlinkConfirm(kid: String): String
    val onlyOutsideQuiet: String
    // Contacts after linking (v0.17)
    fun contactsSyncing(n: Int): String
    val contactsSlow: String; val contactsEmpty: String; val chosen: String
    // Problem report (docs/flows/problem-report.md, v0.14)
    val problems: String; val noProblems: String; val copyReport: String; val copied: String; val clearReport: String; val reportPrivacy: String
    fun closedByItself(day: Int, month: Int, time: String): String
    val crashPointer: String; val openReport: String
    val sendToGitHub: String
    fun issueTitle(version: String, day: Int, month: Int, crashed: Boolean): String
    fun reportCut(kept: Int, total: Int): String

    // Self-update (Gabriel Design System docs/self-update.md, "Words")
    fun updateAvailable(v: String): String
    fun updateButton(size: String): String
    val updateLater: String
    fun downloading(pct: Int): String
    val checkForUpdates: String; val checking: String
    fun latest(v: String): String
    fun couldNotCheck(reason: String): String
    val downloadFailed: String; val checksumFailed: String; val signatureFailed: String; val allowInstalls: String
    fun releaseOnly(v: String, id: String): String
    val version: String
    fun versionLine(v: String, code: Long): String
    val decimalComma: Boolean
}

object Pt : Strings {
    override val back = "Voltar"; override val cont = "Continuar"; override val cancel = "Cancelar"; override val undo = "Desfazer"
    override val later = "Depois"; override val skip = "Pular"; override val send = "Enviar"; override val save = "Salvar"
    override val add = "Adicionar"; override val remove = "Remover"; override val groupWarning = "Grupo · todos do grupo veem as mensagens"
    override fun stepOf(n: Int) = "Passo $n de 6"
    override val welcomeBody = "O Oiê é para seu filho ou filha conversar com a família pelo seu WhatsApp, só com quem você escolher."
    override val start = "Começar"
    override val riskTitle = "Antes de conectar"
    override val riskBody = "O Oiê usa o seu WhatsApp de um jeito que o WhatsApp não oferece oficialmente. Ele pode limitar ou bloquear o seu número. Se isso acontecer, você perde o acesso até resolver com o WhatsApp."
    override val riskAccept = "Entendi, conectar"; override val riskDecline = "Agora não"
    override val linkTitle = "Conecte o seu WhatsApp"
    override val linkSteps = listOf("No seu celular, abra o WhatsApp.", "Toque em Configurações e depois em Dispositivos conectados.", "Toque em Conectar um dispositivo.", "Aponte o celular para o código.")
    override val linkPlaceholder = "Versão de teste: o código do WhatsApp entra aqui na próxima versão."
    override val linkSimulate = "Simular conexão (teste)"
    override val namesTitle = "Como vocês se chamam?"; override val yourName = "Seu nome"; override val kidNameLabel = "Como a família chama seu filho ou filha?"
    override fun yourNameHelp(kid: String, parent: String) = "Aparece para $kid como \"WhatsApp de $parent\"."
    override fun kidNameHelp(kid: String) = "Vai junto das mensagens: \"$kid: Oi, vó\"."
    override val pinCreate = "Crie um PIN de 6 números"; override val pinAgain = "Digite o PIN de novo"
    override val pinCreateHelp = "Ele abre a área dos pais neste tablet. Não use o mesmo PIN do tablet."
    override val pinEasy = "Esse PIN é fácil de adivinhar. Escolha outro."; override val pinMismatch = "Os PINs não são iguais. Digite de novo."
    override fun canTalkTo(kid: String) = "Com quem $kid pode falar?"
    override fun continueWith(n: Int) = if (n == 0) cont else "Continuar · $n ${if (n == 1) "escolhido" else "escolhidos"}"
    override val quietTitle = "Horário de silêncio"
    override fun quietSetupBody(kid: String) = "Nesse horário $kid lê e ouve, mas não manda mensagens. Dá para mudar depois."
    override val done = "Pronto."
    override fun handOver(kid: String) = "Entregue o tablet para $kid."
    override fun openFor(kid: String) = "Abrir para $kid"
    override val parentButton = "Pais"
    override fun hello(kid: String) = "Oiê, $kid"
    override val whoToTalk = "Com quem você quer falar?"
    override fun nobodyYet(parent: String) = "Ainda não tem ninguém aqui. Peça para $parent escolher quem você pode chamar."
    override val askToAdd = "Pedir para adicionar alguém"
    override fun askedToAdd(parent: String) = "Pedido enviado. $parent vai ver na área dos pais."
    override fun kidAsked(kid: String) = "$kid pediu para adicionar alguém."
    override fun newCount(n: Int) = "$n ${if (n == 1) "nova" else "novas"}"
    override val noticeTitle = "Antes de começar"
    override fun noticeBody(parent: String) = "Suas mensagens saem do WhatsApp de $parent, então $parent pode ver o que você manda."
    override val understood = "Entendi"
    override fun linkDropped(parent: String) = "O Oiê se desconectou. Peça para $parent conectar de novo."
    override fun quietUntil(hour: Int) = "Horário de silêncio até ${hour}h. Dá para ler e ouvir, mas não para mandar."
    override fun quietUntilShort(hour: Int) = "Horário de silêncio até ${hour}h."
    override fun firstHi(name: String) = "Mande o primeiro oi para $name."
    override val sending = "Enviando…"
    override fun sentAt(time: String) = "Enviada · $time"
    override fun askReconnect(parent: String) = "Peça para $parent conectar de novo."
    override val recording = "Gravando…"; override val releaseToSend = "Solte para enviar"
    override fun writeTo(name: String) = "Escreva para $name"
    override fun sendingTo(name: String) = "Enviando para $name"
    override val photoError = "Não deu para abrir essa foto. Tente outra."
    override val recordA11y = "Gravar áudio"; override val recordingA11y = "Gravando. Toque para enviar"
    override fun stickerA11y(word: String) = "Figurinha: $word"
    override fun sendSticker(word: String) = "Mandar figurinha $word"
    override fun onlyInWhatsApp(name: String) = "$name mandou algo que só abre no WhatsApp."
    override val stickers = "Figurinhas"; override val photo = "Foto"; override val camera = "Câmera"; override val gallery = "Galeria"
    override fun photoFor(name: String) = "Foto para $name"
    override val photoPreviewA11y = "A foto que você vai mandar"; override val swap = "Trocar"
    override fun listenA11y(seconds: Int) = "Ouvir áudio de $seconds segundos"
    override val photoButton = "Foto"; override val stickerButton = "Figurinhas"
    override val parentArea = "Área dos pais"; override val enterPin = "Digite o seu PIN."
    override val tooMany = "Muitas tentativas. Espere 1 minuto."; override val wrongPin = "PIN errado. Tente de novo."; override val forgotPin = "Esqueci o PIN"
    override val leave = "Sair"; override val sectionContacts = "Contatos"; override val sectionQuiet = "Horário de silêncio"
    override val sectionConnection = "Conexão"; override val sectionHelp = "Ajustes e ajuda"; override val connection = "Conexão"
    override fun connectedTo(parent: String) = "Conectado ao WhatsApp de $parent."
    override val disconnected = "O Oiê se desconectou do seu WhatsApp."
    override val reconnect = "Conectar de novo"
    override fun whoCanCall(kid: String) = "Quem $kid pode chamar"
    override fun peopleAndGroups(people: Int, groups: Int) =
        "$people ${if (people == 1) "pessoa" else "pessoas"}, $groups ${if (groups == 1) "grupo" else "grupos"}"
    override val canTalkInQuiet = "Pode falar no silêncio"
    override fun canTalkInQuietA11y(name: String) = "$name pode falar no horário de silêncio"
    override fun leftTheList(name: String) = "$name saiu da lista"
    override fun addA11y(name: String) = "Adicionar $name"
    override fun quietParentBody(kid: String) = "Nesse horário $kid lê e ouve, mas não manda mensagens."
    override fun noQuiet(kid: String) = "Sem horário de silêncio. $kid pode mandar mensagens a qualquer hora."
    override fun editA11y(name: String) = "Editar $name"
    override val paused = "Pausado"
    override fun switchA11y(name: String, on: Boolean) = "$name ${if (on) "ligado" else "desligado"}"
    override fun deleted(name: String) = "$name apagado"
    override fun talkAnyway(names: String) = "Podem falar mesmo assim: $names"
    override val nobodyChooseInContacts = "ninguém. Escolha em Contatos."
    override val addPeriod = "Adicionar período"; override val newPeriod = "Novo período"; override val editPeriod = "Editar período"
    override val name = "Nome"; override val nameHelp = "Por exemplo: Escola, Dormir, Natação."; override val days = "Dias"
    override fun startsAt(t: String) = "Começa $t"
    override fun endsAt(t: String) = "Termina $t"
    override val nextDay = "até o dia seguinte"; override val deletePeriod = "Apagar período"
    override val dayShort = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    override val everyDay = "Todos os dias"; override val weekdays = "Seg a sex"; override val weekend = "Sáb e dom"; override val noDays = "Nenhum dia"
    override fun hours(start: String, end: String, overnight: Boolean) = "$start às $end" + if (overnight) " do dia seguinte" else ""
    override fun clock(hour: Int, minute: Int) = if (minute == 0) "${hour}h" else "%dh%02d".format(hour, minute)
    override fun pinDots(n: Int, of: Int) = "$n de $of números"
    override val erase = "Apagar"
    override val help = "Ajuda"; override val about = "Sobre"
    override fun helpItems(kid: String, parent: String) = listOf(
        "O que pode acontecer com o meu WhatsApp?" to riskBody,
        "$kid vê as minhas conversas?" to "Não. O tablet mostra só o que foi mandado para $kid: respostas às mensagens de $kid, mensagens que começam com o nome de $kid, e o que chega até 24 horas depois da última mensagem de $kid. As suas outras conversas continuam só no seu WhatsApp.",
        "$kid sabe que eu vejo as mensagens?" to "Sim. Na primeira vez, o Oiê diz a $kid: \"${noticeBody(parent)}\"",
        "Quem pode falar com $kid?" to "Só quem está em Contatos. Mensagens de qualquer outra pessoa não aparecem no tablet. Num grupo, todos do grupo veem o que $kid manda.",
        "Como funciona o horário de silêncio?" to "Durante um período, $kid lê e ouve, mas não manda mensagens. Quem tem \"Pode falar no silêncio\" em Contatos continua liberado. Dá para pausar um período sem apagar, por exemplo nas férias.",
        "O Oiê se desconectou. E agora?" to "Abra Conexão e toque em Conectar de novo. As mensagens que $kid mandou enquanto isso ficam guardadas e saem depois.",
        "Como mando figurinhas para $kid?" to "No seu WhatsApp, mande a figurinha para você mesmo, na conversa com o seu próprio nome. Ela aparece para $kid em Figurinhas, em Novas de $parent, e $kid escolhe se guarda. Só as figurinhas dessa conversa chegam ao Oiê; o resto dela fica só com você. Funciona com o WhatsApp de verdade ligado.",
        "Esqueci o PIN" to "Na tela do PIN, toque em Esqueci o PIN e conecte o mesmo WhatsApp de novo. Isso prova que é você. Depois crie um PIN novo.",
        "Onde ficam as mensagens e as fotos?" to "Só neste tablet, guardadas de forma protegida. Nada vai para um servidor nosso. O Oiê não tem anúncios e não acompanha o que $kid faz.",
    )
    override fun aboutVersion(v: String) = "Oiê $v. Versão de teste: usa dados de exemplo e ainda não se conecta de verdade ao WhatsApp."
    override val notWhatsApp = "O Oiê não é feito nem aprovado pelo WhatsApp ou pela Meta."
    override val credits = "Fonte Nunito, SIL Open Font License 1.1. Ícones do Gabriel Design System. Conexão com o WhatsApp feita com whatsmeow, Mozilla Public License 2.0, código em github.com/tulir/whatsmeow. QR com ZXing, Apache License 2.0."
    override val theKid = "a criança"
    override val notifChannel = "Mensagens da família"; override val notifVoice = "Mandou um áudio"
    override val notifPhoto = "Mandou uma foto"; override val notifSticker = "Mandou uma figurinha"
    override fun quietSummary(n: Int) = if (n == 1) "1 mensagem enquanto estava em silêncio" else "$n mensagens enquanto estava em silêncio"
    override val deletedMessage = "Mensagem apagada"
    override val allowNotifications = "Para avisar quando a família mandar mensagem e para gravar áudios, o Oiê vai pedir permissão para notificações e para o microfone."
    override fun updateAvailable(v: String) = "Atualização disponível · $v"
    override fun updateButton(size: String) = "Atualizar · $size"
    override val updateLater = "Mais tarde"
    override fun downloading(pct: Int) = "Baixando… $pct%"
    override val checkForUpdates = "Procurar atualizações"; override val checking = "Procurando…"
    override fun latest(v: String) = "Você está na versão mais recente ($v)."
    override fun couldNotCheck(reason: String) = "Não foi possível procurar atualizações ($reason). Tente de novo."
    override val downloadFailed = "O download falhou. Tente de novo."
    override val checksumFailed = "O download não confere com a soma de verificação da versão, então não foi instalado."
    override val signatureFailed = "O download não foi assinado pelo Oiê, então não foi instalado."
    override val allowInstalls = "Permita instalações deste app e toque em Atualizar de novo."
    override fun releaseOnly(v: String, id: String) = "A atualização automática só funciona na versão de lançamento ($v / $id)."
    override val version = "Versão"
    override fun versionLine(v: String, code: Long) = "Oiê v$v ($code)"
    override val decimalComma = true
    override val notifLinkChannel = "Conexão com o WhatsApp"; override val notifLinkOn = "O Oiê está conectado."
    override val linkConnect = "Conectar o WhatsApp"
    override val linkWaiting = "Esperando o WhatsApp do seu celular."
    override val notSent = "Não foi"; override val tryAgain = "Tentar de novo"; override val unlinkReal = "Desconectar o WhatsApp"
    override fun pickRealContacts(kid: String) = "Escolha nos seus contatos quem $kid pode chamar."
    override val names = "Nomes"
    override fun newStickersFrom(parent: String) = "Novas de $parent"
    override val keepSticker = "Guardar"; override val sendFamilySticker = "Mandar figurinha da família"; override val familyStickers = "Da família"
    override val micError = "Não deu para gravar. Tente de novo."
    override val linkQrA11y = "Código para conectar o WhatsApp"; override val linkUnavailable = "Este tablet não consegue usar a conexão com o WhatsApp."
    override fun bannedFor(hours: Int) = "O WhatsApp bloqueou este número por cerca de $hours h. O Oiê parou e não vai tentar de novo sozinho."
    override val search = "Buscar"; override val searchHelp = "Os seus contatos do WhatsApp chegam aos poucos depois de conectar."
    override val addByNumber = "Adicionar pelo número"; override val numberLabel = "Número com DDD"; override val numberHelp = "Com o código do país: 55 11 99999-0000."
    override val contactNameLabel = "Nome"; override val notOnWhatsApp = "Esse número não está no WhatsApp."
    override val lookUpFailed = "Não deu para procurar agora. Tente de novo."; override val looking = "Procurando…"
    override fun unlinkConfirm(kid: String) = "$kid para de receber e mandar mensagens até você conectar de novo com o código."
    override val onlyOutsideQuiet = "Só fora do silêncio"
    override fun contactsSyncing(n: Int) = if (n == 0) "Trazendo seus contatos do WhatsApp…" else "Trazendo seus contatos do WhatsApp… $n até agora"
    override val contactsSlow = "Seus contatos estão demorando para chegar. Adicione alguém pelo número, ou escolha depois em Contatos."
    override val contactsEmpty = "Não achamos contatos no seu WhatsApp. Adicione alguém pelo número."
    override val chosen = "Escolhido"
    override val problems = "Problemas"; override val noProblems = "Nenhum problema guardado."
    override val copyReport = "Copiar relatório"; override val copied = "Copiado"; override val clearReport = "Apagar relatório"
    override val reportPrivacy = "O relatório fica só neste tablet e não tem mensagens nem nomes. Copie, ou abra no GitHub e envie você mesmo."
    override fun closedByItself(day: Int, month: Int, time: String) = "O Oiê fechou sozinho em %02d/%02d às %s.".format(day, month, time)
    override val crashPointer = "O Oiê fechou sozinho há pouco. Há um relatório para copiar."; override val openReport = "Ver relatório"
    override val sendToGitHub = "Enviar para o GitHub"
    override fun issueTitle(version: String, day: Int, month: Int, crashed: Boolean) = "Relatório do tablet · Oiê $version · %02d/%02d".format(day, month) + if (crashed) " · fechou sozinho" else ""
    override fun reportCut(kept: Int, total: Int) = "Só as últimas $kept de $total linhas cabem aqui. O relatório inteiro pode ser copiado no tablet."
}

/** English, translated from the Portuguese (01 Brand/voice.md: a native reader checks it before release). */
object En : Strings {
    override val back = "Back"; override val cont = "Continue"; override val cancel = "Cancel"; override val undo = "Undo"
    override val later = "Later"; override val skip = "Skip"; override val send = "Send"; override val save = "Save"
    override val add = "Add"; override val remove = "Remove"; override val groupWarning = "Group · everyone in it sees the messages"
    override fun stepOf(n: Int) = "Step $n of 6"
    override val welcomeBody = "Oiê lets your child talk to the family through your WhatsApp, only with the people you choose."
    override val start = "Get started"
    override val riskTitle = "Before you link"
    override val riskBody = "Oiê uses your WhatsApp in a way WhatsApp doesn't officially offer. WhatsApp may limit or block your number. If it does, you lose access until you sort it out with WhatsApp."
    override val riskAccept = "I understand, link"; override val riskDecline = "Not now"
    override val linkTitle = "Link your WhatsApp"
    override val linkSteps = listOf("On your phone, open WhatsApp.", "Tap Settings, then Linked devices.", "Tap Link a device.", "Point your phone at the code.")
    override val linkPlaceholder = "Test version: the WhatsApp code goes here in the next version."
    override val linkSimulate = "Pretend to link (test)"
    override val namesTitle = "What are your names?"; override val yourName = "Your name"; override val kidNameLabel = "What does the family call your child?"
    override fun yourNameHelp(kid: String, parent: String) = "$kid sees it as \"$parent's WhatsApp\"."
    override fun kidNameHelp(kid: String) = "It goes with the messages: \"$kid: Hi, Grandma\"."
    override val pinCreate = "Create a 6-digit PIN"; override val pinAgain = "Enter the PIN again"
    override val pinCreateHelp = "It opens the parent area on this tablet. Don't use the tablet's own PIN."
    override val pinEasy = "That PIN is easy to guess. Choose another."; override val pinMismatch = "The PINs don't match. Enter it again."
    override fun canTalkTo(kid: String) = "Who can $kid talk to?"
    override fun continueWith(n: Int) = if (n == 0) cont else "Continue · $n chosen"
    override val quietTitle = "Quiet hours"
    override fun quietSetupBody(kid: String) = "During these hours $kid can read and listen, but not send. You can change this later."
    override val done = "All set."
    override fun handOver(kid: String) = "Hand the tablet to $kid."
    override fun openFor(kid: String) = "Open for $kid"
    override val parentButton = "Parents"
    override fun hello(kid: String) = "Oiê, $kid"
    override val whoToTalk = "Who do you want to talk to?"
    override fun nobodyYet(parent: String) = "Nobody here yet. Ask $parent to choose who you can call."
    override val askToAdd = "Ask to add someone"
    override fun askedToAdd(parent: String) = "Request sent. $parent will see it in the parent area."
    override fun kidAsked(kid: String) = "$kid asked you to add someone."
    override fun newCount(n: Int) = "$n new"
    override val noticeTitle = "Before you start"
    override fun noticeBody(parent: String) = "Your messages go out from $parent's WhatsApp, so $parent can see what you send."
    override val understood = "Got it"
    override fun linkDropped(parent: String) = "Oiê got disconnected. Ask $parent to link it again."
    override fun quietUntil(hour: Int) = "Quiet hours until ${clock(hour, 0)}. You can read and listen, but not send."
    override fun quietUntilShort(hour: Int) = "Quiet hours until ${clock(hour, 0)}."
    override fun firstHi(name: String) = "Say the first hi to $name."
    override val sending = "Sending…"
    override fun sentAt(time: String) = "Sent · $time"
    override fun askReconnect(parent: String) = "Ask $parent to link it again."
    override val recording = "Recording…"; override val releaseToSend = "Let go to send"
    override fun writeTo(name: String) = "Write to $name"
    override fun sendingTo(name: String) = "Sending to $name"
    override val photoError = "Couldn't open that photo. Try another one."
    override val recordA11y = "Record a voice message"; override val recordingA11y = "Recording. Tap to send"
    override fun stickerA11y(word: String) = "Sticker: $word"
    override fun sendSticker(word: String) = "Send sticker $word"
    override fun onlyInWhatsApp(name: String) = "$name sent something that only opens in WhatsApp."
    override val stickers = "Stickers"; override val photo = "Photo"; override val camera = "Camera"; override val gallery = "Gallery"
    override fun photoFor(name: String) = "Photo for $name"
    override val photoPreviewA11y = "The photo you're about to send"; override val swap = "Change"
    override fun listenA11y(seconds: Int) = "Play a $seconds-second voice message"
    override val photoButton = "Photo"; override val stickerButton = "Stickers"
    override val parentArea = "Parent area"; override val enterPin = "Enter your PIN."
    override val tooMany = "Too many tries. Wait 1 minute."; override val wrongPin = "Wrong PIN. Try again."; override val forgotPin = "I forgot the PIN"
    override val leave = "Leave"; override val sectionContacts = "Contacts"; override val sectionQuiet = "Quiet hours"
    override val sectionConnection = "Connection"; override val sectionHelp = "Settings and help"; override val connection = "Connection"
    override fun connectedTo(parent: String) = "Linked to $parent's WhatsApp."
    override val disconnected = "Oiê got disconnected from your WhatsApp."
    override val reconnect = "Link again"
    override fun whoCanCall(kid: String) = "Who $kid can call"
    override fun peopleAndGroups(people: Int, groups: Int) =
        "$people ${if (people == 1) "person" else "people"}, $groups ${if (groups == 1) "group" else "groups"}"
    override val canTalkInQuiet = "Can talk in quiet hours"
    override fun canTalkInQuietA11y(name: String) = "$name can talk in quiet hours"
    override fun leftTheList(name: String) = "$name left the list"
    override fun addA11y(name: String) = "Add $name"
    override fun quietParentBody(kid: String) = "During these hours $kid can read and listen, but not send."
    override fun noQuiet(kid: String) = "No quiet hours. $kid can send messages at any time."
    override fun editA11y(name: String) = "Edit $name"
    override val paused = "Paused"
    override fun switchA11y(name: String, on: Boolean) = "$name ${if (on) "on" else "off"}"
    override fun deleted(name: String) = "$name deleted"
    override fun talkAnyway(names: String) = "Can talk anyway: $names"
    override val nobodyChooseInContacts = "nobody. Choose in Contacts."
    override val addPeriod = "Add a period"; override val newPeriod = "New period"; override val editPeriod = "Edit period"
    override val name = "Name"; override val nameHelp = "For example: School, Bedtime, Swimming."; override val days = "Days"
    override fun startsAt(t: String) = "Starts $t"
    override fun endsAt(t: String) = "Ends $t"
    override val nextDay = "until the next day"; override val deletePeriod = "Delete period"
    override val dayShort = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    override val everyDay = "Every day"; override val weekdays = "Mon to Fri"; override val weekend = "Sat and Sun"; override val noDays = "No days"
    override fun hours(start: String, end: String, overnight: Boolean) = "$start to $end" + if (overnight) " the next day" else ""
    override fun clock(hour: Int, minute: Int): String {
        val h = if (hour % 12 == 0) 12 else hour % 12
        val ap = if (hour < 12) "am" else "pm"
        return if (minute == 0) "$h $ap" else "%d:%02d %s".format(h, minute, ap)
    }
    override fun pinDots(n: Int, of: Int) = "$n of $of digits"
    override val erase = "Delete"
    override val help = "Help"; override val about = "About"
    override fun helpItems(kid: String, parent: String) = listOf(
        "What could happen to my WhatsApp?" to riskBody,
        "Can $kid see my chats?" to "No. The tablet only shows what was sent to $kid: replies to $kid's messages, messages that start with $kid's name, and what arrives up to 24 hours after $kid's last message. Your other chats stay only in your WhatsApp.",
        "Does $kid know I can see the messages?" to "Yes. The first time, Oiê tells $kid: \"${noticeBody(parent)}\"",
        "Who can talk to $kid?" to "Only the people in Contacts. Messages from anyone else don't appear on the tablet. In a group, everyone in it sees what $kid sends.",
        "How do quiet hours work?" to "During a period, $kid can read and listen, but not send. Anyone with \"Can talk in quiet hours\" in Contacts still gets through. You can pause a period without deleting it, for example on holiday.",
        "Oiê got disconnected. What now?" to "Open Connection and tap Link again. Messages $kid sent in the meantime are kept and go out afterwards.",
        "How do I send stickers to $kid?" to "In your WhatsApp, send the sticker to yourself, in the chat with your own name. It shows for $kid under Stickers, in New from $parent, and $kid chooses whether to keep it. Only stickers from that chat reach Oiê; the rest of it stays with you. Works once the real WhatsApp is on.",
        "I forgot the PIN" to "On the PIN screen, tap I forgot the PIN and link the same WhatsApp again. That proves it's you. Then create a new PIN.",
        "Where are the messages and photos kept?" to "Only on this tablet, stored securely. Nothing goes to a server of ours. Oiê has no ads and doesn't track what $kid does.",
    )
    override fun aboutVersion(v: String) = "Oiê $v. Test version: it uses example data and doesn't really link to WhatsApp yet."
    override val notWhatsApp = "Oiê isn't made or approved by WhatsApp or Meta."
    override val credits = "Nunito font, SIL Open Font License 1.1. Icons from the Gabriel Design System. WhatsApp link built with whatsmeow, Mozilla Public License 2.0, source at github.com/tulir/whatsmeow. QR codes with ZXing, Apache License 2.0."
    override val theKid = "your child"
    override val notifChannel = "Messages from family"; override val notifVoice = "Sent a voice message"
    override val notifPhoto = "Sent a photo"; override val notifSticker = "Sent a sticker"
    override fun quietSummary(n: Int) = if (n == 1) "1 message during quiet hours" else "$n messages during quiet hours"
    override val deletedMessage = "Message deleted"
    override val allowNotifications = "To tell you when family sends a message and to record voice messages, Oiê will ask for notifications and the microphone."
    override fun updateAvailable(v: String) = "Update available · $v"
    override fun updateButton(size: String) = "Update · $size"
    override val updateLater = "Later"
    override fun downloading(pct: Int) = "Downloading… $pct%"
    override val checkForUpdates = "Check for updates"; override val checking = "Checking…"
    override fun latest(v: String) = "You're on the latest version ($v)."
    override fun couldNotCheck(reason: String) = "Couldn't check for updates ($reason). Try again."
    override val downloadFailed = "Download failed. Try again."
    override val checksumFailed = "The download didn't match the release's checksum, so it wasn't installed."
    override val signatureFailed = "The download wasn't signed by Oiê, so it wasn't installed."
    override val allowInstalls = "Allow installs from this app, then tap Update again."
    override fun releaseOnly(v: String, id: String) = "Self-update works in the release build only ($v / $id)."
    override val version = "Version"
    override fun versionLine(v: String, code: Long) = "Oiê v$v ($code)"
    override val decimalComma = false
    override val notifLinkChannel = "WhatsApp link"; override val notifLinkOn = "Oiê is connected."
    override val linkConnect = "Link WhatsApp"
    override val linkWaiting = "Waiting for the WhatsApp on your phone."
    override val notSent = "Didn't go"; override val tryAgain = "Try again"; override val unlinkReal = "Unlink WhatsApp"
    override fun pickRealContacts(kid: String) = "Choose from your contacts who $kid can talk to."
    override val names = "Names"
    override fun newStickersFrom(parent: String) = "New from $parent"
    override val keepSticker = "Keep"; override val sendFamilySticker = "Send a family sticker"; override val familyStickers = "From family"
    override val micError = "Couldn't record. Try again."
    override val linkQrA11y = "Code to link WhatsApp"; override val linkUnavailable = "This tablet can't use the WhatsApp link."
    override fun bannedFor(hours: Int) = "WhatsApp blocked this number for about $hours h. Oiê stopped and won't try again on its own."
    override val search = "Search"; override val searchHelp = "Your WhatsApp contacts arrive bit by bit after linking."
    override val addByNumber = "Add by number"; override val numberLabel = "Phone number"; override val numberHelp = "With the country code: 55 11 99999-0000."
    override val contactNameLabel = "Name"; override val notOnWhatsApp = "That number isn't on WhatsApp."
    override val lookUpFailed = "Couldn't look it up now. Try again."; override val looking = "Looking…"
    override fun unlinkConfirm(kid: String) = "$kid stops sending and receiving messages until you link again with the code."
    override val onlyOutsideQuiet = "Only outside quiet hours"
    override fun contactsSyncing(n: Int) = if (n == 0) "Bringing in your WhatsApp contacts…" else "Bringing in your WhatsApp contacts… $n so far"
    override val contactsSlow = "Your contacts are taking a while. Add someone by number, or choose later in Contacts."
    override val contactsEmpty = "We didn't find contacts in your WhatsApp. Add someone by number."
    override val chosen = "Chosen"
    override val problems = "Problems"; override val noProblems = "No problems saved."
    override val copyReport = "Copy report"; override val copied = "Copied"; override val clearReport = "Delete report"
    override val reportPrivacy = "The report stays on this tablet and holds no messages or names. Copy it, or open it in GitHub and send it yourself."
    override fun closedByItself(day: Int, month: Int, time: String) = "Oiê closed by itself on %02d/%02d at %s.".format(day, month, time)
    override val crashPointer = "Oiê closed by itself recently. There's a report to copy."; override val openReport = "See report"
    override val sendToGitHub = "Send to GitHub"
    override fun issueTitle(version: String, day: Int, month: Int, crashed: Boolean) = "Tablet report · Oiê $version · %02d/%02d".format(day, month) + if (crashed) " · closed by itself" else ""
    override fun reportCut(kept: Int, total: Int) = "Only the last $kept of $total lines fit here. The whole report can be copied on the tablet."
}

val LocalStrings = staticCompositionLocalOf<Strings> { Pt }

/** Portuguese unless the device (or the per-app language setting) asks for English. */
@Composable
@ReadOnlyComposable
fun stringsFor(): Strings = if (LocalConfiguration.current.locales[0].language == "en") En else Pt

/** The current language's words. In a lambda, read it into a val first: `val tx = str`. */
val str: Strings
    @Composable @ReadOnlyComposable get() = LocalStrings.current
