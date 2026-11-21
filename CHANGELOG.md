# Changelog

## Unreleased
- **`scripts/downloads.sh`:** shows how many times each release was downloaded, from GitHub's public counts. Nothing in the app is measured. See `docs/release.md`, "Downloads".
- **Website: "Como falar com uma criança no Oiê" is simpler, made to share in a family group.**
  - It's five short steps, each with a sample chat that says where each message goes.
  - The steps: write to the parent's WhatsApp; start with the name, or answer the child; in a group everyone sees everything; the parents see everything.
- **Website FAQ: "Por que um tablet e não um celular?"**
  - A tablet stays at home, like the old home phone, and doesn't go in a pocket. Kids talk at calmer moments, not all day.
  - It cites the federal guide's advice against an own phone before 12.
  - If you choose a phone, Oiê works on it too.
- **Website: groups.** The highlights, the FAQ and the safety page now say that in a group you approve, your kid sees the whole group, and people not on their list are marked (v0.46).
- **Website: the example on phones reads as a story to scroll through.** Some visitors didn't see that the example continued.
  - A bar of six steps and "Próximo" now sit right above the words, at the bottom of the picture.
  - The picture is smaller, so the next step's title always peeks in below.
  - "Próximo" lands each step with its title in full view.
- **Website FAQ: "O Oiê é seguro?"** It's first in the list. It says what keeps the risk low and that no app makes a kid 100% safe. It also says that the code is open, so anyone can read it on GitHub and check.
- **Website: "Família e amigos" is in the top menu** of every page, next to "Segurança" (the owner: the information must be easy to reach, not only in the footer). On a phone the menu gets its own row under the logo and the language switch, and the page you're on is underlined.
- **Website: a page for family and friends,** "Como falar com uma criança no Oiê" (`/falar-com-a-crianca/`, EN `/talk-to-a-child/`). It's a link the parent can send to a grandparent or a friend's parent. It explains how to start a conversation (an invitation, a good night): be on the list, write to the parent's number, start with the child's name. It also covers what reaches the tablet, what stays with the parent, and that the parent sees everything. A test keeps the page's examples true to the app's rule. The landing page's footer links to it.
- **Website: no more "meeting outside".** The landing page no longer says that the conversation leads to meeting up (it could read the wrong way). It now talks about sharing the day and missing each other less, and the school friend's example is about yesterday's game.
- **Website polish:** fewer repeated "telefone de casa" lines and no tacked-on fragments. The hero is one sentence, and step 2 now says "A mensagem sai…".
- **Website: the landing page tells the story of the home phone** (decision 0014). A friend said the page felt like watching over kids. Now it opens with "Lembra do telefone de casa?": the kid talks to the family and friends you choose, makes a plan, and the meeting happens outside. The look and the story's steps are the same; only the words changed. "Você vê o que sai" left the highlights (it's still answered in the FAQ), and the footer says Oiê is open source, made by Gabriel Lopes.
- **Website polished to full marks (impeccable, independent review): 40/40 and 20/20 on both pages.**
  - **Safety page:** calmer text, with one weight per job, sources as small print, and 190 and 100 one tap to call.
  - **Landing page:** the story works on phones (one person at a time, never over the words), "Próximo" moves through the steps, and nothing fails contrast.
  - **Landing page copy:** it says to open the page on the kid's tablet, what to do if Android won't install, and what happens if WhatsApp blocks the number.
- **Website: published from the public releases repo,** at https://gabrielbr.github.io/oie-releases/ (the downloads live there too). The source stays here.
- **Website: the font is served by the site itself.** The pages no longer ask Google Fonts, or any other site, for anything. A check fails the publish if one ever does.
- **Website, privacy in plain words:** "Nada passa por nós" on the landing page, a new question "O Oiê guarda alguma coisa sobre a minha família?", and the safety page all now say that Oiê has no servers, links to your WhatsApp like WhatsApp Web, and has no analytics, ads or tracking.
- **Website: "Por que fizemos o Oiê" and a safety guide for parents** (`/seguranca/`, in English at `/safety/`). It explains why Oiê exists: the home phone of the 90s, which kids used to plan meeting friends in real life, without ads or data collection. Then a guide with sources:
  - talking first;
  - the tools you already have: Google Family Link, Google Play, SafeSearch and Chrome, Samsung Kids, Apple Screen Time, YouTube Kids, your WhatsApp's settings, and a family DNS;
  - the risks at 9–12 and what to do about each;
  - what Brazil's law says (ECA Digital, LGPD);
  - where to get help (SaferNet, Disque 100, 190).

  The landing page links to it.
- **Website:** the landing page in `site/` (PT and EN). Its hero animates the family line: Tito's message goes from the tablet, through your WhatsApp, to Vovó. Every "Baixar" links to the latest release. It goes live when oie.app is registered.
- **Website v2:** a scrolling story with people.
  - **Your family:** pick son or daughter, mom or dad, and who they write to (grandma, grandpa, an aunt or a school friend), and the story uses your family.
  - **The screens:** the message is shown on the tablet, then on your WhatsApp, then on theirs, and the reply comes back.
  - **Not only grandma:** the copy and FAQ now say "family and friends you choose" (decision 0013).




