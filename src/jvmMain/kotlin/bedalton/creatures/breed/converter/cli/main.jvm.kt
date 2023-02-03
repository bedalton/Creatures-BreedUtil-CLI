package bedalton.creatures.breed.converter.cli

import com.bedalton.common.coroutines.BackgroundDispatcher
import com.bedalton.log.Log
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking(BackgroundDispatcher) {
        runMain(args)
    }
}