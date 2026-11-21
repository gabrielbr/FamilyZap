// Counts visits to this website with GoatCounter (decision 0017). This is the website only:
// the Oiê app counts nothing and sends nothing about the family.
// - No cookies and nothing stored on the visitor's device. GoatCounter keeps no IP address.
// - What is sent: the page, the site that linked here (without its query), and taps on "Baixar".
// - Not counted: browsers with "Do Not Track" or Global Privacy Control on, and any copy of the
//   site that isn't the published one (tests, local previews).
// - To stop counting your own visits on a browser, open the site once with ?naocontar=1
//   (?contar=1 undoes it). That one choice is kept in the browser's local storage.
(function () {
  "use strict";
  var CODE = "TBD"; // the GoatCounter site code (source needed: the owner's GoatCounter account)
  var HOSTS = ["gabrielbr.github.io"];
  var KEY = "oie-naocontar";

  var params = new URLSearchParams(location.search);
  try {
    if (params.has("naocontar")) localStorage.setItem(KEY, "1");
    if (params.has("contar")) localStorage.removeItem(KEY);
    if (localStorage.getItem(KEY) === "1") return;
  } catch (e) { /* storage blocked: count as usual */ }

  if (CODE === "TBD" || HOSTS.indexOf(location.hostname) < 0) return;
  if (navigator.doNotTrack === "1" || window.doNotTrack === "1" || navigator.globalPrivacyControl) return;

  var END = "https://" + CODE + ".goatcounter.com/count";

  function send(q) {
    var parts = [];
    for (var k in q) if (q[k]) parts.push(k + "=" + encodeURIComponent(q[k]));
    parts.push("rnd=" + Math.random().toString(36).slice(2));
    var url = END + "?" + parts.join("&");
    if (navigator.sendBeacon && navigator.sendBeacon(url)) return;
    new Image().src = url;
  }

  // The page, without the repository prefix; English visits keep their ?lang=en.
  var page = location.pathname.replace(/^\/oie-releases/, "") || "/";
  if (params.get("lang") === "en") page += "?lang=en";

  // Where the visitor came from: only another site's address, without its query.
  var from = "";
  try {
    var r = new URL(document.referrer);
    if (r.host !== location.host) from = r.origin + r.pathname;
  } catch (e) { /* no referrer */ }

  send({ p: page, r: from, t: document.title });

  // A tap on a "Baixar o Oiê" button (marked data-count="baixar") counts as one event.
  document.addEventListener("click", function (ev) {
    var a = ev.target && ev.target.closest && ev.target.closest("a[data-count]");
    if (a) send({ p: a.getAttribute("data-count"), t: "Baixar o Oiê", e: "true" });
  }, true);
})();
