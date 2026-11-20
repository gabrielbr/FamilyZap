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
    fun connectingTo(parent: String): String
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
    // v0.27: videos and GIFs.
    val notifVideo: String; val notifGif: String; val video: String; val gifLabel: String
    fun playVideoA11y(seconds: Int): String
    fun videoTooBig(parent: String): String
    val pause: String; val play: String
    // v0.28: sending videos and GIFs.
    val photoOrVideo: String; val takePhoto: String; val recordVideo: String
    val writeShort: String
    // v0.30: the line to the parent who owns the WhatsApp (decision 0007).
    fun parentLineTitle(parent: String): String
    fun parentLineCreate(kid: String): String
    fun parentLineBody(kid: String): String
    val parentLineNoNotify: String; val parentLineCreating: String; val parentLineNotNow: String
    fun parentLineFallback(kid: String): String
    fun parentLineInGroup(kid: String): String
    val parentLineInOwnChat: String
    fun videoFor(to: String): String
    fun gifFor(to: String): String
    val preparingVideo: String; val videoTooLong: String; val videoTooBigToSend: String; val videoError: String
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
    // v0.19: a name for the kid, and the number for the parent.
    fun renameHint(kid: String): String
    val changeName: String; val noNumber: String; val groupShort: String
    fun changeNameA11y(name: String): String
    fun nameKidSees(kid: String): String
    fun onWhatsApp(name: String): String
    fun renameHelp(waName: String): String
    fun removeNamed(name: String): String
    // v0.21: nicknames, paused chats, and the parent's commands from WhatsApp (docs/flows/parent-commands.md).
    fun nicknamesLabel(kid: String): String
    fun nicknamesHelp(kid: String): String
    // v0.25: Oiê in a bubble (docs/flows/bubble.md).
    val bubbleTitle: String; val bubbleBody: String; val bubbleSwitch: String; val bubbleOnLine: String
    val bubbleNeedsPermission: String; val bubbleSamsung: String; val bubbleNeedsNotifications: String
    val bubbleUnsupported: String; val bubbleAllow: String; val bubbleChannel: String; val bubbleText: String
    // v0.22: the emoji before the bold name.
    val emojiLabel: String; val emojiHelp: String; val looksLikeOnWhatsApp: String; val sampleMessage: String
    val pausedLine: String; val resumeChat: String
    fun cmdHelp(kid: String, cmd: String, here: String?): String
    fun cmdHereReceives(chat: String, kid: String): String
    fun cmdHerePaused(chat: String, kid: String): String
    fun cmdHereNotIn(kid: String): String
    fun cmdPaused(chat: String, kid: String, cmd: String): String
    fun cmdResumed(chat: String, kid: String): String
    fun cmdAdded(chat: String, kid: String, group: Boolean): String
    fun cmdAlready(chat: String, kid: String): String
    fun cmdRemoved(chat: String, kid: String): String
    fun cmdNotIn(kid: String, cmd: String): String
    fun cmdOwnChat(kid: String, cmd: String): String
    fun cmdUnknown(word: String, kid: String, cmd: String): String
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
    val releaseOnly: String
    val version: String
    fun versionLine(v: String, code: Long): String
    val decimalComma: Boolean

    // v0.33: family features (docs/flows/family-features.md)
    fun receiptLine(r: app.oie.logic.Receipt, who: String, group: Boolean): String
    val react: String; val readAloud: String; val stopReading: String
    fun reactWith(emoji: String): String
    fun reactionsA11y(list: String): String
    fun birthdayToday(name: String): String
    val birthdayLabel: String; val birthdayInvalid: String
    fun birthdayHelp(kid: String): String
    val needYouLabel: String
    fun needYouHelp(kid: String): String
    val needYouButton: String; val needYouSend: String; val needYouText: String
    fun needYouWait(minutes: Int): String
    fun needYouAsk(names: String): String
    fun needYouSent(names: String): String
    fun nudgeLine(name: String): String
    val nudgeNotNow: String; val nudgeTitle: String
    fun nudgeSwitch(kid: String): String
    fun nudgeBody(kid: String): String
    val draw: String; val drawThin: String; val drawThick: String; val drawClear: String; val drawDone: String; val drawA11y: String
    fun drawFor(to: String): String
    val drawColors: List<String>
    val makeSticker: String; val stickerMade: String; val stickerError: String
    val photos: String
    fun photosWith(name: String): String
    fun noPhotos(name: String): String
    fun and(names: List<String>): String
    val editContact: String; val noPhotosHow: String
    fun mediaFrom(what: String, who: String): String
    val forgotTitle: String; val forgotShowCode: String
    val ok: String; val arriving: String; val you: String; val why: String; val drawUse: String
    fun recordingEnds(seconds: Int): String
    val linkPreparing: String; val linkSlow: String; val whatCanHappen: String; val offlineHome: String
    val newMessages: String; val reactHint: String; val releaseToCancel: String; val holdToRecord: String
    val photoShort: String; val stickerShort: String; val pauseA11y: String; val close: String
    fun downloading(length: String): String
    val welcomeTitle: String; val linkFailed: String; val linkOffline: String
    val sectionQuietShort: String; val sectionHelpShort: String; val expanded: String; val collapsed: String; val reportCleared: String
    fun periodName(name: String): String
    fun forgotBody(kid: String): String
}

