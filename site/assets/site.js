/* Oiê landing page v2: the visitor's family, the scrolling story, and the PT/EN switch (decision 0013). */
(function () {
  "use strict";

  /* ---------- People: many receivers, friends too ---------- */
  const PEOPLE = {
    pt: {
      kid: { son: { name: "Tito", sil: "son" }, daughter: { name: "Lia", sil: "daughter" } },
      parent: { mother: { name: "Ana", sil: "mother" }, father: { name: "Paulo", sil: "father" } },
      recv: {
        grandma: { name: "Vovó", init: "Vó", the: "a Vovó", of: "da Vovó", cap: "no WhatsApp dela", msg: "Oi vó, tô com saudade", reply: "Também, meu amor. Domingo tem bolo." },
        grandpa: { name: "Vovô", init: "Vô", the: "o Vovô", of: "do Vovô", cap: "no WhatsApp dele", msg: "Oi vô, me ensina a pescar?", reply: "Ensino sim. Domingo bem cedo." },
        aunt: { name: "Tia Bia", init: "T", the: "a Tia Bia", of: "da Tia Bia", cap: "no WhatsApp dela", msg: "Tia, vem no meu aniversário?", reply: "Vou sim, e levo o bolo." },
        friend: { name: "Lucas", init: "L", the: "o Lucas", of: "do Lucas", cap: "amigo da escola", msg: "Bora jogar depois da aula?", reply: "Bora. Te espero na quadra." },
      },
      story: {
        s1h: "{Kid} quer falar com {rThe}.",
        s1p: "No tablet, {Kid} vê só as pessoas que você escolheu. Um toque na foto, e é só escrever, gravar um áudio ou mandar uma foto.",
        s2h: "A mensagem sai do seu WhatsApp.",
        s2p: "O Oiê manda pelo WhatsApp do seu celular, com o nome de {Kid} na frente. Ela fica no seu celular também: você vê tudo o que sai.",
        s3h: "E chega no WhatsApp {rOf}.",
        s3p: "Para {rThe}, é uma mensagem sua, como qualquer outra. Ninguém precisa baixar nada.",
        s4h: "{RThe} responde como sempre.",
        s4p: "A resposta volta pelo seu WhatsApp e aparece no tablet de {Kid}, na conversa com {rThe}.",
        s5h: "Não é só {rThe}.",
        s5p: "Avós, tios, primos e os amigos da escola. Qualquer pessoa que você aprovar, e mais ninguém.",
        s6h: "Você escolhe quem.",
        s6p: "Numa área com PIN, no próprio tablet: os contatos, o horário de silêncio e pausar uma conversa. No seu celular, você só lê um QR, uma vez.",
        capKid6: "na área dos pais",
      },
    },
    en: {
      kid: { son: { name: "Tito", sil: "son" }, daughter: { name: "Lia", sil: "daughter" } },
      parent: { mother: { name: "Ana", sil: "mother" }, father: { name: "Paulo", sil: "father" } },
      recv: {
        grandma: { name: "Grandma", init: "G", the: "Grandma", of: "Grandma’s", cap: "in her WhatsApp", msg: "Hi Grandma, I miss you", reply: "Me too, sweetheart. There’s cake on Sunday." },
        grandpa: { name: "Grandpa", init: "Gp", the: "Grandpa", of: "Grandpa’s", cap: "in his WhatsApp", msg: "Grandpa, will you teach me to fish?", reply: "I will. Sunday, bright and early." },
        aunt: { name: "Aunt Bia", init: "B", the: "Aunt Bia", of: "Aunt Bia’s", cap: "in her WhatsApp", msg: "Will you come to my birthday?", reply: "I will, and I’ll bring the cake." },
        friend: { name: "Lucas", init: "L", the: "Lucas", of: "Lucas’s", cap: "a school friend", msg: "Want to play after school?", reply: "Sure. See you at the court." },
      },
      story: {
        s1h: "{Kid} wants to talk to {rThe}.",
        s1p: "On the tablet, {Kid} sees only the people you chose. One tap on a face, then write, record a voice note or send a photo.",
        s2h: "The message goes out from your WhatsApp.",
        s2p: "Oiê sends it through the WhatsApp on your phone, with {Kid}’s name in front. It stays on your phone too: you see everything that goes out.",
        s3h: "And it arrives in {rOf} WhatsApp.",
        s3p: "To {rThe}, it’s a message from you, like any other. Nobody needs to install anything.",
        s4h: "{RThe} replies as usual.",
        s4p: "The reply comes back through your WhatsApp and shows on {Kid}’s tablet, in the chat with {rThe}.",
        s5h: "It’s not only {rThe}.",
        s5p: "Grandparents, uncles and aunts, cousins and school friends. Anyone you approve, and no one else.",
        s6h: "You choose who.",
        s6p: "In a PIN-protected area on the tablet itself: contacts, quiet hours and pausing a chat. On your phone, you only scan a QR code, once.",
        capKid6: "in the parents’ area",
      },
    },
  };
  const ORDER = ["grandma", "grandpa", "aunt", "friend"];

  const EN = {
    skip: "Skip to the story", langGroup: "Language", navGet: "Download",
    h1: "Family, within reach.",
    lede: "From a tablet, your kid talks with the family and friends you choose, through your WhatsApp.",
    cta: "Download Oiê", ctaNote: "For Android tablets, version 8 or newer.", ctaHow: "How to install",
    familyTitle: "Build your family and see how it works", lgKid: "Who writes", kidSon: "Son", kidDaughter: "Daughter",
    lgParent: "Whose WhatsApp", parMother: "Mom", parFather: "Dad",
    lgRecv: "To whom", rcvGrandma: "Grandma", rcvGrandpa: "Grandpa", rcvAunt: "Aunt", rcvFriend: "Friend", familyGo: "See the story",
    storyA11y: "How it works", example: "Example", sent: "Sent", parentArea: "Contacts", waField: "Message", typing: "typing…",
    capKid: "on the tablet, with Oiê", capParent: "your WhatsApp, the bridge",
    auntName: "Aunt Bia", friendName: "Lucas, from school",
    honH: "No fine print.", honBox: "Before linking, Oiê tells you:",
    risk: "Oiê uses your WhatsApp in a way WhatsApp doesn’t officially offer. WhatsApp may limit or block your number. If it does, you lose access until you sort it out with WhatsApp.",
    riskWhy: "To lower that risk, Oiê sends at a person’s pace and never sends in bulk.",
    f1: "<b>Only the people you approve.</b> Family or friends: your kid talks to no one else, and no one else reaches them through Oiê.",
    f2: "<b>You see what goes out.</b> Messages go out from your WhatsApp, so they’re on your phone too. Your kid is told this the first time.",
    f3: "<b>Nothing is kept by us.</b> Messages and contacts stay on the tablet. No ads, and no tracking of kids.",
    f4: "<b>Not made by WhatsApp.</b> Oiê isn’t made or approved by WhatsApp or Meta. And it doesn’t replace your attention.",
    faqH: "Questions from the family",
    q1: "Does the person receiving need to install anything?", a1: "No. Grandparents, aunts or friends get it in their WhatsApp, like any message from you, with your kid’s name in front.",
    q2: "Can my kid talk to friends?", a2: "Yes, if you add the contact. The friend gets it from your number, with your kid’s name in front. Your kid only talks to people you approve, and no one else reaches them through Oiê.",
    q3: "Does my kid need a phone?", a3: "No. Oiê runs on an Android tablet you already have and uses your WhatsApp.",
    q4: "Can WhatsApp block my number?", a4: "It can. Oiê uses your WhatsApp in a way WhatsApp doesn’t officially offer. That’s why we tell you before linking. Oiê sends at a person’s pace and never in bulk.",
    q5: "Do I see what my kid sends?", a5: "Yes. Messages go out from your WhatsApp, so they’re on your phone too. Your kid is told this the first time.",
    q6: "Can they see and post status updates?", a6: "Yes, if you turn it on in settings. Your kid sees updates from their contacts, and only those people see what your kid posts.",
    q7: "Can they make calls?", a7: "Not yet. For now: messages, voice notes, photos, videos and stickers.",
    q8: "Why isn’t it on the Play Store?", a8: "For now Oiê is downloaded straight from its releases page. Each release comes with a SHA-256 sum to check the file, and the app tells you when there’s a new version.",
    getH: "Ready for the first hello?",
    g1: "On your kid’s tablet, tap Download Oiê and open the file.", g2: "If Android asks, allow installing apps from the browser.",
    g3: "Open Oiê: your name, your kid’s, and a QR code you scan in your WhatsApp.", g4: "Choose the contacts, family or friends. Your kid can send the first hello.",
    getSmall: "For Android 8 or newer. The file comes from Oiê’s releases page on GitHub, with each release’s SHA-256 sum.",
    footLine: "Family, within reach.", footAll: "All releases", footLib: "whatsmeow (MPL-2.0)", footPatch: "The whatsmeow file Oiê modifies",
    footLegal: "Oiê isn’t made or approved by WhatsApp or Meta. WhatsApp is a trademark of Meta. The WhatsApp screens on this page are example drawings.",
    title: "Oiê: family within reach, from your kid’s tablet",
    desc: "From a tablet, your kid talks with the family and friends you choose, through your WhatsApp. For Android tablets.",
  };

  const PT = {};
  document.querySelectorAll("[data-t]").forEach((el) => (PT[el.dataset.t] = el.textContent));
  document.querySelectorAll("[data-t-html]").forEach((el) => (PT[el.dataset.tHtml] = el.innerHTML));
  document.querySelectorAll("[data-t-aria]").forEach((el) => (PT[el.dataset.tAria] = el.getAttribute("aria-label")));
  PT.title = document.title;
  PT.desc = document.querySelector('meta[name="description"]').content;

  let lang = "pt";
  const pick = { kid: "son", parent: "mother", recv: "grandma" };
  let shownRecv = "grandma"; // who the receiver's phone shows right now (step 5 cycles through the others)
  let step = 0;

  const $ = (s) => document.querySelector(s);
  const all = (s) => document.querySelectorAll(s);
  const cap1 = (t) => t.charAt(0).toUpperCase() + t.slice(1);
  const esc = (t) => t.replace(/&/g, "&amp;").replace(/</g, "&lt;");
  const dict = () => (lang === "en" ? EN : PT);
  function store(k, v) { try { localStorage.setItem(k, v); } catch (e) { /* private mode: fine */ } }
  function stored(k) { try { return localStorage.getItem(k); } catch (e) { return null; } }

  /* ---------- Filling the page ---------- */
  function fillStatic() {
    const d = dict();
    document.documentElement.lang = lang === "en" ? "en" : "pt-BR";
    all("[data-t]").forEach((el) => { if (d[el.dataset.t] != null) el.textContent = d[el.dataset.t]; });
    all("[data-t-html]").forEach((el) => { if (d[el.dataset.tHtml] != null) el.innerHTML = d[el.dataset.tHtml]; });
    all("[data-t-aria]").forEach((el) => { if (d[el.dataset.tAria] != null) el.setAttribute("aria-label", d[el.dataset.tAria]); });
    document.title = d.title;
    $('meta[name="description"]').content = d.desc;
    all(".lang button").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.lang === lang)));
  }

  function fillPeople() {
    const P = PEOPLE[lang];
    const kid = P.kid[pick.kid], parent = P.parent[pick.parent], r = P.recv[shownRecv], chosen = P.recv[pick.recv];
    const v = {
      kidName: kid.name, parentName: parent.name, parentInitial: parent.name.charAt(0),
      recvName: r.name, recvInitial: r.init, recvCap: r.cap, msg: r.msg, reply: r.reply,
    };
    ORDER.forEach((k, i) => { v["tile" + i] = P.recv[k].init; v["tile" + i + "n"] = P.recv[k].name; });
    all("[data-v]").forEach((el) => { if (el.dataset.v in v && el.id !== "composeKid") el.textContent = v[el.dataset.v]; });
    $("#silKid").setAttribute("href", "#p-" + (step === 6 ? parent.sil : kid.sil));
    $("#silParent").setAttribute("href", "#p-" + parent.sil);
    $("#silRecv").setAttribute("href", "#p-" + shownRecv);
    // The story's words follow the family the visitor built (the chosen receiver, not the one cycling in step 5).
    const words = { Kid: kid.name, rThe: chosen.the, RThe: cap1(chosen.the), rOf: chosen.of };
    all("[data-s]").forEach((el) => { el.textContent = P.story[el.dataset.s].replace(/\{(\w+)\}/g, (m, k) => (k in words ? words[k] : m)); });
    // In step 6 the parent holds the tablet (the parents' area).
    const kidCap = $(".actor--kid figcaption");
    kidCap.querySelector("b").textContent = step === 6 ? parent.name : kid.name;
    kidCap.querySelector("span").textContent = step === 6 ? P.story.capKid6 : dict().capKid;
  }

  /* ---------- The story ---------- */
  const stage = $("#stage");
  const dot = $("#dot");
  const composeKid = $("#composeKid");
  const composeRecv = $("#composeRecv");
  const waTyping = $("#waTyping");
  const still = window.matchMedia("(prefers-reduced-motion: reduce)");
  let typer = null;
  let cycler = null;
  const CHAR_MS = 70;

  function type(el, text, done) {
    clearInterval(typer);
    if (still.matches) { el.textContent = text; if (done) done(); return; }
    let n = 0;
    el.classList.add("is-typing");
    typer = setInterval(() => {
      n += 1;
      el.innerHTML = esc(text.slice(0, n)) + '<span class="caret"></span>';
      if (n >= text.length) { clearInterval(typer); el.textContent = text; if (done) done(); }
    }, CHAR_MS);
  }

  // Where the message is on the wire: 0 the kid, 0.5 the parent, 1 the receiver; null hides it.
  function moveDot(to) {
    if (to == null) { dot.style.opacity = "0"; return; }
    dot.style.opacity = "1";
    dot.style.left = to * 100 + "%";
  }

  function setStep(n, focus, force) {
    if (!force && n === step && stage.dataset.focus === focus) return;
    step = n;
    stage.dataset.step = String(n);
    stage.dataset.focus = focus || "kid";
    all(".step").forEach((s) => s.classList.toggle("is-on", Number(s.dataset.step) === n));
    clearInterval(typer); clearInterval(cycler);
    stage.removeAttribute("data-replied");
    waTyping.hidden = true;
    composeRecv.classList.remove("is-typing");
    composeRecv.textContent = dict().waField;
    shownRecv = pick.recv;
    fillPeople();
    const r = PEOPLE[lang].recv[pick.recv];
    composeKid.textContent = "";
    if (n === 1) { moveDot(0); type(composeKid, r.msg); }
    else if (n === 2) { moveDot(0.5); }
    else if (n === 3) { moveDot(1); }
    else if (n === 4) {
      moveDot(1);
      waTyping.hidden = false;
      type(composeRecv, r.reply, () => {
        waTyping.hidden = true;
        composeRecv.classList.remove("is-typing");
        composeRecv.textContent = dict().waField;
        stage.setAttribute("data-replied", "");
        moveDot(0);
      });
    } else if (n === 5) {
      moveDot(null);
      if (!still.matches) {
        let i = ORDER.indexOf(pick.recv);
        cycler = setInterval(() => { i = (i + 1) % ORDER.length; shownRecv = ORDER[i]; fillPeople(); }, 2200);
      }
    } else { moveDot(null); }
  }

  // On a phone the stage pins over the top 62% of the screen; the step being read is the one in the band below it.
  const narrow = window.matchMedia("(max-width: 899px)");
  function watchSteps() {
    if (!("IntersectionObserver" in window)) { setStep(4, "recv", true); return; }
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => { if (e.isIntersecting) setStep(Number(e.target.dataset.step), e.target.dataset.focus); });
    }, { rootMargin: narrow.matches ? "-72% 0px -18% 0px" : "-45% 0px -45% 0px" });
    all(".step").forEach((s) => io.observe(s));
    // Back at the top (the hero), the stage rests at step 0: everyone standing, nothing sent.
    new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) setStep(0, "kid");
    }).observe($(".hero"));
  }

  /* ---------- Controls ---------- */
  const family = $("#family");
  family.addEventListener("change", (e) => {
    const t = e.target;
    if (!(t.name in pick)) return;
    pick[t.name] = t.value;
    store("oie-family", JSON.stringify(pick));
    setStep(step, stage.dataset.focus, true);
  });
  family.addEventListener("submit", (e) => e.preventDefault());
  all(".lang button").forEach((b) => b.addEventListener("click", () => {
    lang = b.dataset.lang === "en" ? "en" : "pt";
    store("oie-lang", lang);
    fillStatic();
    setStep(step, stage.dataset.focus, true);
  }));

  function start() {
    try {
      const saved = JSON.parse(stored("oie-family") || "null");
      if (saved) Object.keys(pick).forEach((k) => {
        const input = saved[k] && family.querySelector(`input[name="${k}"][value="${saved[k]}"]`);
        if (input) { pick[k] = saved[k]; input.checked = true; }
      });
    } catch (e) { /* fine */ }
    const wanted = new URLSearchParams(location.search).get("lang") || stored("oie-lang");
    lang = wanted === "en" ? "en" : "pt";
    fillStatic();
    setStep(0, "kid", true);
    watchSteps();
  }
  start();
})();
