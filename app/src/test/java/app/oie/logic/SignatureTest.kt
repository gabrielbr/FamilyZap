package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SignatureTest {
    @Test fun textGetsTheKidsName() = assertEquals("Tito: Oi, vó", Signature.sign("Tito", "Oi, vó"))
    @Test fun aPhotoOrStickerCarriesTheNameAlone() = assertEquals("Tito", Signature.sign(" Tito ", ""))
    @Test fun noNameNoChange() = assertEquals("Oi", Signature.sign("", "Oi"))
}