object Pt : Strings {
    override val back = "Voltar"; override val cont = "Continuar"; override val cancel = "Cancelar"; override val undo = "Desfazer"
    override val later = "Depois"; override val skip = "Pular"; override val send = "Mandar"; override val save = "Salvar"
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
    override fun yourNameHelp(kid: String, parent: String) = "Aparece para $kid como “WhatsApp de $parent”."
    override fun kidNameHelp(kid: String) = "Vai em negrito no começo das mensagens de $kid."
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
    override fun nobodyYet(parent: String) = "Ainda não tem ninguém aqui. Peça para $parent escolher com quem você pode falar."
    override val askToAdd = "Pedir para adicionar alguém"
    override fun askedToAdd(parent: String) = "Pedido feito. $parent vai ver na área dos pais."
    override fun kidAsked(kid: String) = "$kid pediu para adicionar alguém."
    override fun newCount(n: Int) = "$n ${if (n == 1) "nova" else "novas"}"
    override val noticeTitle = "Antes de começar"
    override fun noticeBody(parent: String) = "Suas mensagens saem do WhatsApp de $parent, então $parent pode ver o que você manda."
    override val understood = "Entendi"
    override fun linkDropped(parent: String) = "O Oiê se desconectou. Peça para $parent conectar de novo."
    override fun quietUntil(hour: Int) = "Horário de silêncio até ${hour}h. Dá para ler e ouvir, mas não para mandar."
    override fun quietUntilShort(hour: Int) = "Horário de silêncio até ${hour}h."
    override fun firstHi(name: String) = "Mande o primeiro oi para $name."
    override val sending = "Mandando…"
    override fun sentAt(time: String) = "Mandada · $time"
    override fun askReconnect(parent: String) = "Peça para $parent conectar de novo."
    override val recording = "Gravando…"; override val releaseToSend = "Solte para mandar"
    override fun writeTo(name: String) = "Escreva para $name"
    override fun sendingTo(name: String) = "Mandando para $name"
    override val photoError = "Não deu para abrir essa foto. Tente outra."
    override val recordA11y = "Gravar áudio"; override val recordingA11y = "Gravando. Toque para mandar"
    override fun stickerA11y(word: String) = "Figurinha: $word"
    override fun sendSticker(word: String) = "Mandar figurinha $word"
    override fun onlyInWhatsApp(name: String) = "$name mandou algo que só abre no WhatsApp."
    override val stickers = "Figurinhas"; override val photo = "Foto"; override val camera = "Câmera"; override val gallery = "Escolher da galeria"
    override fun photoFor(name: String) = "Foto para $name"
    override val photoPreviewA11y = "A foto que você vai mandar"; override val swap = "Trocar"
    override fun listenA11y(seconds: Int) = "Ouvir áudio de $seconds segundos"
    override val photoButton = "Foto"; override val stickerButton = "Figurinhas"
    override val parentArea = "Área dos pais"; override val enterPin = "Digite o seu PIN."
    override val tooMany = "Muitas tentativas. Espere 1 minuto."; override val wrongPin = "PIN errado. Tente de novo."; override val forgotPin = "Esqueci o PIN"
    override val leave = "Sair"; override val sectionContacts = "Contatos"; override val sectionQuiet = "Horário de silêncio"
    override val sectionConnection = "Conexão"; override val sectionHelp = "Ajustes e ajuda"; override val connection = "Conexão"
    override fun connectedTo(parent: String) = "Conectado ao WhatsApp de $parent."
    override fun connectingTo(parent: String) = "Conectando ao WhatsApp de $parent…"
    override val disconnected = "O Oiê se desconectou do seu WhatsApp."
    override val reconnect = "Conectar de novo"
    override fun whoCanCall(kid: String) = "Com quem $kid pode falar"
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
        "$kid sabe que eu vejo as mensagens?" to "Sim. Na primeira vez, o Oiê diz a $kid: “${noticeBody(parent)}”",
        "Quem pode falar com $kid?" to "Só quem está em Contatos. Mensagens de qualquer outra pessoa não aparecem no tablet. Num grupo, todos do grupo veem o que $kid manda. Em Contatos, toque em alguém para mudar o nome que $kid vê (Vovó, por exemplo) ou para remover. O nome muda só no Oiê, não no seu WhatsApp.",
        "O que é o “Preciso de você”?" to "Um botão na tela inicial de $kid, que aparece quando você marca alguém em Contatos (toque na pessoa e ligue “Recebe 'Preciso de você'”). $kid confirma, e cada pessoa marcada recebe “Preciso de você.” no WhatsApp, até no horário de silêncio. Dá para mandar uma vez a cada 5 minutos. É um jeito de chamar vocês, não um serviço de emergência: em perigo, ligue 190 ou 192.",
        "Como funciona o horário de silêncio?" to "Durante um período, $kid lê e ouve, mas não manda mensagens. Quem tem “Pode falar no silêncio” em Contatos continua liberado. Dá para pausar um período sem apagar, por exemplo nas férias.",
        "O Oiê se desconectou. E agora?" to "Abra Conexão e toque em Conectar o WhatsApp. As mensagens que $kid mandou enquanto isso ficam guardadas e saem depois.",
        "Como mando figurinhas para $kid?" to "No seu WhatsApp, mande a figurinha para você mesmo, na conversa com o seu próprio nome. Ela aparece para $kid em Figurinhas, em Novas de $parent, e $kid escolhe se guarda. Só as figurinhas dessa conversa chegam ao Oiê; o resto dela fica só com você. Funciona com o WhatsApp de verdade ligado.",
        "Posso mudar algo pelo meu WhatsApp?" to "Sim. Numa conversa do seu WhatsApp, mande /$kid (ou /um apelido) e o Oiê responde na sua conversa com você mesmo, com a lista: parar, voltar, adicionar e remover. O comando vale para a conversa onde você mandou. Quem está nela vê o seu comando, mas não as respostas. Só o seu WhatsApp pode usar.",
        "Esqueci o PIN" to "Na tela do PIN, toque em Esqueci o PIN e conecte o mesmo WhatsApp de novo. Isso prova que é você. Depois crie um PIN novo.",
        "Onde ficam as mensagens e as fotos?" to "Só neste tablet, guardadas de forma protegida. Nada vai para um servidor nosso. O Oiê não tem anúncios e não acompanha o que $kid faz.",
    )
    override fun aboutVersion(v: String) = "Oiê $v, versão de teste. Usa o seu WhatsApp de verdade."
    override val notWhatsApp = "O Oiê não é feito nem aprovado pelo WhatsApp ou pela Meta."
    override val credits = "Fonte Nunito, SIL Open Font License 1.1. Ícones do Gabriel Design System. Conexão com o WhatsApp feita com whatsmeow, Mozilla Public License 2.0, código em github.com/tulir/whatsmeow. QR com ZXing, Apache License 2.0. Vídeos preparados com Media3, Apache License 2.0. Figurinhas animadas com Lottie, Apache License 2.0."
    override val theKid = "a criança"
    override val notifChannel = "Mensagens da família"; override val notifVoice = "Mandou um áudio"
    override val notifPhoto = "Mandou uma foto"; override val notifSticker = "Mandou uma figurinha"
    override val notifVideo = "Mandou um vídeo"; override val notifGif = "Mandou um GIF"; override val video = "Vídeo"; override val gifLabel = "GIF"
    override fun playVideoA11y(seconds: Int) = "Ver o vídeo, %d:%02d".format(seconds / 60, seconds % 60)
    override fun videoTooBig(parent: String) = "Vídeo grande: só abre no WhatsApp de $parent."
    override val pause = "Pausar"; override val play = "Tocar"
    override val writeShort = "Escreva"
    override fun parentLineTitle(parent: String) = "Você ($parent)"
    override fun parentLineCreate(kid: String) = "Criar conversa com $kid"
    override fun parentLineBody(kid: String) = "Cria no seu WhatsApp um grupo só seu chamado “$kid”. O que $kid mandar para você aparece lá, e o que você escrever lá chega para $kid."
    override val parentLineNoNotify = "O WhatsApp não avisa você dessas mensagens, porque saem da sua própria conta. Fixe a conversa no topo para ver quando chegam."
    override val parentLineCreating = "Criando…"
    override val parentLineNotNow = "Conecte o WhatsApp para criar a conversa."
    override fun parentLineFallback(kid: String) = "O WhatsApp não aceitou o grupo. $kid vai escrever na sua Mensagem para você; para responder, deslize a mensagem dele ou comece com o nome dele."
    override fun parentLineInGroup(kid: String) = "Grupo “$kid” no seu WhatsApp"
    override val parentLineInOwnChat = "Sua Mensagem para você no WhatsApp"
    override val photoOrVideo = "Foto ou vídeo"; override val takePhoto = "Tirar foto"; override val recordVideo = "Gravar vídeo"
    override fun videoFor(to: String) = "Vídeo para $to"
    override fun gifFor(to: String) = "GIF para $to"
    override val preparingVideo = "Preparando o vídeo…"
    override val videoTooLong = "Esse vídeo tem mais de 3 minutos. Escolha um mais curto."
    override val videoTooBigToSend = "Esse vídeo ficou grande demais. Escolha um mais curto."
    override val videoError = "Não deu para abrir esse vídeo. Tente outro."
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
    override val releaseOnly = "A atualização automática só funciona na versão de lançamento."
    override val version = "Versão"
    override fun versionLine(v: String, code: Long) = "Oiê v$v ($code)"
    override val decimalComma = true
    override val notifLinkChannel = "Conexão com o WhatsApp"; override val notifLinkOn = "O Oiê está conectado."
    override val linkConnect = "Conectar o WhatsApp"
    override val linkWaiting = "Esperando o WhatsApp do seu celular."
    override val notSent = "Não foi"; override val tryAgain = "Tentar de novo"; override val unlinkReal = "Desconectar o WhatsApp"
    override fun pickRealContacts(kid: String) = "Escolha nos seus contatos com quem $kid pode falar."
    override val names = "Nomes"
    override fun newStickersFrom(parent: String) = "Novas de $parent"
    override val keepSticker = "Guardar"; override val sendFamilySticker = "Mandar figurinha da família"; override val familyStickers = "Da família"
    override val micError = "Não deu para gravar. Tente de novo."
    override val linkQrA11y = "Código para conectar o WhatsApp"; override val linkUnavailable = "Este tablet não consegue usar a conexão com o WhatsApp."
    override fun bannedFor(hours: Int) = "O WhatsApp bloqueou este número por cerca de $hours h. O Oiê parou e não vai tentar de novo sozinho."
    override val search = "Buscar"; override val searchHelp = "Busque pelo nome ou pelo número. Os contatos chegam aos poucos depois de conectar."
    override val addByNumber = "Adicionar pelo número"; override val numberLabel = "Número com DDD"; override val numberHelp = "Com o código do país: 55 11 99999-0000."
    override val bubbleTitle = "Bolha na tela"
    override val bubbleBody = "O Oiê fica numa bolha por cima dos outros apps, e cada conversa ativa ganha a sua. Tocar numa bolha abre uma janela menor."
    override val bubbleSwitch = "Mostrar a bolha"
    override val bubbleOnLine = "A bolha está na tela. Para tirar, arraste até o X ou desligue aqui."
    override val bubbleNeedsPermission = "O Android ainda não deixa o Oiê mostrar bolhas."
    override val bubbleSamsung = "Em tablets Samsung: Configurações › Notificações › Configurações avançadas › Notificações flutuantes › Bolhas."
    override val bubbleNeedsNotifications = "Ative as notificações do Oiê para ver a bolha."
    override val bubbleUnsupported = "Este tablet não tem bolhas (precisa do Android 11 ou mais novo)."
    override val bubbleAllow = "Permitir bolhas"; override val bubbleChannel = "Bolha do Oiê"; override val bubbleText = "Toque para falar com a família"
    override val emojiLabel = "Emoji antes do nome"; override val emojiHelp = "Opcional. Escolha um no teclado."
    override val looksLikeOnWhatsApp = "Fica assim no WhatsApp:"; override val sampleMessage = "Oi, vó"
    override fun nicknamesLabel(kid: String) = "Apelidos de $kid"
    override fun nicknamesHelp(kid: String) = "Separados por vírgula. Uma mensagem que começa com qualquer um deles, como “Oi Chico”, chega para $kid."
    override val pausedLine = "Pausado pelo seu WhatsApp"; override val resumeChat = "Voltar a receber"
    override fun cmdHelp(kid: String, cmd: String, here: String?) = listOfNotNull(
        "Oiê · comandos para $kid", here,
        "Mande na conversa que você quer mudar:",
        "$cmd parar · pausa esta conversa para $kid",
        "$cmd voltar · volta a mostrar esta conversa",
        "$cmd adicionar · coloca esta conversa nos contatos de $kid",
        "$cmd remover · tira esta conversa dos contatos",
        "Só o seu WhatsApp pode usar os comandos. As respostas chegam só aqui.",
    ).joinToString("\n")
    override fun cmdHereReceives(chat: String, kid: String) = "Agora, $chat: $kid recebe as mensagens."
    override fun cmdHerePaused(chat: String, kid: String) = "Agora, $chat: pausada para $kid."
    override fun cmdHereNotIn(kid: String) = "Agora: esta conversa não está nos contatos de $kid."
    override fun cmdPaused(chat: String, kid: String, cmd: String) = "Oiê · pausado: $chat não chega mais para $kid. Para voltar: $cmd voltar"
    override fun cmdResumed(chat: String, kid: String) = "Oiê · $chat voltou para $kid."
    override fun cmdAdded(chat: String, kid: String, group: Boolean) = "Oiê · $chat agora pode falar com $kid." + if (group) " Todos do grupo veem o que $kid manda." else ""
    override fun cmdAlready(chat: String, kid: String) = "Oiê · $chat já pode falar com $kid."
    override fun cmdRemoved(chat: String, kid: String) = "Oiê · $chat saiu dos contatos de $kid."
    override fun cmdNotIn(kid: String, cmd: String) = "Oiê · esta conversa não está nos contatos de $kid. Para incluir: $cmd adicionar"
    override fun cmdOwnChat(kid: String, cmd: String) = "Oiê · mande o comando na conversa que você quer mudar, por exemplo na conversa com a avó: $cmd parar"
    override fun cmdUnknown(word: String, kid: String, cmd: String) = "Oiê · não entendi “$word”.\n" + cmdHelp(kid, cmd, null)
    override fun renameHint(kid: String) = "Toque em alguém para mudar o nome que $kid vê ou para remover."
    override fun removeNamed(name: String) = "Remover $name dos contatos"
    override val changeName = "Mudar nome"; override val noNumber = "Número não disponível"; override val groupShort = "Grupo"
    override fun changeNameA11y(name: String) = "Mudar o nome de $name"
    override fun nameKidSees(kid: String) = "Nome que $kid vê"
    override fun onWhatsApp(name: String) = "$name no WhatsApp"
    override fun renameHelp(waName: String) = "Em branco, volta a ser $waName. No WhatsApp, nada muda."
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

    // v0.34 audit: no time here; the message already shows it.
    override fun receiptLine(r: app.oie.logic.Receipt, who: String, group: Boolean) = when (r) {
        app.oie.logic.Receipt.READ -> if (group) "Alguém viu" else "$who viu"
        app.oie.logic.Receipt.DELIVERED -> "Entregue"
        else -> "Mandada"
    }
    override val react = "Reagir"; override val readAloud = "Ouvir"; override val stopReading = "Parar"
    override fun reactWith(emoji: String) = "Reagir com $emoji"
    override fun reactionsA11y(list: String) = "Reações: $list"
    override fun birthdayToday(name: String) = "$name faz aniversário hoje"
    override val birthdayLabel = "Aniversário (dia/mês)"
    override val birthdayInvalid = "Esse dia não existe. Escreva dia/mês, como 25/12."
    override fun birthdayHelp(kid: String) = "Por exemplo 25/12. No dia, $kid vê um lembrete. O ano não é guardado."
    override val needYouLabel = "Recebe “Preciso de você”"
    override fun needYouHelp(kid: String) = "Quando $kid tocar em Preciso de você, essa pessoa recebe uma mensagem."
    override val needYouButton = "Preciso de você"; override val needYouSend = "Mandar"; override val needYouText = "Preciso de você."
    override fun needYouWait(minutes: Int) = if (minutes <= 1) "Já foi. Dá para mandar de novo em 1 minuto." else "Já foi. Dá para mandar de novo em $minutes minutos."
    override fun needYouAsk(names: String) = "Mandar “Preciso de você” para $names?"
    override fun needYouSent(names: String) = "Mandado para $names."
    override fun nudgeLine(name: String) = "Uma semana sem falar com $name"
    override val nudgeNotNow = "Agora não"; override val nudgeTitle = "Lembrete de família"
    override fun nudgeSwitch(kid: String) = "Lembrar $kid de falar com a família"
    override fun nudgeBody(kid: String) = "Se $kid passar uma semana sem escrever para alguém, o Oiê mostra um lembrete na tela inicial. Um por vez, sem contar dias."
    override val draw = "Desenhar"; override val drawThin = "Fino"; override val drawThick = "Grosso"; override val drawClear = "Limpar"
    override val drawDone = "Pronto"; override val drawA11y = "Área de desenho"
    override fun drawFor(to: String) = "Desenho para $to"
    override val drawColors = listOf("Preto", "Vermelho", "Amarelo", "Verde", "Azul")
    override val makeSticker = "Criar figurinha"; override val stickerMade = "Figurinha criada. Ela está nas suas figurinhas."
    override val stickerError = "Não deu para usar essa foto. Tente outra."
    override val photos = "Fotos"
    override fun photosWith(name: String) = "Fotos com $name"
    override fun noPhotos(name: String) = "Ainda não tem fotos com $name."
    override fun and(names: List<String>) = if (names.size < 2) names.joinToString() else names.dropLast(1).joinToString(", ") + " e " + names.last()
    override val editContact = "Editar contato"; override val noPhotosHow = "As fotos, os vídeos e os GIFs da conversa aparecem aqui. Para mandar um, toque em Foto."
    override fun mediaFrom(what: String, who: String) = "$what de $who"
    override val newMessages = "Novas"; override val reactHint = "Segure uma mensagem para reagir ou ouvir."
    override val releaseToCancel = "Solte para cancelar"; override val holdToRecord = "Segure o botão enquanto fala."
    override val photoShort = "Foto"; override val stickerShort = "Figurinha"; override val pauseA11y = "Pausar"; override val close = "Fechar"
    override fun downloading(length: String) = "Vídeo de $length, chegando…"
    override val linkPreparing = "Preparando o código…"; override val linkSlow = "Isso pode levar até 1 minuto."
    override val whatCanHappen = "O que pode acontecer com o meu WhatsApp?"
    override val offlineHome = "Sem internet agora. Suas mensagens vão quando ela voltar."
    override val you = "Você"; override val why = "Por quê?"; override val drawUse = "Usar desenho"
    override fun recordingEnds(seconds: Int) = "Termina em $seconds s"
    override val ok = "Usar"; override val arriving = "Chegando…"
    override val welcomeTitle = "A família ao alcance."
    override val sectionQuietShort = "Silêncio"; override val sectionHelpShort = "Ajustes"; override val expanded = "Aberto"; override val collapsed = "Fechado"
    override val reportCleared = "Relatório apagado."
    override val linkFailed = "Não deu para conectar. Confira a internet do celular e tente de novo."
    override val linkOffline = "Sem internet no tablet. Quando ela voltar, tente de novo."
    override fun periodName(name: String) = name
    override val forgotTitle = "Fazer um PIN novo"; override val forgotShowCode = "Mostrar o código"
    override fun forgotBody(kid: String) = "Para provar que é você, conecte o Oiê ao seu WhatsApp de novo. O tablet sai do WhatsApp e mostra um código para ler com o seu celular, como no começo. Os contatos e as conversas de $kid continuam aqui. Depois, você escolhe o PIN novo."
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
    override fun yourNameHelp(kid: String, parent: String) = "$kid sees it as “$parent's WhatsApp”."
    override fun kidNameHelp(kid: String) = "It goes in bold at the start of $kid's messages."
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
    override fun nobodyYet(parent: String) = "Nobody here yet. Ask $parent to choose who you can talk to."
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
    override val stickers = "Stickers"; override val photo = "Photo"; override val camera = "Camera"; override val gallery = "Choose from gallery"
    override fun photoFor(name: String) = "Photo for $name"
    override val photoPreviewA11y = "The photo you're about to send"; override val swap = "Change"
    override fun listenA11y(seconds: Int) = "Play a $seconds-second voice message"
    override val photoButton = "Photo"; override val stickerButton = "Stickers"
    override val parentArea = "Parent area"; override val enterPin = "Enter your PIN."
    override val tooMany = "Too many tries. Wait 1 minute."; override val wrongPin = "Wrong PIN. Try again."; override val forgotPin = "I forgot the PIN"
    override val leave = "Leave"; override val sectionContacts = "Contacts"; override val sectionQuiet = "Quiet hours"
    override val sectionConnection = "Connection"; override val sectionHelp = "Settings and help"; override val connection = "Connection"
    override fun connectedTo(parent: String) = "Linked to $parent's WhatsApp."
    override fun connectingTo(parent: String) = "Connecting to $parent's WhatsApp…"
    override val disconnected = "Oiê got disconnected from your WhatsApp."
    override val reconnect = "Link again"
    override fun whoCanCall(kid: String) = "Who $kid can talk to"
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
        "Does $kid know I can see the messages?" to "Yes. The first time, Oiê tells $kid: “${noticeBody(parent)}”",
        "Who can talk to $kid?" to "Only the people in Contacts. Messages from anyone else don't appear on the tablet. In a group, everyone in it sees what $kid sends. In Contacts, tap someone to change the name $kid sees (Grandma, for example) or to remove them. The name changes only in Oiê, not in your WhatsApp.",
        "What is “I need you”?" to "A button on $kid's home screen that shows once you mark someone in Contacts (tap the person and turn on “Gets 'I need you'”). $kid confirms, and each marked person gets “I need you.” on WhatsApp, even in quiet hours. It can be sent once every 5 minutes. It's a way to call you, not an emergency service: in danger, call the emergency number.",
        "How do quiet hours work?" to "During a period, $kid can read and listen, but not send. Anyone with “Can talk in quiet hours” in Contacts still gets through. You can pause a period without deleting it, for example on holiday.",
        "Oiê got disconnected. What now?" to "Open Connection and tap Link WhatsApp. Messages $kid sent in the meantime are kept and go out afterwards.",
        "How do I send stickers to $kid?" to "In your WhatsApp, send the sticker to yourself, in the chat with your own name. It shows for $kid under Stickers, in New from $parent, and $kid chooses whether to keep it. Only stickers from that chat reach Oiê; the rest of it stays with you. Works once the real WhatsApp is on.",
        "Can I change things from my WhatsApp?" to "Yes. In a chat on your WhatsApp, send /$kid (or /a nickname) and Oiê answers in your chat with yourself, with the list: stop, start, add and remove. The command works on the chat you sent it in. People in that chat see your command, but not the answers. Only your WhatsApp can use them.",
        "I forgot the PIN" to "On the PIN screen, tap I forgot the PIN and link the same WhatsApp again. That proves it's you. Then create a new PIN.",
        "Where are the messages and photos kept?" to "Only on this tablet, stored securely. Nothing goes to a server of ours. Oiê has no ads and doesn't track what $kid does.",
    )
    override fun aboutVersion(v: String) = "Oiê $v, a test version. It uses your real WhatsApp."
    override val notWhatsApp = "Oiê isn't made or approved by WhatsApp or Meta."
    override val credits = "Nunito font, SIL Open Font License 1.1. Icons from the Gabriel Design System. WhatsApp link built with whatsmeow, Mozilla Public License 2.0, source at github.com/tulir/whatsmeow. QR codes with ZXing, Apache License 2.0. Videos prepared with Media3, Apache License 2.0. Animated stickers with Lottie, Apache License 2.0."
    override val theKid = "your child"
    override val notifChannel = "Messages from family"; override val notifVoice = "Sent a voice message"
    override val notifPhoto = "Sent a photo"; override val notifSticker = "Sent a sticker"
    override val notifVideo = "Sent a video"; override val notifGif = "Sent a GIF"; override val video = "Video"; override val gifLabel = "GIF"
    override fun playVideoA11y(seconds: Int) = "Watch the video, %d:%02d".format(seconds / 60, seconds % 60)
    override fun videoTooBig(parent: String) = "Big video: it only opens in $parent's WhatsApp."
    override val pause = "Pause"; override val play = "Play"
    override val writeShort = "Write"
    override fun parentLineTitle(parent: String) = "You ($parent)"
    override fun parentLineCreate(kid: String) = "Create a chat with $kid"
    override fun parentLineBody(kid: String) = "Creates a group with only you in your WhatsApp, called “$kid”. What $kid sends you shows there, and what you write there reaches $kid."
    override val parentLineNoNotify = "WhatsApp won't notify you of these messages, because they come from your own account. Pin the chat at the top to see when they arrive."
    override val parentLineCreating = "Creating…"
    override val parentLineNotNow = "Link WhatsApp to create the chat."
    override fun parentLineFallback(kid: String) = "WhatsApp didn't accept the group. $kid will write in your Message yourself chat; to answer, swipe to reply to his message or start with his name."
    override fun parentLineInGroup(kid: String) = "Group “$kid” in your WhatsApp"
    override val parentLineInOwnChat = "Your Message yourself chat in WhatsApp"
    override val photoOrVideo = "Photo or video"; override val takePhoto = "Take a photo"; override val recordVideo = "Record a video"
    override fun videoFor(to: String) = "Video for $to"
    override fun gifFor(to: String) = "GIF for $to"
    override val preparingVideo = "Getting the video ready…"
    override val videoTooLong = "That video is longer than 3 minutes. Pick a shorter one."
    override val videoTooBigToSend = "That video came out too big. Pick a shorter one."
    override val videoError = "That video couldn't be opened. Try another."
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
    override val releaseOnly = "Self-update works in the release build only."
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
    override val search = "Search"; override val searchHelp = "Search by name or number. Contacts arrive bit by bit after linking."
    override val addByNumber = "Add by number"; override val numberLabel = "Phone number"; override val numberHelp = "With the country code: 55 11 99999-0000."
    override val bubbleTitle = "Bubble on screen"
    override val bubbleBody = "Oiê stays in a bubble on top of other apps, and each active chat gets its own. Tapping a bubble opens a smaller window."
    override val bubbleSwitch = "Show the bubble"
    override val bubbleOnLine = "The bubble is on screen. To remove it, drag it to the X or turn it off here."
    override val bubbleNeedsPermission = "Android doesn't let Oiê show bubbles yet."
    override val bubbleSamsung = "On Samsung tablets: Settings › Notifications › Advanced settings › Floating notifications › Bubbles."
    override val bubbleNeedsNotifications = "Turn on Oiê's notifications to see the bubble."
    override val bubbleUnsupported = "This tablet has no bubbles (it needs Android 11 or newer)."
    override val bubbleAllow = "Allow bubbles"; override val bubbleChannel = "Oiê bubble"; override val bubbleText = "Tap to talk to the family"
    override val emojiLabel = "Emoji before the name"; override val emojiHelp = "Optional. Pick one on the keyboard."
    override val looksLikeOnWhatsApp = "On WhatsApp it looks like this:"; override val sampleMessage = "Hi, Grandma"
    override fun nicknamesLabel(kid: String) = "$kid's nicknames"
    override fun nicknamesHelp(kid: String) = "Separated by commas. A message that starts with any of them, like “Hi Chico”, reaches $kid."
    override val pausedLine = "Paused from your WhatsApp"; override val resumeChat = "Show it again"
    override fun cmdHelp(kid: String, cmd: String, here: String?) = listOfNotNull(
        "Oiê · commands for $kid", here,
        "Send them in the chat you want to change:",
        "$cmd stop · pauses this chat for $kid",
        "$cmd start · shows this chat again",
        "$cmd add · puts this chat in $kid's contacts",
        "$cmd remove · takes this chat out of the contacts",
        "Only your WhatsApp can use commands. Answers come only here.",
    ).joinToString("\n")
    override fun cmdHereReceives(chat: String, kid: String) = "Now, $chat: $kid gets the messages."
    override fun cmdHerePaused(chat: String, kid: String) = "Now, $chat: paused for $kid."
    override fun cmdHereNotIn(kid: String) = "Now: this chat isn't in $kid's contacts."
    override fun cmdPaused(chat: String, kid: String, cmd: String) = "Oiê · paused: $chat no longer reaches $kid. To undo: $cmd start"
    override fun cmdResumed(chat: String, kid: String) = "Oiê · $chat is back for $kid."
    override fun cmdAdded(chat: String, kid: String, group: Boolean) = "Oiê · $chat can now talk to $kid." + if (group) " Everyone in the group sees what $kid sends." else ""
    override fun cmdAlready(chat: String, kid: String) = "Oiê · $chat can already talk to $kid."
    override fun cmdRemoved(chat: String, kid: String) = "Oiê · $chat left $kid's contacts."
    override fun cmdNotIn(kid: String, cmd: String) = "Oiê · this chat isn't in $kid's contacts. To add it: $cmd add"
    override fun cmdOwnChat(kid: String, cmd: String) = "Oiê · send the command in the chat you want to change, for example in the chat with grandma: $cmd stop"
    override fun cmdUnknown(word: String, kid: String, cmd: String) = "Oiê · I didn't understand “$word”.\n" + cmdHelp(kid, cmd, null)
    override fun renameHint(kid: String) = "Tap someone to change the name $kid sees, or to remove them."
    override fun removeNamed(name: String) = "Remove $name from contacts"
    override val changeName = "Change name"; override val noNumber = "Number not available"; override val groupShort = "Group"
    override fun changeNameA11y(name: String) = "Change the name of $name"
    override fun nameKidSees(kid: String) = "Name $kid sees"
    override fun onWhatsApp(name: String) = "$name on WhatsApp"
    override fun renameHelp(waName: String) = "Left blank, it goes back to $waName. Nothing changes on WhatsApp."
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

    override fun receiptLine(r: app.oie.logic.Receipt, who: String, group: Boolean) = when (r) {
        app.oie.logic.Receipt.READ -> if (group) "Someone saw it" else "$who saw it"
        app.oie.logic.Receipt.DELIVERED -> "Delivered"
        else -> "Sent"
    }
    override val react = "React"; override val readAloud = "Listen"; override val stopReading = "Stop"
    override fun reactWith(emoji: String) = "React with $emoji"
    override fun reactionsA11y(list: String) = "Reactions: $list"
    override fun birthdayToday(name: String) = "It's $name's birthday today"
    override val birthdayLabel = "Birthday (day/month)"
    override val birthdayInvalid = "That day doesn't exist. Write day/month, like 25/12."
    override fun birthdayHelp(kid: String) = "For example 25/12. On the day, $kid sees a reminder. The year isn't kept."
    override val needYouLabel = "Gets “I need you”"
    override fun needYouHelp(kid: String) = "When $kid taps I need you, this person gets a message."
    override val needYouButton = "I need you"; override val needYouSend = "Send"; override val needYouText = "I need you."
    override fun needYouWait(minutes: Int) = if (minutes <= 1) "It went. You can send it again in 1 minute." else "It went. You can send it again in $minutes minutes."
    override fun needYouAsk(names: String) = "Send “I need you” to $names?"
    override fun needYouSent(names: String) = "Sent to $names."
    override fun nudgeLine(name: String) = "A week since you talked to $name"
    override val nudgeNotNow = "Not now"; override val nudgeTitle = "Family reminder"
    override fun nudgeSwitch(kid: String) = "Remind $kid to talk to family"
    override fun nudgeBody(kid: String) = "If $kid goes a week without writing to someone, Oiê shows a reminder on the home screen. One at a time, no counting days."
    override val draw = "Draw"; override val drawThin = "Thin"; override val drawThick = "Thick"; override val drawClear = "Clear"
    override val drawDone = "Done"; override val drawA11y = "Drawing area"
    override fun drawFor(to: String) = "Drawing for $to"
    override val drawColors = listOf("Black", "Red", "Yellow", "Green", "Blue")
    override val makeSticker = "Make a sticker"; override val stickerMade = "Sticker made. It's with your stickers."
    override val stickerError = "Couldn't use that photo. Try another one."
    override val photos = "Photos"
    override fun photosWith(name: String) = "Photos with $name"
    override fun noPhotos(name: String) = "No photos with $name yet."
    override fun and(names: List<String>) = if (names.size < 2) names.joinToString() else names.dropLast(1).joinToString(", ") + " and " + names.last()
    override val editContact = "Edit contact"; override val noPhotosHow = "The chat's photos, videos and GIFs show here. To send one, tap Photo."
    override fun mediaFrom(what: String, who: String) = "$what from $who"
    override val newMessages = "New"; override val reactHint = "Hold a message to react or listen."
    override val releaseToCancel = "Release to cancel"; override val holdToRecord = "Hold the button while you talk."
    override val photoShort = "Photo"; override val stickerShort = "Sticker"; override val pauseA11y = "Pause"; override val close = "Close"
    override fun downloading(length: String) = "A $length video, on its way…"
    override val linkPreparing = "Preparing the code…"; override val linkSlow = "This can take up to a minute."
    override val whatCanHappen = "What could happen to my WhatsApp?"
    override val offlineHome = "No internet right now. Your messages go when it's back."
    override val you = "You"; override val why = "Why?"; override val drawUse = "Use drawing"
    override fun recordingEnds(seconds: Int) = "Ends in $seconds s"
    override val ok = "Use"; override val arriving = "On its way…"
    override val welcomeTitle = "Family, within reach."
    override val sectionQuietShort = "Quiet"; override val sectionHelpShort = "Settings"; override val expanded = "Open"; override val collapsed = "Closed"
    override val reportCleared = "Report deleted."
    override val linkFailed = "Couldn't link. Check the phone's internet and try again."
    override val linkOffline = "No internet on the tablet. When it's back, try again."
    // The presets are stored in Portuguese; shown in English when the app is.
    override fun periodName(name: String) = when (name) { "Escola" -> "School"; "Dormir" -> "Sleep"; else -> name }
    override val forgotTitle = "Make a new PIN"; override val forgotShowCode = "Show the code"
    override fun forgotBody(kid: String) = "To prove it's you, link Oiê to your WhatsApp again. The tablet leaves WhatsApp and shows a code to scan with your phone, like at the start. $kid's contacts and chats stay here. Then you choose the new PIN."
}

val LocalStrings = staticCompositionLocalOf<Strings> { Pt }

/** Portuguese unless the device (or the per-app language setting) asks for English. */
@Composable
@ReadOnlyComposable
fun stringsFor(): Strings = if (LocalConfiguration.current.locales[0].language == "en") En else Pt

/** The current language's words. In a lambda, read it into a val first: `val tx = str`. */
val str: Strings
    @Composable @ReadOnlyComposable get() = LocalStrings.current
