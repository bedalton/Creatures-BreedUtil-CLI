package bedalton.creatures.breed.cli

import bedalton.creatures.breed.cli.internal.BackgroundDispatcher
import bedalton.creatures.common.util.Log
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking(BackgroundDispatcher) {
        Log.i { "main(args)" }
        runMain(args)
    }
}