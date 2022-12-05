package bedalton.creatures.breed.cli

import bedalton.creatures.common.util.Log
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking {
        Log.i { "main(args)" }
        runMain(args)
    }
}