package bedalton.creatures.breed.converter.cli.internal

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal actual val MainDispatcher get() = Dispatchers.Default

internal actual val BackgroundDispatcher get() = Dispatchers.Default