## 0.48.0
- **Tito picks how the home looks.** In Arrumar › Mostrar como:
  - **Lista:** one row each.
  - **Fotos:** a grid of big photos with the names, nothing to read.
  - **Mosaico:** cards as tall as each person's last message.

  The choice is remembered. Until Tito picks, a phone shows Lista and the tablet Mosaico (today's cards).
- **People and groups apart:** the home shows **Pessoas**, then **Grupos**, in every view and in Arrumar. News still comes first in each, then favorites, then Tito's order. Your own line with Tito counts as a person.
- **What you have to do:** nothing.

## 0.47.0
- **Oiê introduces Tito in each chat, once** (in the words you approved). Before Tito's first message in a chat, Oiê sends one short line.
  - **To a person:** "Oiê · Tito está escrevendo pelo WhatsApp de Ana, com o app Oiê, no tablet. Responda aqui mesmo. Para puxar conversa com Tito depois, comece a mensagem com “Tito”. Como funciona: (link)".
  - **To a group:** it says Tito will take part and sees everything the group says.
  - **When:** every chat gets it once, chats where Tito already wrote included. It goes out at the usual pace, 4 s before Tito's message, and never in your own chat with Tito.
  - **Turning it off:** Ajustes › Primeira mensagem.
- **Reactions sit on the message,** on its bottom edge, as in WhatsApp, instead of below it.
- **Nothing touches the screen's edge:** everything sits inside the coral frame, and the top bar has room above and below its buttons.
- **What you have to do:** nothing. If you'd rather Oiê didn't introduce Tito, turn it off in Ajustes before Tito writes.

## 0.46.0
- **Groups work like groups** (decision 0016). In a group you allowed, Tito now sees everything the group says, from everyone:
  - Each message shows who wrote it, once per run, as in WhatsApp. People on Tito's list show by the name Tito knows ("Vovó").
  - Anyone else shows by their WhatsApp name, with a **"Fora dos contatos"** tag.
  - What you write in that group from your phone shows too, under your name.
  - The home preview and the notification say who wrote it ("Vovó: Domingo tem bolo").
  - In a group's settings, the "Conversa só de Tito" switch is replaced by a line saying what Tito sees. The switch stays for people.
- **React to anything.** A long press on a photo, video, GIF or voice message now opens Reagir, as on text. A tap still opens or plays it.
- **What you have to do:** look at the groups in Contatos. Tito will see everything in them. If one shouldn't be whole for Tito, remove it or pause it.

## 0.45.0
- **New messages come first on Tito's home, in the brand coral.**
  - Whoever wrote and hasn't been read yet moves to the top, the newest first, as in WhatsApp.
  - Their card (or row, on a phone) gets a coral edge, and "3 novas" turns coral.
  - Once Tito reads the chat, the person goes back to their place in Tito's order.
  - Arrumar always shows Tito's own order.
- **Phones:** every kid and parent screen was checked on a 360dp phone with 1.3× text. On the home, the rows now line up and have a small gap between them.
- **What you have to do:** nothing.

## 0.44.0
- **A tidier parent area.** "Ajustes e ajuda" mixed changing things with reading about them in one long scroll. Now:
  - **Ajustes** (gear) holds what changes how Oiê works, in this order:
    - **Conexão** comes first, so a dropped link is the first thing you see. "Marcar como não lida" is under it.
    - **Nomes** is now one line ("Ana · Tito (Chico) · 😎"). Tap it to change the names and the emoji, then Voltar.
    - **Bolha**, **Lembrete** and **Novidades** follow.
  - **Ajuda** (info) holds the questions and answers, Problemas and Sobre (version and updates). After Oiê closes by itself, the report still comes first.
  - Conexão is no longer a tab of its own: it's a few lines that now lead Ajustes. You still get there with one tap.
  - The help answers now point to "Ajustes › Novidades" and "Ajustes".
- **What you have to do:** nothing. Everything you set is where the list above says.

## 0.43.0
- **More commands from your own WhatsApp.** In your chat with yourself ("Mensagem para você"):
  - **/Tito contatos** lists your kid's contacts, numbered;
  - **/Tito parar 2**, **voltar 2**, **remover 2** change one by its number (or by name);
  - **/Tito só 2** makes it a conversation that's your kid's own, and **/Tito normal 2** undoes it;
  - **/Tito adicionar**, then share a contact card in the next 10 minutes, adds that person (several cards work);
  - **/Tito silêncio** turns quiet hours on now, until **/Tito liberar**. Your kid sees "Horário de silêncio até (you) liberar", and you can also lift it in Horário de silêncio on the tablet.

  /Tito alone shows them all. Only your WhatsApp can use the commands, and the answers come only to your own chat.
## 0.42.0
- **Groups are back in Adicionar.** You could only find people when adding someone for your kid. Oiê now keeps the list of your groups, so they don't vanish when WhatsApp is slow to answer.
- **Your kid arranges their home.** "Arrumar", next to "Com quem você quer falar?":
  - a heart keeps a favorite on top;
  - arrows, or holding and dragging, change the order.

  Your order in Contatos doesn't change.
- **Conversations that are your kid's own.** In a contact's page in Contatos, turn on **"Conversa só de (your kid)"**, for a friend or a group. Everything in that chat reaches your kid, with no need for their name and no 24-hour limit. Contatos says "Tudo chega para (your kid)" under it. What you write there from your phone still stays in your WhatsApp.
- **Recording like WhatsApp.** Your kid holds the mic to record, as before. Now, sliding up keeps it recording without holding. The mic turns into a stop button: a tap sends it, with the usual few seconds to undo or listen first.
## 0.41.0
- **Gentler on your WhatsApp number.** Oiê now does less of what unofficial apps can get flagged for:
  - **A calmer pace:** at least 4 seconds between your kid's messages, and at most 20 in 10 minutes. In normal use you won't notice.
  - **The unread mark is now your choice.** On your own conversation with your kid, Oiê used to mark the chat as unread after each of their messages, so you'd notice. The official WhatsApp never does that, so it's now off. Turn it on in Ajustes e ajuda › Sua conversa com (your kid) if you want it.
  - **Posting statuses** stays off unless you turn it on, as before.

  Oiê's WhatsApp connection is already on the newest version. Nothing can make an unofficial app risk-free: WhatsApp may still restrict or block numbers that use it.
## 0.40.0
- **Fewer messages reach the tablet by mistake.** Messages that came within 24 hours of your kid's last one used to reach the tablet, even when they weren't for them, like "o Tito já chegou?". Now, in that time:
  - **Talk about your kid stays with you:** "o Tito já chegou?", "como está a Lia?", "manda beijo pro Tito".
  - **When you answer in that chat, it's yours:** what the person says next stays in your WhatsApp, until your kid writes again.

  Replies to your kid's messages, and messages that start with their name ("Tito, …", "Oi Tito"), still always reach them. A message like "o Tito já chegou?" that's already on the tablet leaves the chat.

## 0.39.0
- **Your kid's name on every kind of message.** WhatsApp doesn't let a voice message or a real sticker carry a caption, so the family couldn't tell those came from your kid. Now, before a voice message or a sticker, the name goes on its own line (😎**Tito**, with the emoji you chose), then the audio or the sticker. It goes once per burst: if your kid sends five audios in a row, the name goes once, and again only after 2 minutes. Text, photos, videos, GIFs and Oiê's stickers already carried the name. Reactions stay as they are.

## 0.38.1
- **Clearer about privacy.** Ajuda now says it plainly: Oiê has no servers. The tablet links to your WhatsApp the way WhatsApp itself does (like WhatsApp Web), and messages go only through WhatsApp. There is no analytics, no ads and no tracking, so nothing about your kid or your family reaches us. The only other connection checks GitHub for a new version of Oiê. Nothing in how the app works changed.

## 0.38.0
- **A new home for the kid.** Each person now shows what you last said and when ("Você: Também tô com saudade · 16h35", "Áudio · 1:12 · Ontem"), with "3 novas" and the line in bold when something is unread. People stay in the order you set, so Vovó is always in the same place.
  - **On a phone:** a list, like a chat app.
  - **On the tablet:** cards, each with **Escrever** (opens the chat with the keyboard up) and **Gravar áudio** (opens the chat already recording: tap the microphone to send, or Cancelar). In quiet hours, or while WhatsApp is disconnected, the cards keep only the reading.
- **Anexar instead of Foto.** The chat's photo button is now **Anexar**: Tirar foto, Gravar vídeo, Galeria and Desenhar, with the stickers right below, so a sticker is still two taps. The separate sticker button is gone.
- **Fixed:** leaving a chat while a voice message was recording by tap (TalkBack) left the microphone on. Leaving now throws the recording away.

## 0.37.0
- **The kid posts updates.** Turn it on in Ajustes e ajuda › Novidades › "Tito pode postar novidades" (off until you do; Novidades must be on).
- **What the kid can post:** a photo, a video (up to 1 minute) or a text. The home's Novidades row gets **Postar**, and once the kid has posted, **Você**.
- **Who sees it:** only the people in Contatos, never groups or paused people. Oiê says who before posting ("Quem vê: Vovó, Mãe e Tia Bia").
- **It goes out from your account,** signed with the kid's name. You see it on your phone in Meu status, and you can delete it there.
- **The kid can take it back** with "Apagar para todos". A post that fails says so and offers Tentar de novo.
- **How it stays private:** WhatsApp would send a status to your whole status audience. Oiê changes one file of its WhatsApp library so it goes only to Contatos. That file is published with every release (its licence, the MPL-2.0, asks for it).

## 0.36.0
- **Novidades: the family's WhatsApp statuses.** Off until you turn it on in Ajustes e ajuda › Novidades. Then the kid's home shows a row of faces for the people in Contatos who posted in the last 24 hours, with a coral ring and "nova" until seen.
- **Watching:** photos and texts stay 5 seconds, videos play to the end. Tap the right side for the next, the left for the previous; Pausar holds it.
- **Reacting and answering:** the six reactions, and Responder. Both go to that person's own chat (the reply also shows in the kid's chat, marked "Resposta a uma novidade"). Nothing goes to anyone else.
- **What the family sees:** when the kid watches a status, the person sees that you viewed it, because it's your account. Your own statuses never show, nor anyone outside Contatos. Each status leaves the tablet after 24 hours.
- Posting a status from the tablet comes next (0.37).

