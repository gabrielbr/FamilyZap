package oielink

import (
	"bytes"
	"encoding/binary"
	"testing"
)

// opusHead is a mono OpusHead with a pre-skip of 312, like Android's.
func opusHead() []byte {
	h := []byte("OpusHead\x01\x01\x38\x01\x80\xbb\x00\x00\x00\x00\x00")
	return h
}

// frame20ms is a CELT 20 ms mono frame: config 31, one frame (TOC 0xf8), then payload.
func frame20ms(n int) []byte { return append([]byte{0xf8}, bytes.Repeat([]byte{byte(n)}, 60)...) }

// androidLike writes pages the way a messy writer might: odd granules, several packets per page,
// and one packet split across two pages.
func androidLike(packets [][]byte) []byte {
	var out []byte
	out = append(out, oggPage(7, 0, 0, 0x02, [][]byte{opusHead()})...)
	out = append(out, oggPage(7, 1, 0, 0, [][]byte{[]byte("OpusTags\x00\x00\x00\x00\x00\x00\x00\x00")})...)
	out = append(out, oggPage(7, 2, 999999, 0, packets[:3])...) // a wrong granule
	out = append(out, oggPage(7, 3, 1, 0x04, packets[3:])...)
	return out
}

func TestOpusFrameLengths(t *testing.T) {
	for _, c := range []struct {
		toc  []byte
		want int
	}{
		{[]byte{0xf8}, 960},        // CELT config 31: 20 ms, one frame
		{[]byte{0xf9}, 1920},       // two frames
		{[]byte{0xfb, 0x03}, 2880}, // code 3: three frames
		{[]byte{0x08}, 960},        // SILK config 1: 20 ms
		{[]byte{0x18}, 2880},       // SILK config 3: 60 ms
		{[]byte{0x78}, 960},        // Hybrid config 15: 20 ms
		{[]byte{0x80}, 120},        // CELT config 16: 2.5 ms
	} {
		if got := opusSamples(c.toc); got != c.want {
			t.Errorf("toc %x: got %d, want %d", c.toc, got, c.want)
		}
	}
}

func TestVoiceNoteIsRepackedCleanly(t *testing.T) {
	var audio [][]byte
	for i := 0; i < 150; i++ { // 3 seconds of 20 ms frames
		audio = append(audio, frame20ms(i))
	}
	out, seconds, err := normalizeOpus(androidLike(audio))
	if err != nil {
		t.Fatal(err)
	}
	if seconds != 3 {
		t.Fatalf("seconds %d", seconds)
	}
	got, err := oggPackets(out)
	if err != nil {
		t.Fatal(err)
	}
	if string(got[0][:8]) != "OpusHead" || string(got[1][:8]) != "OpusTags" || len(got) != 2+len(audio) {
		t.Fatalf("packets: %d", len(got))
	}
	for i, p := range audio {
		if !bytes.Equal(got[2+i], p) {
			t.Fatalf("packet %d changed", i)
		}
	}
	// Every page: a valid CRC, increasing sequence numbers, granules that end at the total samples, EOS last.
	b, seq := out, uint32(0)
	var last []byte
	for len(b) > 0 {
		nseg := int(b[26])
		size := 0
		for _, l := range b[27 : 27+nseg] {
			size += int(l)
		}
		page := append([]byte(nil), b[:27+nseg+size]...)
		crc := binary.LittleEndian.Uint32(page[22:26])
		binary.LittleEndian.PutUint32(page[22:26], 0)
		if oggCRC(page) != crc {
			t.Fatalf("page %d: bad CRC", seq)
		}
		if binary.LittleEndian.Uint32(page[18:22]) != seq {
			t.Fatalf("page %d: sequence", seq)
		}
		seq++
		last = b[:27]
		b = b[27+nseg+size:]
	}
	if binary.LittleEndian.Uint64(last[6:14]) != 150*960 || last[5]&0x04 == 0 {
		t.Fatalf("last page: granule %d flags %x", binary.LittleEndian.Uint64(last[6:14]), last[5])
	}
}

func TestNotOpusIsRefused(t *testing.T) {
	if _, _, err := normalizeOpus([]byte("ftypM4A ")); err == nil {
		t.Fatal("an AAC file isn't Ogg Opus")
	}
	if _, _, err := normalizeOpus(oggPage(1, 0, 0, 2, [][]byte{[]byte("hello")})); err == nil {
		t.Fatal("an Ogg file without OpusHead isn't Opus")
	}
}

func TestAPacketSplitAcrossPagesIsJoined(t *testing.T) {
	big := bytes.Repeat([]byte{1}, 600)
	// Page one ends mid-packet (lacing 255, 255: no terminator); page two continues it (flag 1, lacing 90).
	raw := func(seq uint32, flags byte, lacing, body []byte) []byte {
		h := make([]byte, 27)
		copy(h, "OggS")
		h[5] = flags
		binary.LittleEndian.PutUint32(h[18:22], seq)
		h[26] = byte(len(lacing))
		return append(append(h, lacing...), body...)
	}
	in := append(raw(0, 0, []byte{255, 255}, big[:510]), raw(1, 1, []byte{90}, big[510:])...)
	got, err := oggPackets(in)
	if err != nil || len(got) != 1 || !bytes.Equal(got[0], big) {
		t.Fatalf("got %d packets, err %v", len(got), err)
	}
}
