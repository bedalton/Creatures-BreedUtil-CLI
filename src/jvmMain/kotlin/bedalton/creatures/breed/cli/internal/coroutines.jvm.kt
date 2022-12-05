package bedalton.creatures.breed.cli.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal actual object MainDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable)
            = Dispatchers.Main.dispatch(context, block)
}

internal actual object BackgroundDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable)
            = Dispatchers.IO.dispatch(context, block)
}