## 0.35.0
- **The line to you now shows up on your phone.** WhatsApp never notifies an account of its own messages, and Tito writes from yours. Now, after each message Tito sends you, Oiê uses WhatsApp's own "Marcar como não lida" on that chat: it gets the unread dot and moves to the top of your chats. There's still no sound (WhatsApp doesn't allow it for your own account). Only that chat is ever touched.

## 0.34.0
A design pass over every screen, with the four design skills and anti-slop, in rounds until each area scored its maximum. What you'll notice:
- **Fixed: "Esqueci o PIN" was not safe.** It could let anyone holding the tablet set a new PIN while WhatsApp was linked. Now a new PIN needs the same WhatsApp to link again (a fresh QR, scanned with your phone); the kid's contacts and chats stay. If another WhatsApp links, Oiê starts over.
- **The kid's home** scrolls (people further down were unreachable on a phone), keeps two even columns on a phone, and shows "Vó" and "Vô" when two people share a first letter. A Wi-Fi drop says "Sem internet agora…" instead of asking you to reconnect.
- **Chats** open on the first new message, under a "Novas" line. Recording shows a clock, a Cancelar, a countdown in the last 10 seconds (2 minutes at most), and sliding off the button cancels. A short hint shows how to react, until the kid has reacted once.
- **Setup** is calmer: one title style, Voltar on every step, the QR beside its steps on the tablet, clear lines when linking fails or there's no internet, and your name filled in from WhatsApp.
- **Parent area:** undo is one bar at the bottom that never scrolls away; Back in a quiet period's editor no longer loses it; a phone shows the four sections at once; the 2-minute lock counts typing and scrolling as activity.
- **Everywhere:** buttons press to 97%, sheets close with Back or a swipe down, errors are announced to TalkBack, and dark mode's overlays darken.
- **One word on the kid's side:** "Mandar" (the voice guide's own); the contact list says "Com quem Tito pode falar" (there are no calls).
- **For you to confirm:** the logo's size in the top bar (decision 0012).

