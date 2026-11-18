#!/usr/bin/env python3
"""Builds the Oiê tablet prototype (index.html) from the design system only.

    python3 "03 Product/prototype/build.py"

Inputs, all in this repo:
  design/generated/tokens.css      colours, sizes, type (never edit; run design/build.py)
  design/core/icons/gg_ic_*.xml    the core line icons
  01 Brand/logo/, 01 Brand/stickers/
  components.css                   the prototype's components, built only from var(--gds-*)
Names are examples, and the QR code is fake (labelled "Exemplo").
"""
import pathlib
import random
import re

HERE = pathlib.Path(__file__).resolve().parent
REPO = HERE.parent.parent


def icon(n, cls="ic"):
    x = (REPO / "design/core/icons" / f"gg_ic_{n}.xml").read_text()
    ps = re.findall(r'pathData="([^"]+)"', x)
    return f'<svg class="{cls}" viewBox="0 0 24 24" aria-hidden="true">' + "".join(f'<path d="{p}"/>' for p in ps) + "</svg>"


LOGO = (REPO / "01 Brand/logo/oie-wordmark-coral.svg").read_text().replace("<svg ", '<svg class="wordmark" role="img" aria-label="Oiê" ')
STICKERS = sorted((REPO / "01 Brand/stickers").glob("*.svg"))
FAM = [("Vovó", "V", 2), ("Mãe", "M", 0), ("Vovô", "V", 0), ("Tia Bia", "B", 1), ("Primos", "P", 0)]
ROUNDS = ('<svg class="rounds" viewBox="0 0 280 256" aria-hidden="true">'
          '<circle cx="72" cy="150" r="41" fill="none" stroke="var(--gds-selected)" stroke-width="32"/>'
          '<rect x="124" y="98" width="32" height="108" rx="16" fill="var(--gds-selected)"/>'
          '<circle cx="140" cy="62" r="19" fill="var(--gds-selected)"/></svg>')


def tiles():
    s = '<div class="tiles">'
    for n, i, u in FAM:
        badge = f'<span class="unread">{u} {"nova" if u == 1 else "novas"}</span>' if u else ""
        s += f'<button class="tile"><span class="photo">{i}</span><span class="name">{n}</span>{badge}</button>'
    return s + "</div>"


def header_kid():
    return f'<header class="bar">{LOGO}<button class="quiet-btn small end">{icon("gear")}<span>Pais</span></button></header>'


def wave(hs):
    return '<span class="wave">' + "".join(f'<i style="height:{h}px"></i>' for h in hs) + "</span>"


def chat(extra=""):
    return ('<div class="msg them"><span class="who">Vovó</span>Oi meu amor, como foi a escola?<span class="time">16h20</span></div>'
            f'<div class="msg me voice">{icon("play", "ic play")}{wave([8,14,22,12,26,18,10,20,28,16,12,8,18,24,14,10])}<span class="time">0:12</span></div>'
            f'<div class="msg them voice">{icon("play", "ic play")}{wave([10,18,12,24,16,8,20,26,14,10,16,22,12,8])}<span class="time">0:09</span></div>'
            '<div class="newline"><span>Novas</span></div>'
            '<div class="msg them">Que saudade. Domingo tem bolo aqui em casa.<span class="time">16h31</span></div>' + extra)


def convo_header():
    return (f'<header class="bar"><button class="quiet-btn icon-only" aria-label="Voltar">{icon("back")}</button>'
            '<span class="photo sm">V</span><h2 class="title">Vovó</h2></header>')


def composer():
    return (f'<footer class="composer"><button class="quiet-btn icon-only" aria-label="Foto">{icon("camera")}</button>'
            f'<button class="quiet-btn icon-only" aria-label="Figurinha">{icon("sticker")}</button>'
            '<label class="field"><span class="sr">Mensagem</span><input placeholder="Escreva para Vovó"></label>'
            f'<button class="record" aria-label="Gravar áudio">{icon("mic", "ic big")}</button></footer>')


def setup(step, body):
    step_label = f'<span class="caption muted end">Passo {step} de 6</span>' if step else ""
    return f'<header class="bar">{LOGO}{step_label}</header><main class="setup">{body}</main>'


def keypad(title, sub, extra=""):
    keys = "".join(f'<button class="key">{k}</button>' for k in "123456789") + '<span></span><button class="key">0</button>' + \
        f'<button class="key" aria-label="Apagar">{icon("back")}</button>'
    dots = "".join(f'<i class="{"on" if j < 3 else ""}"></i>' for j in range(6))
    return (f'<div class="pinbox"><h1 class="title">{title}</h1><p class="body muted">{sub}</p>'
            f'<div class="dots" aria-label="3 de 6 números">{dots}</div><div class="keys">{keys}</div>{extra}</div>')


