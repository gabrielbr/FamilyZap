/* Oiê landing page: the family line (hero animation), the PT/EN switch, and section entrances. */
(function () {
  "use strict";

  /* ---------- Language ---------- */
  const EN = {
    skip: "Skip to the content", langGroup: "Language", navGet: "Download",
    h1: "Family, within reach.",
    lede: "From a tablet, your kid sends messages, voice notes and photos to the family through your WhatsApp. Only to the people you choose.",
    cta: "Download Oiê", ctaNote: "For Android tablets, version 8 or newer.", ctaHow: "How to install",
    routeA11y: "Example of how it works: Tito writes “Hi Grandma, I miss you” on the tablet. The message goes out from your WhatsApp, with Tito’s name, and reaches Grandma’s WhatsApp. Grandma replies, and the reply comes back to the tablet.",
    st0: "writes on the tablet", st1name: "Your WhatsApp", st1: "sends it, with Tito’s name", st2name: "Grandma", st2: "gets it in her WhatsApp",
    example: "Example", pause: "Pause", play: "Play",
    kidH: "Your kid taps a face and talks.",
    kidLede: "The tablet shows only the people you chose. One tap opens the chat, like any family chat.",
    w1: "Voice", w2: "Text", w3: "Photo and video", w4: "Stickers", w5: "Updates (status)",
    exKid: "Example: Tito’s tablet", n1: "Grandma", n2: "Mom", n3: "Grandpa", n4: "Aunt Bia", new3: "3 new",
    youH: "You choose who.",
    youLede: "On your phone you scan a QR code, once. Everything else you set on the tablet, behind a PIN.",
    c1h: "Only the people you choose", c1: "Your kid talks to no one else, and no one else can reach them.",
    c2h: "Quiet hours", c2: "School and bedtime, with exceptions for whoever you want.",
    c3h: "Pause a chat", c3: "When you need a break, without deleting anything.",
    c4h: "Settings behind a PIN", c4: "Contacts and settings sit behind a 6-digit PIN.",
    famH: "The family doesn’t install anything.",
    famLede: "To Grandma it’s a message from your number, with Tito’s name in front. She replies as usual, and the reply arrives on the tablet.",
    exFam: "Example: what Grandma gets", ana: "Ana (you)",
    m1: "<b>Tito</b>: Hi Grandma, I miss you", m2: "Me too, sweetheart. There’s cake on Sunday.", m2meta: "Grandma · 4:32 pm",
    honH: "No fine print.", honBox: "Before linking, Oiê tells you:",
    risk: "Oiê uses your WhatsApp in a way WhatsApp doesn’t officially offer. WhatsApp may limit or block your number. If it does, you lose access until you sort it out with WhatsApp.",
    riskWhy: "To lower that risk, Oiê sends at a person’s pace and never sends in bulk.",
    f1: "<b>Nothing is kept by us.</b> Messages and contacts stay on the tablet. No ads, and no tracking of kids.",
    f2: "<b>You see what goes out.</b> Messages go out from your WhatsApp, so they’re on your phone too. Your kid is told this the first time.",
    f3: "<b>Not made by WhatsApp.</b> Oiê isn’t made or approved by WhatsApp or Meta.",
    f4: "<b>It doesn’t replace you.</b> Oiê helps the family talk. Your attention is still what matters most.",
    faqH: "Questions from the family",
    q1: "Does Grandma need to install anything?", a1: "No. She gets it in her WhatsApp, like any message from you, with your kid’s name in front.",
    q2: "Does my kid need a phone?", a2: "No. Oiê runs on an Android tablet you already have and uses your WhatsApp.",
    q3: "Can WhatsApp block my number?", a3: "It can. Oiê uses your WhatsApp in a way WhatsApp doesn’t officially offer. That’s why we tell you before linking. Oiê sends at a person’s pace and never in bulk.",
    q4: "Do I see what my kid sends?", a4: "Yes. Messages go out from your WhatsApp, so they’re on your phone too. Your kid is told this the first time.",
    q5: "Can they see and post status updates?", a5: "Yes, if you turn it on in settings. Your kid sees updates from their contacts, and only those people see what your kid posts.",
    q6: "Can they make calls?", a6: "Not yet. For now: messages, voice notes, photos, videos and stickers.",
    q7: "Why isn’t it on the Play Store?", a7: "For now Oiê is downloaded straight from its releases page. Each release comes with a SHA-256 sum to check the file, and the app tells you when there’s a new version.",
    getH: "Ready for the first hello?",
    s1: "On your kid’s tablet, tap Download Oiê and open the file.", s2: "If Android asks, allow installing apps from the browser.",
    s3: "Open Oiê: your name, your kid’s, and a QR code you scan in your WhatsApp.", s4: "Choose the contacts. Your kid can send the first hello.",
    getSmall: "For Android 8 or newer. The file comes from Oiê’s releases page on GitHub, with each release’s SHA-256 sum.",
    footLine: "Family, within reach.", footAll: "All releases", footLib: "whatsmeow (MPL-2.0)", footPatch: "The whatsmeow file Oiê modifies",
    footLegal: "Oiê isn’t made or approved by WhatsApp or Meta. WhatsApp is a trademark of Meta.",
    title: "Oiê: family within reach, from your kid’s tablet",
    desc: "From a tablet, your kid sends messages, voice notes and photos to the family through your WhatsApp, only to the people you choose. For Android tablets.",
  };
  // The story, beat by beat. Tito's message stays on top; Vovó's reply types in under it.
  const STORY = {
    pt: {
      tito: "Oi vó, tô com saudade", reply: "Também, meu amor. Domingo tem bolo.", who: "Vovó", seen: "Vovó viu",
      heads: ["Tito escreve no tablet", "Sai do seu WhatsApp", "Chega para a Vovó", "Vovó está respondendo…", "Volta pelo seu WhatsApp", "Chega no tablet do Tito"],
      replied: "Vovó respondeu",
    },
    en: {
      tito: "Hi Grandma, I miss you", reply: "Me too, sweetheart. There’s cake on Sunday.", who: "Grandma", seen: "Grandma saw it",
      heads: ["Tito types on the tablet", "Goes out from your WhatsApp", "Reaches Grandma", "Grandma is replying…", "Back through your WhatsApp", "Reaches Tito’s tablet"],
      replied: "Grandma replied",
    },
  };
  // at: the station; signed: "Tito:" in front (added as it leaves your WhatsApp); type: who is typing in this beat.
  const BEATS = [
    { at: 0, signed: false, type: "tito" },
    { at: 1, signed: true },
    { at: 2, signed: true, seen: true },
    { at: 2, signed: true, seen: true, reply: true, type: "reply" },
    { at: 1, signed: true, seen: true, reply: true },
    { at: 0, signed: true, seen: true, reply: true },
  ];
  // How long each beat stays once it's fully shown (travel takes 1.8 s of it).
  const HOLD = [1800, 3400, 3600, 2600, 3400, 4200];
  const CHAR_MS = 85;

  const PT = {};
  document.querySelectorAll("[data-i18n]").forEach((el) => (PT[el.dataset.i18n] = el.textContent));
  document.querySelectorAll("[data-i18n-html]").forEach((el) => (PT[el.dataset.i18nHtml] = el.innerHTML));
  document.querySelectorAll("[data-i18n-aria]").forEach((el) => (PT[el.dataset.i18nAria] = el.getAttribute("aria-label")));
  PT.title = document.title;
  PT.desc = document.querySelector('meta[name="description"]').content;
  PT.play = "Continuar";

  let lang = "pt";
  function store(v) { try { localStorage.setItem("oie-lang", v); } catch (e) { /* private mode: fine */ } }
  function stored() { try { return localStorage.getItem("oie-lang"); } catch (e) { return null; } }

  function setLang(next, save) {
    lang = next === "en" ? "en" : "pt";
    const d = lang === "en" ? EN : PT;
    document.documentElement.lang = lang === "en" ? "en" : "pt-BR";
    document.querySelectorAll("[data-i18n]").forEach((el) => { if (d[el.dataset.i18n] != null) el.textContent = d[el.dataset.i18n]; });
    document.querySelectorAll("[data-i18n-html]").forEach((el) => { if (d[el.dataset.i18nHtml] != null) el.innerHTML = d[el.dataset.i18nHtml]; });
    document.querySelectorAll("[data-i18n-aria]").forEach((el) => { if (d[el.dataset.i18nAria] != null) el.setAttribute("aria-label", d[el.dataset.i18nAria]); });
    document.title = d.title;
    document.querySelector('meta[name="description"]').content = d.desc;
    document.querySelectorAll(".lang button").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.lang === lang)));
    setPauseLabel();
    if (save) { layout(); show(beat, false); store(lang); }
  }

  /* ---------- The family line ---------- */
  const route = document.getElementById("route");
  const plate = document.getElementById("plate");
  const head = document.getElementById("plateFrom");
  const text = document.getElementById("plateText");
  const seen = document.getElementById("plateSeen");
  const seenText = document.getElementById("plateSeenText");
  const replyRow = document.getElementById("plateReply");
  const replyWho = document.getElementById("replyWho");
  const replyText = document.getElementById("replyText");
  const track = route.querySelector(".route__track");
  const stations = Array.from(route.querySelectorAll(".station"));
  const pauseBtn = document.getElementById("pause");
  const drop = document.getElementById("drop");
  const still = window.matchMedia("(prefers-reduced-motion: reduce)");

  let beat = 0;
  let typed = -1; // characters typed in this beat; -1 when the beat has no typing or it's done
  let timer = null;
  let userPaused = false;
  let visible = true;

  const esc = (t) => t.replace(/&/g, "&amp;").replace(/</g, "&lt;");
  const sign = (t) => "<b>Tito</b>: " + esc(t);
  const CARET = '<span class="caret"></span>';

  function ringCenter(i) {
    const r = stations[i].querySelector(".station__ring").getBoundingClientRect();
    const box = route.getBoundingClientRect();
    return { x: r.left - box.left + r.width / 2, y: r.top - box.top + r.height / 2, top: r.top - box.top, r };
  }

  // Draws one beat. n = characters typed so far (only for the beat's typing row).
  function render(i, n) {
    const st = STORY[lang], b = BEATS[i];
    const typingTito = b.type === "tito" && n >= 0;
    const typingReply = b.type === "reply" && n >= 0;
    head.textContent = typingReply || b.type !== "reply" ? st.heads[i] : st.replied;
    text.innerHTML = typingTito ? esc(st.tito.slice(0, n)) + CARET : b.signed ? sign(st.tito) : esc(st.tito);
    seen.hidden = !b.seen;
    seenText.textContent = st.seen;
    replyRow.hidden = !b.reply;
    replyWho.textContent = st.who;
    replyText.innerHTML = typingReply ? esc(st.reply.slice(0, n)) + CARET : esc(st.reply);
    stations.forEach((s, k) => s.classList.toggle("is-on", k === b.at));
  }

  // The route leaves room above the line for the tallest plate (both messages), at this width and language.
  function roomForPlate() {
    const keep = { beat, typed };
    render(4, -1);
    const h = plate.offsetHeight;
    render(keep.beat, keep.typed);
    route.style.paddingTop = h + 48 + "px";
  }

  function layout() {
    roomForPlate();
    const a = ringCenter(0), c = ringCenter(2);
    const line = parseFloat(getComputedStyle(track).height) || 6;
    track.style.top = a.y - line / 2 + "px";
    track.style.left = a.x + "px";
    track.style.width = c.x - a.x + "px";
    // The line drops from Tito's station to the bottom of the coral field, where the page's spine starts.
    const hero = drop.parentElement.getBoundingClientRect();
    drop.style.left = a.r.left - hero.left + a.r.width / 2 - line / 2 + "px";
    drop.style.top = a.r.bottom - hero.top + "px";
    drop.style.height = hero.bottom - a.r.bottom + "px";
  }

  function place(i, animate) {
    const c = ringCenter(i);
    const w = plate.offsetWidth, h = plate.offsetHeight;
    const max = route.clientWidth - w;
    const x = Math.max(0, Math.min(max, c.x - w / 2));
    const y = c.top - h - 16;
    if (!animate) plate.style.transition = "none";
    plate.style.transform = `translate(${x}px, ${y}px)`;
    if (!animate) { void plate.offsetWidth; plate.style.transition = ""; }
  }

  function show(i, animate) {
    render(i, typed);
    place(BEATS[i].at, animate);
  }

  // Starts a beat: travel there, and start its typing if it has some.
  function enter(i, animate) {
    beat = i;
    typed = BEATS[i].type ? 0 : -1;
    show(i, animate);
    schedule();
  }

  function tick() {
    const st = STORY[lang], b = BEATS[beat];
    const full = (b.type === "tito" ? st.tito : st.reply).length;
    typed += 1;
    if (typed >= full) typed = -1;
    render(beat, typed);
    place(b.at, false); // the plate grows as the words come; it stays on its station
    schedule();
  }

  function step() {
    const next = (beat + 1) % BEATS.length;
    if (next === 0) {
      // The story starts again: fade, reset without travelling, fade back in.
      plate.classList.add("is-fading");
      timer = setTimeout(() => { plate.classList.remove("is-fading"); enter(0, false); }, 300);
      return;
    }
    enter(next, BEATS[next].at !== BEATS[beat].at);
  }

  function schedule() {
    clearTimeout(timer);
    if (userPaused || !visible || still.matches) return;
    timer = typed >= 0 ? setTimeout(tick, typed === 0 && BEATS[beat].type === "reply" ? 900 : CHAR_MS) : setTimeout(step, HOLD[beat]);
  }

  function setPauseLabel() {
    const d = lang === "en" ? EN : PT;
    pauseBtn.querySelector("span").textContent = userPaused ? d.play : d.pause;
    pauseBtn.querySelector("use").setAttribute("href", userPaused ? "#i-play" : "#i-pause");
    pauseBtn.setAttribute("aria-pressed", String(userPaused));
  }

  pauseBtn.addEventListener("click", () => { userPaused = !userPaused; setPauseLabel(); schedule(); });

  function applyMotionPreference() {
    pauseBtn.hidden = still.matches;
    // Reduced motion: the end of the story, still: Tito's message on top, Vovó's answer under it.
    if (still.matches) { clearTimeout(timer); beat = 3; typed = -1; show(beat, false); } else schedule();
  }
  still.addEventListener("change", applyMotionPreference);

  if ("IntersectionObserver" in window) {
    new IntersectionObserver((entries) => {
      visible = entries[0].isIntersecting;
      if (visible) schedule(); else clearTimeout(timer);
    }, { threshold: 0.1 }).observe(route);
  }

  // Lay the line out again whenever the hero changes size: a resize, the web font arriving, a language switch.
  let resizeFrame = 0;
  function relayout() {
    cancelAnimationFrame(resizeFrame);
    resizeFrame = requestAnimationFrame(() => { layout(); show(beat, false); });
  }
  window.addEventListener("resize", relayout);
  if ("ResizeObserver" in window) new ResizeObserver(relayout).observe(document.querySelector(".hero__copy"));

  /* ---------- Section entrances (content is visible without JS) ---------- */
  function reveals() {
    const items = document.querySelectorAll(".reveal");
    if (still.matches || !("IntersectionObserver" in window)) { items.forEach((el) => el.classList.add("is-in")); return; }
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => { if (e.isIntersecting) { e.target.classList.add("is-in"); io.unobserve(e.target); } });
    }, { threshold: 0.15 });
    items.forEach((el) => {
      const r = el.getBoundingClientRect();
      if (r.top < window.innerHeight) el.classList.add("is-in"); else io.observe(el);
    });
  }
  // Without JS the sections are simply there; with it they rise in once.
  document.documentElement.classList.add("js");

  document.querySelectorAll(".lang button").forEach((b) => b.addEventListener("click", () => setLang(b.dataset.lang, true)));

  function start() {
    const wanted = new URLSearchParams(location.search).get("lang") || stored() || ((navigator.language || "").toLowerCase().startsWith("pt") ? "pt" : "pt");
    setLang(wanted, false);
    layout();
    enter(0, false);
    applyMotionPreference();
    reveals();
  }
  if (document.fonts && document.fonts.ready) document.fonts.ready.then(start); else window.addEventListener("load", start);
})();