## 0.33.0
Ten things for the family, in one update:
- **"Vovó viu".** Under the kid's last message: Mandada, then Entregue, then "Vovó viu" (in a group, "Alguém viu").
- **Reactions.** The family's reactions show under the message. The kid holds a message to react with ❤️ 😂 😮 😢 👍 🙏; the same one again takes it away.
- **Ouvir.** In the same place, the tablet reads a message aloud with its own voice. Nothing is sent anywhere.
- **Listen before it goes.** After recording, the undo bar has Ouvir; the 5 seconds wait while it plays.
- **Desenhar.** The camera button offers a drawing pad. Pronto sends the drawing as a picture.
- **Criar figurinha.** In the stickers, a photo becomes a round sticker, kept with the family ones.
- **Fotos.** In a chat's header, all the photos, videos and GIFs with that person.
- **Birthdays.** In a contact (parent area), set the day and month. On the day, the kid's home says so.
- **"Preciso de você".** In a contact, turn on "Recebe 'Preciso de você'". The kid's home then has that button: it asks once, then sends "Preciso de você." to the people you marked, at most once every 5 minutes, even in quiet hours. It's a way to call you, not an emergency service.
- **Family reminder** (off until you turn it on, in Ajustes e ajuda): if the kid goes a week without writing to someone, the home shows one gentle reminder. "Agora não" hides it for a week.

## 0.32.0
- **Fixed: voice messages arrived broken for the family.** Android's way of saving the recording wasn't one WhatsApp could play. Oiê now re-packs each voice message into the same form WhatsApp's own voice notes use, without changing the sound. If a recording can't be read, it shows as not sent, with Tentar de novo, instead of reaching the family broken.

## 0.31.0
- **WhatsApp's own animated stickers move.** The ones from WhatsApp's sticker store are a different kind of animation (Lottie); now they move in the chat and in the sticker sheet, and a saved one goes back out animated.
- **GIFs and stickers from the keyboard.** In the text field, a GIF or sticker picked in Gboard, or a picture, GIF or video pasted in, goes to its preview: GIFs to the GIF preview, pictures and stickers to the photo preview, videos to the video preview. Nothing is sent until Enviar.
- Animated stickers use Airbnb's Lottie (Apache License 2.0), credited in Sobre.

## 0.30.0
- **The kid can write to you, the parent whose WhatsApp Oiê uses.** In Contatos, "Criar conversa com Tito" creates a group with only you in your WhatsApp, named "Tito". You appear on the kid's screen with your name and picture. What he sends you shows in that group, and what you write there reaches him.
- WhatsApp doesn't notify you of these messages, because they come from your own account. Pin the group at the top to see them.
- If WhatsApp doesn't accept a group with one person, the kid writes in your "Mensagem para você" instead. There, only your replies to his messages, and messages starting with his name, reach him; your notes stay yours.

## 0.29.0
- **A bubble for each chat.** With "Bolha na tela" on, a chat gets its own bubble, with the person's picture, when a message reaches the kid or the kid writes. Two people, two bubbles. Tapping one opens only that chat in a small window, with the keyboard already up to answer. Back folds it away.
- The Oiê bubble stays, for everyone else.
- On a phone-size screen (and in the bubbles), the camera and stickers step aside while the kid types, so the text has room; they come back when the field is empty.

