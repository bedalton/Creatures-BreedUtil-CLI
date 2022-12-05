package bedalton.creatures.breed.converter.cli.internal

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal expect val MainDispatcher: CoroutineDispatcher
internal expect val BackgroundDispatcher: CoroutineDispatcher


private fun CoroutineScope.launchNow(start: CoroutineStart, callback: suspend () -> Unit): Job {
    return launch(coroutineContext, start = start) {
        try {
            callback()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}

private fun <T> CoroutineScope.async(start: CoroutineStart, callback: suspend () -> T): Deferred<T> {
    return async(coroutineContext, start = start) {
        try {
            callback()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}