def parent(sel, content):
    nav = "".join(f'<button class="navrow{" on" if k == sel else ""}">{icon(i)}<span>{k}</span></button>'
                  for k, i in [("Contatos", "people"), ("Horário de silêncio", "clock"), ("Conexão", "link"), ("Sobre", "info")])
    return (f'<header class="bar"><h2 class="title">Área dos pais</h2><button class="quiet-btn small end">{icon("close")}<span>Sair</span></button></header>'
            f'<main class="parent"><nav>{nav}</nav><section class="pane">{content}</section></main>')


def qr():
    random.seed(4)
    s = '<svg class="qr" viewBox="0 0 29 29" aria-label="QR code de exemplo">'
    for y in range(29):
        for x in range(29):
            if (x < 7 and y < 7) or (x > 21 and y < 7) or (x < 7 and y > 21):
                fx, fy = (x - 22 if x > 21 else x), (y - 22 if y > 21 else y)
                on = fx in (0, 6) or fy in (0, 6) or (2 <= fx <= 4 and 2 <= fy <= 4)
            else:
                on = random.random() < 0.48
            if on:
                s += f'<rect x="{x}" y="{y}" width="1" height="1"/>'
    return s + "</svg>"


S = {}
# --- v0.1 screens -----------------------------------------------------------------------------
S["01-kid-home"] = ("Kid home", header_kid() + '<main class="kid"><h1 class="display">Oiê, Tito</h1><p class="muted">Com quem você quer falar?</p>' + ROUNDS + tiles() + "</main>")
S["02-kid-home-quiet"] = ("Kid home, quiet hours", header_kid() + f'<div class="banner">{icon("clock")}<span>Horário de silêncio até 18h. Dá para ler e ouvir, mas não para mandar.</span></div><main class="kid"><h1 class="display">Oiê, Tito</h1>' + tiles() + "</main>")
S["03-conversation"] = ("Conversation", convo_header() + '<main class="chat">' + chat('<div class="msg me">Eba, vou sim<span class="time">16h32</span></div><p class="sent">Enviada · 16h32</p>') + "</main>" + composer())
S["04-kid-notice"] = ("Kid notice (first open)", header_kid() + '<main class="kid"><h1 class="display">Oiê, Tito</h1>' + tiles() + '</main><div class="scrim"></div><section class="sheet" role="dialog" aria-labelledby="kn"><h2 id="kn" class="title">Antes de começar</h2><p class="body">Suas mensagens saem do WhatsApp da Mãe, então a Mãe pode ver o que você manda.</p><button class="primary">Entendi</button></section>')
S["05-risk-notice"] = ("Setup 1: risk notice", setup(1, '<div class="col"><h1 class="display">Antes de conectar</h1><p class="lead">O Oiê usa o seu WhatsApp de um jeito que o WhatsApp não oferece oficialmente. Ele pode limitar ou bloquear o seu número. Se isso acontecer, você perde o acesso até resolver com o WhatsApp.</p><div class="actions"><button class="primary">Entendi, conectar</button><button class="quiet-btn wide">Agora não</button></div></div>'))
S["06-link"] = ("Setup 2: link", setup(2, '<div class="two"><div class="qrbox">' + qr() + '<p class="caption">Exemplo. O código muda sozinho.</p></div><div class="col"><h1 class="title">Conecte o seu WhatsApp</h1><ol class="steps"><li>No seu celular, abra o <b>WhatsApp</b>.</li><li>Toque em <b>Configurações</b> e depois em <b>Dispositivos conectados</b>.</li><li>Toque em <b>Conectar um dispositivo</b>.</li><li>Aponte o celular para este código.</li></ol><button class="quiet-btn wide back">Voltar</button></div></div>'))
rows = "".join(f'<div class="row"><span class="photo sm">{i}</span><span class="label">{n}</span><span class="caption muted">{"Grupo · todos veem as mensagens" if n == "Primos" else ("Pode falar no silêncio" if n == "Vovó" else "")}</span><button class="text-btn">Remover</button></div>' for n, i, u in FAM)
S["07-parent-contacts"] = ("Parent area: contacts", parent("Contatos", f'<div class="panehead"><div><h1 class="title">Quem Tito pode chamar</h1><p class="caption muted">4 pessoas, 1 grupo</p></div><button class="primary">{icon("plus")}Adicionar</button></div>' + rows))
quiet_rows = ('<div class="row"><span class="label">Escola</span><span class="body-small muted">Seg a sex · 7h às 12h</span><span class="switch on" role="switch" aria-checked="true" aria-label="Escola ligado"><i></i></span></div>'
              '<div class="row"><span class="label">Dormir</span><span class="body-small muted">Todos os dias · 21h às 7h do dia seguinte</span><span class="switch on" role="switch" aria-checked="true" aria-label="Dormir ligado"><i></i></span></div>')
