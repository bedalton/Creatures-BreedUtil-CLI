package com.bedalton.creatures.breed.converter.cli

import com.bedalton.common.coroutines.BackgroundDispatcher
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking(BackgroundDispatcher)  {
        runMain(args)
    }
}