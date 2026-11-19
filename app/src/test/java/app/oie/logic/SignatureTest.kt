package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SignatureTest {
    @Test fun textGetsTheKidsNameInBold() = assertEquals("*Tito*: Oi, vó", Signature.sign("Tito", "Oi, vó"))
    @Test fun withTheParentsEmoji() = assertEquals("😎*Tito*: Oi, vó", Signature.sign("Tito", "Oi, vó", "😎"))
    @Test fun aPhotoOrStickerCarriesTheNameAlone() = assertEquals("😎*Tito*", Signature.sign(" Tito ", "", "😎"))
    @Test fun noNameNoChange() = assertEquals("Oi", Signature.sign("", "Oi", "😎"))
    @Test fun aStarInTheNameCantBreakTheBold() = assertEquals("*Tito*: Oi", Signature.sign("*Tito*", "Oi"))

    @Test fun onlyTheFirstEmojiIsKept() {
        assertEquals("😎", Signature.emojiOf(" 😎🎉 "))
        assertEquals("👍🏽", Signature.emojiOf("👍🏽"))
        assertEquals("👨‍👩‍👧", Signature.emojiOf("👨‍👩‍👧 oi"))
        assertEquals("❤️", Signature.emojiOf("❤️"))
        assertEquals("⭐", Signature.emojiOf("⭐"))
        assertEquals("🇧🇷", Signature.emojiOf("🇧🇷🇵🇹"))
    }

    @Test fun lettersDigitsAndPunctuationAreNotAnEmoji() {
        for (t in listOf("a", "Tito", "1", "*", ":", "", "  ")) assertEquals(t, "", Signature.emojiOf(t))
    }
}