S["08-parent-quiet"] = ("Parent area: quiet hours", parent("Horário de silêncio", f'<div class="panehead"><div><h1 class="title">Horário de silêncio</h1><p class="caption muted">Nesse horário Tito lê e ouve, mas não manda mensagens.</p></div><button class="primary">{icon("plus")}Adicionar período</button></div>' + quiet_rows + '<div class="row"><span class="label">Férias</span><span class="body-small muted">Pausado</span><span class="switch" role="switch" aria-checked="false" aria-label="Férias desligado"><i></i></span></div>' + f'<div class="row link"><span class="label">Podem falar mesmo assim</span><span class="body-small muted">Vovó</span>{icon("chevron")}</div>'))
# --- v0.2 screens: the states still missing in the v0.1 audit (I-6) -----------------------------
S["09-pin"] = ("Parent area: PIN", header_kid() + '<main class="setup">' + keypad("Área dos pais", "Digite o seu PIN.", '<button class="text-btn center">Esqueci o PIN</button>') + "</main>")
S["10-setup-names"] = ("Setup 3: names", setup(3, '<div class="col"><h1 class="title">Como vocês se chamam?</h1><label class="formfield"><span class="label">Seu nome</span><input value="Ana"><span class="caption muted">Do seu WhatsApp. Aparece para Tito como "WhatsApp de Ana".</span></label><label class="formfield"><span class="label">Como a família chama seu filho ou filha?</span><input value="Tito"><span class="caption muted">Vai junto das mensagens: "Tito: Oi, vó".</span></label><div class="actions"><button class="primary">Continuar</button><button class="quiet-btn wide">Voltar</button></div></div>'))
S["11-setup-pin"] = ("Setup 4: create PIN", setup(4, keypad("Crie um PIN de 6 números", "Ele abre a área dos pais neste tablet. Não use o mesmo PIN do tablet.")))
picks = [("Vovó", "V", True, ""), ("Mãe", "M", True, ""), ("Vovô", "V", True, ""), ("Tia Bia", "B", False, ""), ("Primos", "P", False, "Grupo · todos do grupo veem as mensagens"), ("Vizinha Marina", "M", False, "")]
pick_rows = "".join(f'<button class="row pick{" on" if on else ""}" role="checkbox" aria-checked="{"true" if on else "false"}"><span class="photo sm">{i}</span><span class="label">{n}</span><span class="caption muted">{note}</span>{icon("check", "ic tick") if on else ""}</button>' for n, i, on, note in picks)
S["12-setup-contacts"] = ("Setup 5: first contacts", setup(5, f'<div class="col wide"><h1 class="title">Com quem Tito pode falar?</h1><label class="search">{icon("search")}<input placeholder="Buscar nos seus contatos do WhatsApp"></label><div class="list">{pick_rows}</div><div class="actions"><button class="primary">Continuar · 3 escolhidos</button><button class="quiet-btn wide">Depois</button></div></div>'))
S["13-setup-quiet"] = ("Setup 6: quiet hours", setup(6, '<div class="col wide"><h1 class="title">Horário de silêncio</h1><p class="body muted">Nesse horário Tito lê e ouve, mas não manda mensagens. Dá para mudar depois.</p><div class="list">' + quiet_rows.replace('switch on" role="switch" aria-checked="true"', 'switch" role="switch" aria-checked="false"').replace("ligado", "desligado") + '</div><div class="actions"><button class="primary">Continuar</button><button class="quiet-btn wide">Pular</button></div></div>'))
S["14-setup-done"] = ("Setup done", setup(0, '<div class="col center">' + LOGO.replace('class="wordmark"', 'class="wordmark big"') + '<h1 class="display">Pronto.</h1><p class="lead">Entregue o tablet para Tito.</p><button class="primary">Abrir para Tito</button></div>'))
results = [("Vovó", "V", True, ""), ("Vovô", "V", True, ""), ("Vó Lúcia", "L", False, ""), ("Família Vó Lu", "F", False, "Grupo · todos do grupo veem as mensagens")]
res_rows = "".join(f'<button class="row pick{" on" if on else ""}" role="checkbox" aria-checked="{"true" if on else "false"}"><span class="photo sm">{i}</span><span class="label">{n}</span><span class="caption muted">{note}</span>{icon("check", "ic tick") if on else ""}</button>' for n, i, on, note in results)
S["15-parent-add"] = ("Parent area: add contact", parent("Contatos", f'<div class="panehead"><div><h1 class="title">Adicionar</h1><p class="caption muted">Toque para escolher. Toque de novo para tirar.</p></div></div><label class="search">{icon("search")}<input value="vó"></label><div class="list">{res_rows}</div>'))
S["16-recording"] = ("Recording a voice message", convo_header() + '<main class="chat">' + chat() + '</main><footer class="composer recording"><button class="quiet-btn wide">Cancelar</button><span class="rec-dot" aria-hidden="true"></span><span class="label">Gravando 0:12</span>' + wave([6,10,18,26,14,22,30,18,10,24,16,8,20,28,12,18,24,10,16,22]) + f'<span class="body-small muted end">Solte para enviar</span><button class="record pressed" aria-label="Gravando 0:12. Solte para enviar">{icon("mic", "ic big")}</button></footer>')
S["17-photo-preview"] = ("Photo preview", convo_header().replace('<h2 class="title">Vovó</h2>', '<h2 class="title">Foto para Vovó</h2>') + '<main class="preview"><div class="photo-frame"><svg viewBox="0 0 280 256" aria-hidden="true"><circle cx="72" cy="150" r="41" fill="none" stroke="var(--gds-selected)" stroke-width="32"/><rect x="124" y="98" width="32" height="108" rx="16" fill="var(--gds-selected)"/><circle cx="140" cy="62" r="19" fill="var(--gds-selected)"/></svg><span class="caption">Exemplo: o desenho que Tito fotografou</span></div><div class="actions"><button class="quiet-btn wide">Trocar</button><button class="primary">' + icon("send") + 'Enviar</button></div></main>')
tray = "".join(f'<button class="sticker" aria-label="{p.stem.split("_")[2].replace("-", " ")}">' + p.read_text().replace("<svg ", '<svg aria-hidden="true" ') + "</button>" for p in STICKERS)
S["18-sticker-tray"] = ("Sticker tray", convo_header() + '<main class="chat">' + chat() + '</main><div class="scrim"></div><section class="sheet wide" role="dialog" aria-labelledby="st"><h2 id="st" class="title">Figurinhas</h2><div class="stickers">' + tray + "</div></section>")
S["19-undo"] = ("Sending, with undo", convo_header() + '<main class="chat">' + chat('<div class="msg me pending">Eba, vou sim<span class="time">Enviando…</span></div>') + '</main><div class="snackbar" role="status"><span>Enviando para Vovó</span><button class="snack-action">Desfazer</button></div>' + composer())
S["20-offline"] = ("Offline", convo_header() + f'<div class="banner">{icon("info")}<span>Sem internet. Suas mensagens vão quando voltar.</span></div><main class="chat">' + chat('<div class="msg me pending">Eba, vou sim<span class="time">Vai quando a internet voltar</span></div>') + "</main>" + composer())
S["21-kid-unlinked"] = ("Kid home, link dropped", header_kid() + f'<div class="banner">{icon("link")}<span>O Oiê se desconectou. Peça para a Mãe conectar de novo.</span></div><main class="kid"><h1 class="display">Oiê, Tito</h1>' + tiles() + "</main>")


def main():
    css = (REPO / "design/generated/tokens.css").read_text() + "\n" + (HERE / "components.css").read_text()
    out = ['<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>Oiê prototype</title>'
           '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap">'
           f"<style>{css}</style></head><body>"]
    for k, (name, body) in S.items():
        for th in ("light", "dark"):
            out.append(f'<p class="board-label">OIE · {k} · {name} · {th} · 1280×800 (names are examples)</p><div class="screen" id="{k}-{th}" data-theme="{th}">{body}</div>')
    for k in ("01-kid-home", "03-conversation"):
        out.append(f'<p class="board-label">OIE · {k} · portrait · light · 800×1280</p><div class="screen portrait" id="{k}-portrait-light" data-theme="light">{S[k][1]}</div>')
    out.append("</body></html>")
    (HERE / "index.html").write_text("\n".join(out))
    print(f"wrote index.html: {len(S)} screens, {len(S) * 2 + 2} artboards")


if __name__ == "__main__":
    main()