## 0.28.0
- **The kid can send videos**, up to 3 minutes: the camera button now offers "Tirar foto", "Gravar vídeo" and "Galeria". Oiê shrinks the video to 720p on the tablet ("Preparando o vídeo…"), shows it, and sends it with Enviar. A video longer than 3 minutes gets "Escolha um mais curto".
- **The kid can send GIFs** saved on the tablet (from Galeria). Oiê turns them into WhatsApp's GIF form, so they loop in the family's WhatsApp. There's no GIF search: nothing about the kid leaves the tablet.
- Videos are prepared with Google's Media3 (Apache License 2.0), credited in Sobre.

## 0.27.0
- **Animated stickers move**, in the chat and in the sticker sheet.
- **Videos.** A video from family shows its first picture, a ▶ and its length. Tap to watch it big, with sound, with Tocar/Pausar. Videos over 64 MB stay in WhatsApp; the chat says so.
- **GIFs** play in a silent loop in the chat, like in WhatsApp. Tap to see them big.
- With Android's "Remover animações" on, stickers and GIFs stay still.
- Sending videos and GIFs comes next.

## 0.26.0
- **Profile pictures.** Each allowed contact and group now shows their WhatsApp profile picture instead of the letter: on the kid's home, in the chat, and in Contatos. Someone with no picture, or who hides it, keeps the letter.
- Only the contacts you allowed are looked up, at most once a day each, and the pictures are kept sealed on the tablet.

## 0.25.0
- **Oiê in a bubble.** In Ajustes e ajuda › Bolha na tela, turn on "Mostrar a bolha". A round Oiê bubble floats over other apps. Tapping it opens Oiê in a smaller window, like a phone on top of the screen: the family's faces, the chats, everything. Tap outside or Back to fold it away; drag it to the X to remove it.
- Android has to allow bubbles for Oiê. If it doesn't yet, the setting says so and "Permitir bolhas" opens the right Android screen. On Samsung tablets it also shows where Samsung keeps it (Notificações flutuantes › Bolhas).
- Needs Android 11 or newer.

## 0.24.0
- **Fixed: "conectar de novo" right after an update.** After an update or a restart, Oiê takes a few seconds to reconnect by itself, and it used to call that "disconnected". Now Conexão says "Conectando ao WhatsApp de Ana…" with no button, and the kid sees nothing. Only if it still isn't connected after 30 seconds does Oiê say it's disconnected. The kid can keep writing meanwhile; messages go out once it connects.

## 0.23.0
- **Fixed: photos.** Every photo, from the camera or the gallery, said "Não deu para abrir essa foto. Tente outra." It was a mistake in Oiê since the first version: a step that only reads the photo's size gives an empty answer on purpose, and Oiê took that for a failure. Photos now go through.
- Camera photos arrive the right way up.
- **Fixed: WhatsApp's "disk I/O error" in the report.** The WhatsApp connection couldn't save some data on the tablet, because Android has no temporary folder where it looked. It now does that work in memory.
- If a file really isn't a picture, the message still shows, and the problem report says why.

## 0.22.0
- **The kid's name in bold on WhatsApp.** Messages from the tablet now start with the name in WhatsApp's bold: **Tito**: Oi, vó.
- **An emoji before the name.** In Ajustes e ajuda › Nomes, "Emoji antes do nome" takes one emoji from the keyboard, so the family sees 😎**Tito**: Oi, vó. A line under it shows how it will look. Leave it empty for just the bold name.

