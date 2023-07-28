package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.common.util.Platform
import com.bedalton.common.util.platform
import com.bedalton.log.ConsoleColors


val whiteBackgroundBlackText get() = if(platform == Platform.WINDOWS) {
    ConsoleColors.WHITE_BACKGROUND + ConsoleColors.BLACK
} else {
    ConsoleColors.WHITE_BACKGROUND + ConsoleColors.BLACK
}
