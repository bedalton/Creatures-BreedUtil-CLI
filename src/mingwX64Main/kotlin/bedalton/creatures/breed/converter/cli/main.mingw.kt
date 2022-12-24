package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.cli.internal.BackgroundDispatcher
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking(BackgroundDispatcher)  {
        runMain(args)
        bedalton.creatures.cli.readLine( "* Press \"enter\" to exit *" )
    }
}