## 0.21.0
- **"Oi Tito" counts.** A message that starts with a short greeting and then the kid's name now reaches the kid: "Oi Tito", "Bom dia, Tito", "E aí Tito", "Olá Tito". The name in the middle of a sentence still doesn't.
- **Nicknames.** In Ajustes e ajuda › Nomes, "Apelidos" takes as many names as the family uses ("Tito, Chico, Fran"). Any of them works, with or without a greeting. The kid's messages still go out with the main name.
- **Commands from your WhatsApp.** In any chat on your phone, send "/Tito" (or any of the kid's names). Oiê answers in your chat with yourself, with the list:
  - "/Tito parar" pauses that chat: nothing from it reaches the kid, and it leaves the kid's home;
  - "/Tito voltar" brings it back;
  - "/Tito adicionar" adds that chat to the kid's contacts;
  - "/Tito remover" takes it out.
- Only your own WhatsApp can use commands. People in the chat see your command, but not Oiê's answers. A paused chat shows "Pausado pelo seu WhatsApp" in Contatos, and you can resume it there too.

## 0.20.0
- **Fixed: the tablet said "desconectado" while WhatsApp showed it connected.** Each time Oiê came back to the screen, it asked to connect again. When it was already connected, that answer was taken for an error, and Oiê showed "disconnected" over a working link. Now Oiê checks first and leaves a working link alone. If the screen was wrong, it corrects itself when Oiê opens.
- Turning the tablet or coming back to Oiê while the QR is on screen no longer restarts the linking.
- WhatsApp's list of connected devices shows every linked device, even one that is asleep. The tablet's own status is the one that says whether messages are flowing now. Real drops still show, and Oiê reconnects by itself.

## 0.19.0
- **A name for your kid.** In Contatos, tap someone to change the name your kid sees: "Maria Silva" can be "Vovó". In setup, each person you pick has "Mudar nome". The name changes only in Oiê; your WhatsApp stays the same. Leave it blank to go back to the WhatsApp name.
- **The number.** Each person in Contatos and in the lists you add from shows their number, so you can check it's the right one. A renamed person also shows their WhatsApp name. Search finds a number too.
- To remove someone, tap them and use "Remover … dos contatos". Undo is still there.
- About no longer says Oiê uses example data.

## 0.18.0
- In Ajustes e ajuda › Problemas, "Enviar para o GitHub" opens a new issue in the private FamilyZap repo, with the report already filled in. You read it and tap Submit, logged in as yourself. Oiê holds no password and sends nothing on its own. "Copiar relatório" is still there.

## 0.17.0
- After you link, WhatsApp takes a few seconds to a minute to send your contacts. Groups come straight away, so the list used to show only groups. Now setup and Contatos say "Trazendo seus contatos do WhatsApp… N até agora", and the list fills in as they arrive, people first.
- If it takes more than a minute, Oiê says so and opens "Adicionar pelo número". If your WhatsApp has no contacts, it says that. You can always continue and choose later.
- Setup scrolls on small screens, so its buttons are always reachable. A group's "todos do grupo veem as mensagens" sits under its name.

## 0.16.0
- **Fixed: Oiê closing right after linking, and WhatsApp removing the tablet.** Your problem report showed the cause. Right after you scanned the code, Oiê asked for your contacts before any had arrived. The answer was "nothing", and Oiê wasn't ready for that, so it closed in the middle of linking. WhatsApp then saw a half-finished link and dropped the tablet from Dispositivos conectados. It wasn't the photo: it happened at every new link. Now an empty contact list is just empty.
- **Fixed: Oiê closing again and again after that.** Android tried to restart Oiê's background connection by itself, which it doesn't allow on Android 12 and later, and Oiê closed each time. Now it waits quietly until you open Oiê.
- An error in the WhatsApp connection's background work now goes into the problem report instead of closing Oiê.

## 0.15.0
- Oiê went back to the home screen without any message, and the tablet disappeared from WhatsApp's Dispositivos conectados, and not only when sending a photo. That kind of exit wasn't recorded. Now the problem report (Ajustes e ajuda › Problemas) also shows, the next time Oiê opens:
  - a failure inside the WhatsApp connection itself;
  - Android's reason for closing Oiê (for example low memory).
- Nothing else changes on screen.

## 0.14.0
- Sending a photo closed Oiê and logged the tablet out of WhatsApp. This version finds out why, and makes sure it can't happen the same way:
  - **Problem report:** Ajustes e ajuda › Problemas keeps a report on the tablet of what the link did, its errors, and any time Oiê closed by itself. "Copiar relatório" copies it, so you can paste it here. It has no messages or names, phone numbers are masked, and Oiê never sends it anywhere. After a crash, the report comes first in Ajustes e ajuda, and Conexão points to it.
  - **The WhatsApp connection can no longer close Oiê:** an error inside it now becomes "Não foi" on the message instead.
  - **Linking again after WhatsApp logs the tablet out** works straight away. Before, it needed Oiê to be closed first.
  - **Photos are sent the way WhatsApp's own apps send them,** with their size and a small preview.
- On a phone, the parent area's tab row keeps the open section in view.

## 0.13.0
- A full design check of every screen, and its fixes:
  - **Works on a phone:** Tito's chat and the parent area used tablet-wide margins and broke at phone width. Phones now get narrow margins, and the parent area's sections become tabs on top.
  - **Setup buttons** wrap to the next line on a narrow screen instead of squeezing.
  - **In Contatos,** each person's line now says the quiet-hours setting in words ("Só fora do silêncio" or "Pode falar no silêncio"), next to the switch.
  - "Desconectar o WhatsApp" now asks once, because getting back takes a new code.
  - Right after linking, Contatos says "Carregando seus contatos do WhatsApp…" while your contacts arrive.
  - Message times no longer wrap.
- The full report is in `audits/2026-11-19-v0.13-full-audit.md`.

## 0.12.0
- Your contacts now show up. WhatsApp sends them a few seconds after linking; Oiê used to look only once, too early, and found only groups. Now the list fills in as they arrive and refreshes each time you open Contatos. People come first, then groups, with a search.
- Someone missing from the list? Use "Adicionar pelo número" in Contatos: type their name and number, and Oiê checks the number is on WhatsApp.
- The test features are gone: the sample conversations, the "Simular…" buttons and the rule test. Oiê now works only with your real WhatsApp. If the tablet was still on the sample, its sample chats go, and Conexão shows "Conectar o WhatsApp".

## 0.11.0
- Real voice messages. Tito holds the microphone to record and lets go to send; Oiê records in the same format as WhatsApp's own voice messages, so the family hears them as normal voice notes. Each one carries the 5-second Desfazer like any message.
- Voice messages from the family play: tap play, tap again to stop. They are kept sealed on the tablet like photos.
- At the end of setup, Oiê now asks for the microphone together with notifications, while you're there. On a tablet that's already set up, the first time Tito records, Android asks.
- The microphone is back on the real WhatsApp.

## 0.10.0
- Send stickers to Tito from your own WhatsApp: send a sticker to yourself, in the chat with your own name. It shows for Tito under Figurinhas, in "Novas de Ana", and Tito taps Guardar to keep it. Saved ones sit in "Da família", next to Oiê's six, and go out as real WhatsApp stickers.
- Only stickers from your own chat reach Oiê. Nothing else from it is read.
- Works once the real WhatsApp is on. Ajustes e ajuda explains it in "Como mando figurinhas para Tito?".

## 0.9.0
- You can now change your name and Tito's name after setup. In the parent area, the last section is now called Ajustes e ajuda, and the names are at the top. An empty field keeps the last name.
- On the real WhatsApp, Tito's messages carry his name in front ("Tito: Oi, vó"), as setup promised. The family knows it's him writing from your number. A photo or sticker carries his name as its caption.
- Tito can now use your real WhatsApp. Turn it on in Conexão, after the rule test is connected: "Usar o WhatsApp de verdade com Tito". The sample chats go, and you pick from your contacts who Tito can talk to. Long contact lists have a search.
- Until you turn it on, nothing changes: Tito's side keeps the sample.
- A new setup can link your WhatsApp at step 2, with "Usar exemplo por enquanto" to keep trying the sample.
- On the real WhatsApp:
  - Tito sends text, photos and stickers (stickers go as pictures). Each message waits at least 2 seconds after the last, and never goes twice.
  - If a message can't go while the link is down, it's tried once more when the link comes back. If it still doesn't go, Tito sees "Não foi" and "Tentar de novo".
  - Messages from people you didn't allow, and your own messages, never reach the tablet.
  - Family photos, stickers and voice messages arrive. Playing voice messages comes in the next version.
  - Recording voice messages comes in the next version too, so the microphone is hidden on the real WhatsApp for now.
- If WhatsApp unlinks the tablet, Conexão shows "Conectar de novo" with a new code.
- (0.8.0 was never released on its own: it ships inside 0.9.0.)

## 0.7.0
- The rule test now keeps listening when Oiê is closed or the tablet is asleep, so it can collect a whole day of messages. A quiet notification, "O Oiê está conectado.", shows while it listens. You can hide it in Android's settings, under Conexão com o WhatsApp.
- If the Wi-Fi drops, Oiê connects again by itself when it comes back. If WhatsApp blocks the number, it stops and doesn't try again on its own.
- Deleting the test stops the listening and the notification.

## 0.6.0
- Smaller updates: the tablet now downloads the version made for its processor, about 21 MB instead of 40 MB. This update is still the big one; the next ones are smaller.
- In the rule test, a photo or sticker that would reach your kid now shows in the list, the way your kid would see it. Voice messages show how long they are.
- Behind the scenes, the WhatsApp link can now receive and send photos and voice messages, at a person's pace and never twice. Your kid's side doesn't use it yet.

## 0.5.0
- Test the message rule with your real WhatsApp. In the parent area, open Conexão and tap Testar a regra com seu WhatsApp.
- It links your WhatsApp to read only: nothing is sent.
- Pick a few chats. Each new message shows whether it would reach your kid ("Chegaria") or stay with you ("Fica com você"), and why.
- Only the text of messages that would reach your kid is kept. Messages from other chats are only counted.
- "Fingir que Tito falou agora" tries the 24-hour part of the rule.
- Desconectar e apagar o teste logs out and deletes the test from the tablet.
- The kid's side still uses sample data.
- The app is bigger (about 40 MB), because it now carries the WhatsApp link.
- **The WhatsApp link, chosen and tried:**
  - It uses whatsmeow (decision 0003). A spike in `link/` builds it for Android and opens its session store.
  - The rules that keep the link from looking like a bot (L1–L9) are in `docs/whatsapp-link.md`. The first one, human pace (`SendPace`), is in the app with tests.
  - Nothing is wired in yet: the app still uses sample data.
- **Updates without a tag:** merging a version bump to `main` now publishes the release by itself, so the tablet offers it at the next check. Release notes come from this file's `## <version>` section (`docs/release.md`).

## 0.4.0
- Receiving (flow "Kid receives messages").
- **One way in:** every message from the WhatsApp link goes through `Inbox.decide`. Messages from chats that aren't allowed, and the parent's private messages, are **never stored** on the tablet. The rest is shown.
- **Notifications:** Oiê's own channel, one per family member, with the kind in words ("Mandou um áudio"). Tapping one opens that chat. Permission is asked at the end of setup, after saying why.
- **Quiet hours:** messages still arrive quietly, so the kid can read them, and become one summary notification when the period ends.
- **Delete for everyone:** the message turns into "Mensagem apagada" and its photo is dropped.
- **Test only:** "Simular mensagem da Vovó (teste)" in Conexão, until the real link exists.
- 59 tests.

## 0.3.0
- Self-update (the Gabriel Design System's rule, `docs/self-update.md`).
- **The check:** on every return to the foreground, at most every 15 minutes, with ETag. It asks GitHub's public API for the latest release and sends nothing about the family.
- **Install rules:** only a newer, non-prerelease release with an `Oie-*.apk` and `SHA256SUMS.txt` is offered. The download installs only if it's complete, its SHA-256 matches, and it's the same package signed with the same key. Then Android asks to confirm.
- **Where it shows:** the "Atualização disponível" sheet appears **only in the parent area**, not to the kid (proposed, for the owner to confirm). About has the version and "Procurar atualizações". A debug copy says it can't update itself.
- **New permissions:** `INTERNET` and `REQUEST_INSTALL_PACKAGES`, for self-update only.
- **Releases:** `.github/workflows/release.yml` builds a signed APK and `SHA256SUMS.txt` on a `v*` tag, once the owner adds the signing secrets (`docs/release.md`).
- **Tests:** portrait screenshots and the update rules (version compare, offer, sums, checks, throttle, sizes). 48 tests.
- **Owner decisions, built:**
  - "Pedir para adicionar alguém" on the kid's empty home. The parent sees "{kid} pediu para adicionar alguém" in Contatos. No names and no typing, so it can't reach anyone.
  - Self-update reads the public `gabrielbr/oie-releases`, while the code stays private.
  - The update sheet stays in the parent area only (design system divergence D-14).
  - The six sticker words are approved.

## 0.2.0
- **Sticker images:** the six stickers are real images. Their words are outlined with Nunito (`_02` masters) and converted to Android vectors by `tools/svg2vector.py`.
- **Quiet hours in the parent area:** add, edit (name, days, start and end with a time picker) and delete with undo.
- **Ajuda e sobre:** short answers to eight questions the flows raise, the risk notice repeated, version, "not made or approved by WhatsApp or Meta", and the licences.
- **Photo files encrypted:** each photo is sealed under its own Keystore key.
- **English:** every word lives in one typed `Strings` with Portuguese (the source) and English, picked from the device or per-app language. Tests check the voice rules (no "!", no em dash, no Zap/Wpp) in both languages.

## 0.1.0
- An Android app (`app/`, Jetpack Compose, `app.oie`) for the kid's tablet. It has setup with the risk notice, a PIN, the kid home and conversation (text, hold-to-record voice, stickers, 5 s undo), and the parent area (contacts with undo, quiet-hours exceptions, connection, about). It works only with **sample data**: there is no real WhatsApp link yet. The message rule, quiet hours and PIN rules are tested, Roborazzi screenshot tests cover the screens, and CI builds and tests on every PR. Nunito is bundled (OFL, `licenses/`).
- **Photo sending:** camera or gallery through Android's own camera app and photo picker, so Oiê asks for no permission. The last source used comes first. A preview offers Trocar and Enviar, undo returns to the preview, and the photo shows in the chat. Photos are re-encoded on the way in (at most 1600 px), which drops the camera's metadata, including location.
- **Saving on the tablet:** the whole state (setup, names, the PIN hash, contacts, quiet hours, messages, drafts) is saved as it changes. It is sealed with AES-GCM under a key kept in the Android Keystore, in the app's private storage; nothing goes to a server. Setup resumes at the same step; the parent area always asks for the PIN again. A damaged file starts fresh instead of crashing.
- Tests: 30 in total (6 message rule, 7 quiet hours and PIN, 4 saving, 13 screenshots). The screenshots use a pinned clock and time zone so CI can compare them.

## Before the app
- Project started from the Gabriel Product Boilerplate.
- Stage 1: keywords (warm, simple, trustworthy, close) and references (Headspace, Duolingo) added to the brief.
- Stage 1: research on how to reach WhatsApp, with a decision (0001): linked-device bridge on the kid's tablet, no calls in v1. Stage 1 done.
- Stage 2 done: strategy (Family, within reach · Send love home) and the name Oiê (decision 0002), replacing the working name FamilyZap. Project code is now OIE.
- Stage 3: three logo directions; direction C (Close) approved; logo pack in `01 Brand/logo/` in coral #E8573C.
- Stage 3: voice (`01 Brand/voice.md`) and imagery and icons (`01 Brand/imagery-and-icons.md`). Stage 3 done.
- Stage 4: Oiê builds on the Gabriel Design System core 0.31.1 (`design/`), with its own brand (coral, Nunito) and layer. Generated `OTokens.kt` and `tokens.css`; design system README and AGENTS.md brand rules filled. Stage 4 done.
- Stage 5: flow "Kid sends a message" (`docs/flows/kid-sends-message.md`). Icons now follow the design system's line set.
- Stage 5: flow "Parent setup + link" (`docs/flows/parent-setup-link.md`). Oiê is now one app on the kid's tablet with a parent area behind a PIN; the parent's phone only scans the QR in WhatsApp.
- Stage 5: flows "Choose contacts + quiet hours" and "Kid receives messages"; tablet prototype in `03 Product/prototype/` (8 screens, light and dark); tokens `qr-ground`/`qr-ink`.
- Stage 6: the full five-skill audit of the v0.1 prototype.
- Owner decisions: the message rule (to test), PIN wait, idle lock, presets, groups. Stages 5 and 6 done.
- Prototype v0.2: 13 more screens (PIN, setup 3–6, done, add contact, recording, photo preview, stickers, undo, offline, link dropped), built by `03 Product/prototype/build.py`; 6 proposed stickers in `01 Brand/stickers/`; the core icons Oiê uses are vendored in `design/core/icons/` (core 0.32.0).
