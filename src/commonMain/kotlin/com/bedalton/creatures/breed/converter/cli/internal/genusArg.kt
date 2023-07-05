package com.bedalton.creatures.breed.converter.cli.internal

import kotlinx.cli.ArgType


internal val GenusArg = ArgType.Choice(
    listOf("n", "norn", "g", "grendel", "e", "ettin", "s", "shee", "geat"),
    {
        it
    }
)