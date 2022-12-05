package bedalton.creatures.breed.cli.internal

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal actual object MainDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        CoroutineScope(context).launch {
            block.run()
        }
    }
}

internal actual object BackgroundDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        CoroutineScope(context).launch {
            block.run()
        }
    }
}