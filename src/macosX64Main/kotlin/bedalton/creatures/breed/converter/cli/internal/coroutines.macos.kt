package bedalton.creatures.breed.converter.cli.internal

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal actual val MainDispatcher: CoroutineDispatcher get() = Dispatchers.Main

internal actual val BackgroundDispatcher get() = Dispatchers.Default