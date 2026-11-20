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
  const PLATES = {
    pt: [
      { at: 0, from: "Tito", text: "Oi vó, tô com saudade" },
      { at: 1, from: "Sai do seu WhatsApp", text: "<b>Tito</b>: Oi vó, tô com saudade" },
      { at: 2, from: "Chega para a Vovó", text: "<b>Tito</b>: Oi vó, tô com saudade", seen: "Vovó viu" },
      { at: 2, from: "Vovó responde", text: "Também, meu amor. Domingo tem bolo.", swap: true },
      { at: 1, from: "Pelo seu WhatsApp", text: "Também, meu amor. Domingo tem bolo." },
      { at: 0, from: "Chega no tablet do Tito", text: "Também, meu amor. Domingo tem bolo." },
    ],
    en: [
      { at: 0, from: "Tito", text: "Hi Grandma, I miss you" },
      { at: 1, from: "Goes out from your WhatsApp", text: "<b>Tito</b>: Hi Grandma, I miss you" },
      { at: 2, from: "Reaches Grandma", text: "<b>Tito</b>: Hi Grandma, I miss you", seen: "Grandma saw it" },
      { at: 2, from: "Grandma replies", text: "Me too, sweetheart. There’s cake on Sunday.", swap: true },
      { at: 1, from: "Through your WhatsApp", text: "Me too, sweetheart. There’s cake on Sunday." },
      { at: 0, from: "Reaches Tito’s tablet", text: "Me too, sweetheart. There’s cake on Sunday." },
    ],
  };
  const HOLD = [2400, 2400, 2800, 2200, 2000, 2600];

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
    show(beat, false);
    if (save) store(lang);
  }

  /* ---------- The family line ---------- */
  const route = document.getElementById("route");
  const plate = document.getElementById("plate");
  const from = document.getElementById("plateFrom");
  const text = document.getElementById("plateText");
  const seen = document.getElementById("plateSeen");
  const seenText = document.getElementById("plateSeenText");
  const track = route.querySelector(".route__track");
  const stations = Array.from(route.querySelectorAll(".station"));
  const pauseBtn = document.getElementById("pause");
  const drop = document.getElementById("drop");
  const still = window.matchMedia("(prefers-reduced-motion: reduce)");

  let beat = 0;
  let timer = null;
  let userPaused = false;
  let visible = true;

  function ringCenter(i) {
    const r = stations[i].querySelector(".station__ring").getBoundingClientRect();
    const box = route.getBoundingClientRect();
    return { x: r.left - box.left + r.width / 2, y: r.top - box.top + r.height / 2, top: r.top - box.top, r };
  }

  function layout() {
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
    const b = PLATES[lang][i];
    from.textContent = b.from;
    text.innerHTML = b.text;
    seen.hidden = !b.seen;
    if (b.seen) seenText.textContent = b.seen;
    stations.forEach((s, k) => s.classList.toggle("is-on", still.matches ? true : k === b.at));
    place(b.at, animate);
  }

  function step() {
    const next = (beat + 1) % PLATES.pt.length;
    const wrapAround = next === 0;
    const swap = PLATES.pt[next].swap;
    if (wrapAround || swap) {
      // The message changes hands: fade, swap without travelling, fade back in.
      plate.classList.add("is-fading");
      timer = setTimeout(() => {
        beat = next; show(beat, false);
        plate.classList.remove("is-fading");
        schedule();
      }, 260);
      return;
    }
    beat = next;
    show(beat, true);
    schedule();
  }

  function schedule() {
    clearTimeout(timer);
    if (userPaused || !visible || still.matches) return;
    timer = setTimeout(step, HOLD[beat]);
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
    if (still.matches) { clearTimeout(timer); beat = 2; show(beat, false); } else schedule();
  }
  still.addEventListener("change", applyMotionPreference);

  if ("IntersectionObserver" in window) {
    new IntersectionObserver((entries) => {
      visible = entries[0].isIntersecting;
      if (visible) schedule(); else clearTimeout(timer);
    }, { threshold: 0.1 }).observe(route);
  }

  let resizeFrame = 0;
  window.addEventListener("resize", () => {
    cancelAnimationFrame(resizeFrame);
    resizeFrame = requestAnimationFrame(() => { layout(); show(beat, false); });
  });

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
    layout();
    setLang(wanted, false);
    applyMotionPreference();
    reveals();
  }
  if (document.fonts && document.fonts.ready) document.fonts.ready.then(start); else window.addEventListener("load", start);
})();
