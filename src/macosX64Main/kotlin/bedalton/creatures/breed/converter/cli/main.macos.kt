package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.cli.internal.BackgroundDispatcher
import bedalton.creatures.common.util.Log
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking(BackgroundDispatcher) {
        runMain(args)
    }
}