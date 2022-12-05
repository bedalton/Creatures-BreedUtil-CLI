package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.cli.ConsoleColors
import bedalton.creatures.cli.promptYesNo
import bedalton.creatures.cli.readLine
import bedalton.creatures.common.util.Log
import bedalton.creatures.common.util.nullIfEmpty
import bedalton.creatures.common.util.stripSurroundingQuotes
import com.bedalton.app.exitNativeOk


internal val wishToExit = "\t${ConsoleColors.BOLD}Wish to exit?${ConsoleColors.RESET} Type ${ConsoleColors.BOLD}\"exit\"${ConsoleColors.RESET} to cancel conversion"

internal suspend fun readLineCancellable(prompt: String): String? {
    val needsTail = !prompt.endsWith(':') &&
            !prompt.endsWith('\n') &&
            !prompt.trim().endsWith('-') &&
            !prompt.trim().endsWith('?')
    val tail = if (needsTail) {
        "\n\t- "
    } else if (!prompt.endsWith(' ')) {
        " "
    } else {
        ""
    }
    val response = readLine(prompt + tail)
        ?.trim()
        ?: return null
    if (response.lowercase() == "cancel" || response.lowercase() == "exit") {
        exitNativeOk("Conversion has been cancelled")
    }
    return response
}

internal suspend fun yes(prompt: String, default: Boolean? = null, shouldAbortOnNothing: (suspend () -> Boolean?)? = null): Boolean {
    return yesNullable(prompt, default, shouldAbortOnNothing) == true
}

internal suspend fun yesNullable(prompt: String, default: Boolean? = null, shouldAbortOnNothing: (suspend () -> Boolean?)? = null): Boolean? {
    while (true) {
        val temp = readLineCancellable(
            "$prompt: " +
                    "[${ConsoleColors.BOLD}Y${ConsoleColors.RESET}]es${if (default == true) "(default)" else ""}, " +
                    "[${ConsoleColors.BOLD}N${ConsoleColors.RESET}]o?${if (default == false) "(default)" else ""}: " +
                    "\n\t- "
        )
            ?.stripSurroundingQuotes(2, true)
            ?.nullIfEmpty()
        if (temp == null) {
            if (default != null) {
                return default
            }
            val abort = shouldAbortOnNothing?.invoke()
            if (abort != null && abort == true) {
                return null
            }
            continue
        }
        when (temp.lowercase()) {
            "y", "yes", "true" -> return true
            "n", "no", "false" -> return false
            else -> Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Invalid [Y]es, [N]o value" + ConsoleColors.RESET }
        }
    }
}