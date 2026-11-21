/* Oiê landing page v3 (decision 0014): the PT/EN switch and the scenes cutting in as they arrive.
   Portuguese is the page itself; English comes from the table below. Nothing is sent anywhere. */
(() => {
  "use strict";
  const EN = {
    skip: "Skip to the download", navSafety: "Safety", navSafetyHref: "safety/", langGroup: "Language",
    new: "New!", h1: "Remember the home phone?", lede: "It’s back. Now on the tablet, so your kid can call family and friends.",
    cta: "Get it now. It’s free!", ctaNote: "For Android tablets, version 8 or newer.", ring: "Ring ring!",
    s2h: "Today, to talk to Grandma…", s2p: "…a kid gets a whole phone. With a feed, ads and strangers thrown in.", s2stamp: "It doesn’t have to be like this.",
    c1: "AD!", c2: "ENDLESS FEED", c3: "WHO ARE YOU?", c4: "BUY NOW!",
    s3burst: "Introducing", s3h: "Oiê: the home phone,<br>on the tablet.", s3p: "Your kid talks to the people you chose. No one else.",
    s4h: "It’s this easy:", s4a: "Tap Grandma’s photo.", s4b: "Talk, write or send a sticker.", s4c: "It reaches her WhatsApp, through yours.",
    s5h: "But wait… there’s more!",
    m1: "<b>Only who you choose.</b> Family or friends.", m2: "<b>No ads.</b> No feed. No endless scrolling.",
    m3: "<b>Oiê has no servers.</b> Nothing about your family reaches us.", m4: "<b>Quiet hours.</b> At school and at bedtime.",
    s6h: "Phone vs Oiê", vsWhat: "What", vsPhone: "Kid’s own phone", vsOie: "Oiê on the tablet",
    v1: "Who they talk to", v1a: "Anyone", v1b: "Only who you approved",
    v2: "Ads and feeds", v2a: "Lots", v2b: "None",
    v3: "Your kid’s data", v3a: "Turned into ads", v3b: "Stays at home",
    v4: "What it needs", v4a: "A SIM, a plan and a device of their own", v4b: "The home tablet and your WhatsApp",
    s7h: "And how much is it?", price: "R$ 0,00", s7p: "It’s free. And the code is open: anyone can check what it does.",
    s8h: "Get it now!", ctaDl: "Download Oiê", dlNote: "Open this page on your kid’s Android tablet (Android 8 or newer).",
    g1: "On your kid’s tablet, tap Download Oiê and open the file.",
    g2: "If Android asks, allow the install. If it won’t let you, whoever manages the tablet can allow it.",
    g3: "Open Oiê, scan the QR code with your WhatsApp and choose the contacts.",
    noticeH: "Notice",
    n1: "Oiê is an independent app that connects to your WhatsApp the way WhatsApp Web does. <b>It is not affiliated with WhatsApp or Meta.</b>",
    n2: "Use it at your own risk: WhatsApp may restrict or block numbers that use third-party apps. Oiê sends at a person’s pace, one message at a time, and explains this before you link.",
    faqH: "Questions",
    q1: "Does the person receiving need to install anything?", a1: "No. Family and friends get it in their WhatsApp, as a message from you, with your kid’s name in front.",
    q2: "Can my kid talk to friends?", a2: "Yes, if you add them. Your kid only talks to people you approve, and no one else reaches them through Oiê.",
    q3: "Does my kid need a phone?", a3: "No. Oiê runs on an Android tablet you already have and uses your WhatsApp.",
    q4: "Is Oiê made by WhatsApp?", a4: "No. It’s an independent app that connects to your WhatsApp like WhatsApp Web, and it isn’t affiliated with WhatsApp or Meta. WhatsApp may restrict or block numbers that use third-party apps.",
    q5: "Do I see what my kid sends?", a5: "Yes. Messages go out from your WhatsApp, so they’re on your phone too. Your kid knows this from day one.",
    q6: "Does Oiê keep anything about my family?", a6: "No. Oiê has no servers. Messages go only through WhatsApp. There are no analytics, ads or tracking. The only other connection checks GitHub for a new version.",
    q7: "What if WhatsApp blocks my number?", a7: "Oiê stops at once, shows WhatsApp’s notice on the tablet and doesn’t try again on its own. On your phone, follow what WhatsApp shows you. You can unlink Oiê in Linked devices.",
    q8: "Can they make calls?", a8: "Not yet. For now: messages, voice notes, photos, videos and stickers.",
    q9: "Why isn’t it on the Play Store?", a9: "For now Oiê is downloaded straight from its releases page. Each release comes with a code (SHA-256) to check the file is the original, and the app tells you when there’s a new version.",
    moreSafety: "Why we made Oiê, and a safety guide for parents",
    made: "Oiê is <b>open source</b>. Made by <a href=\"https://www.gabriellopes.com\" rel=\"noopener\">Gabriel Lopes</a>.",
    footCode: "The code on GitHub", footAll: "All releases", footLib: "whatsmeow (MPL-2.0)",
    footLegal: "Oiê is an independent app, not affiliated with WhatsApp or Meta. WhatsApp is a trademark of Meta. The drawings on this page are examples.",
    title: "Oiê: the home phone, back. On the tablet.",
    desc: "Remember the home phone? Oiê brings it to the tablet: your kid talks to the family and friends you choose, through your WhatsApp. No ads, no feed, no servers. Free and open source.",
    f1: "Grandma", f2: "Grandpa", f3: "Aunt Bia", f4: "Lucas",
  };

  document.documentElement.classList.add("js");
  const all = (s) => document.querySelectorAll(s);
  const PT = {};
  all("[data-t]").forEach((el) => (PT[el.dataset.t] = el.textContent));
  all("[data-t-html]").forEach((el) => (PT[el.dataset.tHtml] = el.innerHTML));
  all("[data-t-aria]").forEach((el) => (PT[el.dataset.tAria] = el.getAttribute("aria-label")));
  all("[data-t-href]").forEach((el) => (PT[el.dataset.tHref] = el.getAttribute("href")));
  all("[data-v]").forEach((el) => (PT[el.dataset.v] = el.textContent));
  PT.title = document.title;
  PT.desc = document.querySelector('meta[name="description"]').content;

  function store(k, v) { try { localStorage.setItem(k, v); } catch (e) { /* private mode: fine */ } }
  function stored(k) { try { return localStorage.getItem(k); } catch (e) { return null; } }

  function apply(lang) {
    const d = lang === "en" ? EN : PT;
    document.documentElement.lang = lang === "en" ? "en" : "pt-BR";
    all("[data-t]").forEach((el) => { if (d[el.dataset.t] != null) el.textContent = d[el.dataset.t]; });
    all("[data-t-html]").forEach((el) => { if (d[el.dataset.tHtml] != null) el.innerHTML = d[el.dataset.tHtml]; });
    all("[data-t-aria]").forEach((el) => { if (d[el.dataset.tAria] != null) el.setAttribute("aria-label", d[el.dataset.tAria]); });
    all("[data-t-href]").forEach((el) => { if (d[el.dataset.tHref] != null) el.setAttribute("href", d[el.dataset.tHref]); });
    all("[data-v]").forEach((el) => { if (d[el.dataset.v] != null) el.textContent = d[el.dataset.v]; });
    document.title = d.title;
    document.querySelector('meta[name="description"]').content = d.desc;
    all(".lang button").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.lang === lang)));
  }
  all(".lang button").forEach((b) => b.addEventListener("click", () => {
    const lang = b.dataset.lang === "en" ? "en" : "pt";
    store("oie-lang", lang);
    apply(lang);
  }));
  const wanted = new URLSearchParams(location.search).get("lang") || stored("oie-lang");
  if (wanted === "en") apply("en");

  // Each scene cuts in once, the way an ad cuts between shots. Without the observer, everything simply shows.
  const scenes = all("main .scene");
  scenes.forEach((s) => {
    Array.from(s.querySelectorAll("h2, .big, .burst, .stamp, .steps li, .stickers li, .versus, .btn, .install, .chaos")).forEach((el, i) => {
      el.classList.add("reveal");
      el.style.setProperty("--i", String(i));
    });
  });
  if (!("IntersectionObserver" in window)) { scenes.forEach((s) => s.classList.add("is-in")); return; }
  const io = new IntersectionObserver((entries) => {
    entries.forEach((e) => { if (e.isIntersecting) { e.target.classList.add("is-in"); io.unobserve(e.target); } });
  }, { threshold: 0.18 });
  scenes.forEach((s) => io.observe(s));
})();
