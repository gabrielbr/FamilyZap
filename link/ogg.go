package oielink

import (
	"encoding/binary"
	"errors"
)

// Voice notes (v0.32). Android records Opus in Ogg with its own Ogg writer; the owner's test showed
// WhatsApp failing to play what the tablet sent, while the tablet played it fine. Before sending,
// the recording is re-packed into a plain, standard Ogg Opus stream: the same Opus packets, a clean
// OpusHead and OpusTags, pages with correct granule positions and CRCs. The audio isn't touched.

var errNotOpus = errors.New("not an Ogg Opus recording")

// oggPackets reads every packet of the first logical stream of an Ogg file.
func oggPackets(b []byte) ([][]byte, error) {
	var packets [][]byte
	var cur []byte
	serial, seen := uint32(0), false
	for len(b) > 0 {
		if len(b) < 27 || string(b[:4]) != "OggS" {
			return nil, errors.New("ogg: bad page")
		}
		nseg := int(b[26])
		if len(b) < 27+nseg {
			return nil, errors.New("ogg: short page")
		}
		lacing := b[27 : 27+nseg]
		size := 0
		for _, l := range lacing {
			size += int(l)
		}
		body := b[27+nseg:]
		if len(body) < size {
			return nil, errors.New("ogg: short body")
		}
		s := binary.LittleEndian.Uint32(b[14:18])
		if !seen {
			serial, seen = s, true
		}
		if s == serial {
			off := 0
			for _, l := range lacing {
				cur = append(cur, body[off:off+int(l)]...)
				off += int(l)
				if l < 255 {
					packets = append(packets, cur)
					cur = nil
				}
			}
		}
		b = body[size:]
	}
	if len(cur) > 0 {
		packets = append(packets, cur)
	}
	return packets, nil
}

// opusSamples is how many 48 kHz samples an Opus packet holds (RFC 6716, section 3.1).
func opusSamples(p []byte) int {
	if len(p) == 0 {
		return 0
	}
	config := int(p[0] >> 3)
	var tenthsMs int // frame length in tenths of a millisecond
	switch {
	case config < 12: // SILK: 10, 20, 40, 60 ms
		tenthsMs = []int{100, 200, 400, 600}[config%4]
	case config < 16: // Hybrid: 10, 20 ms
		tenthsMs = []int{100, 200}[config%2]
	default: // CELT: 2.5, 5, 10, 20 ms
		tenthsMs = []int{25, 50, 100, 200}[config%4]
	}
	frames := 1
	switch p[0] & 3 {
	case 1, 2:
		frames = 2
	case 3:
		if len(p) < 2 {
			return 0
		}
		frames = int(p[1] & 0x3f)
	}
	return frames * tenthsMs * 48 / 10
}

// oggCRC is Ogg's CRC-32 (polynomial 0x04c11db7, not reflected, initial value 0).
var oggCRCTable = func() (t [256]uint32) {
	for i := range t {
		r := uint32(i) << 24
		for j := 0; j < 8; j++ {
			if r&0x80000000 != 0 {
				r = r<<1 ^ 0x04c11db7
			} else {
				r <<= 1
			}
		}
		t[i] = r
	}
	return
}()

func oggCRC(b []byte) uint32 {
	var c uint32
	for _, x := range b {
		c = c<<8 ^ oggCRCTable[byte(c>>24)^x]
	}
	return c
}

// oggPage writes one page holding whole packets.
func oggPage(serial, seq uint32, granule uint64, flags byte, packets [][]byte) []byte {
	var lacing []byte
	var body []byte
	for _, p := range packets {
		n := len(p)
		for n >= 255 {
			lacing = append(lacing, 255)
			n -= 255
		}
		lacing = append(lacing, byte(n))
		body = append(body, p...)
	}
	h := make([]byte, 27, 27+len(lacing)+len(body))
	copy(h, "OggS")
	h[5] = flags
	binary.LittleEndian.PutUint64(h[6:14], granule)
	binary.LittleEndian.PutUint32(h[14:18], serial)
	binary.LittleEndian.PutUint32(h[18:22], seq)
	h[26] = byte(len(lacing))
	page := append(append(h, lacing...), body...)
	binary.LittleEndian.PutUint32(page[22:26], oggCRC(page))
	return page
}

// normalizeOpus re-packs an Ogg Opus recording into a standard stream for WhatsApp, and returns it
// with its length in seconds (from the audio itself). An error means it isn't Ogg Opus.
func normalizeOpus(in []byte) ([]byte, int, error) {
	packets, err := oggPackets(in)
	if err != nil {
		return nil, 0, err
	}
	if len(packets) < 2 || len(packets[0]) < 19 || string(packets[0][:8]) != "OpusHead" {
		return nil, 0, errNotOpus
	}
	head := make([]byte, 19)
	copy(head, packets[0][:19])
	head[8] = 1 // version
	if head[9] == 0 {
		head[9] = 1 // mono
	}
	head[18] = 0 // channel mapping family 0 (mono or stereo)
	preSkip := int(binary.LittleEndian.Uint16(head[10:12]))
	tags := []byte("OpusTags\x03\x00\x00\x00Oie\x00\x00\x00\x00")
	audio := packets[1:]
	if len(audio) > 0 && len(audio[0]) >= 8 && string(audio[0][:8]) == "OpusTags" {
		audio = audio[1:]
	}
	const serial = 0x4f696521 // "Oie!"
	out := oggPage(serial, 0, 0, 0x02, [][]byte{head})
	out = append(out, oggPage(serial, 1, 0, 0, [][]byte{tags})...)
	seq, granule, total := uint32(2), uint64(0), 0
	var page [][]byte
	segs := 0
	flush := func(last bool) {
		var flags byte
		if last {
			flags = 0x04
		}
		out = append(out, oggPage(serial, seq, granule, flags, page)...)
		seq++
		page, segs = nil, 0
	}
	for _, p := range audio {
		n := opusSamples(p)
		if n == 0 {
			continue
		}
		need := len(p)/255 + 1
		if segs+need > 255 || len(page) >= 50 { // about a second of 20 ms frames per page
			flush(false)
		}
		page = append(page, p)
		segs += need
		granule += uint64(n)
		total += n
	}
	if len(page) == 0 {
		return nil, 0, errNotOpus // no audio at all
	}
	flush(true)
	seconds := (total - preSkip + 47_999) / 48_000
	if seconds < 1 {
		seconds = 1
	}
	return out, seconds, nil
}
