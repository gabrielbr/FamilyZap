package app.oie.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Where screens open and decode files (photos, frames, avatars) off the main thread (v0.34). The
 * screenshot tests swap in an immediate dispatcher, so what they capture never depends on timing.
 */
object Background {
    @Volatile var dispatcher: CoroutineDispatcher = Dispatchers.IO
}
