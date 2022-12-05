@file:OptIn(ExperimentalCli::class)

package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.cli.ASK_CLI_NAME
import bedalton.creatures.cli.*
import bedalton.creatures.common.util.*
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.coroutines.*
import kotlin.math.min

internal const val defaultCommandName = "BreedUtil"

internal fun getArgsWithAsking(
    commandName: String,
    args: Array<String>,
    subcommands: Array<Subcommand>,
    askSubCommand: String,
): Array<String> {
    if (args.isEmpty()) {
        return arrayOf(ASK_CLI_NAME)
    }
    Log.iIf(LOG_DEBUG) { "Get args with asking" }
    val subcommandNames = subcommands.map { it.name.lowercase() }
    var walk = true

    Log.iIf(LOG_DEBUG) { "Check walking" }
    for (i in 0..min(3, args.lastIndex)) {
        Log.iIf(LOG_DEBUG) { "Check args[$i]<${args[i]}>" }
        if (args[i].lowercase() in subcommandNames) {
            Log.iIf(LOG_DEBUG) { "is subcommand: ${args[i]}" }
            walk = false
            break
        }
    }
    var theArgs = args
    if (walk) {
        var inserted = false
        val out = mutableListOf<String>()
        for (i in 0..args.lastIndex) {
            Log.iIf(LOG_DEBUG) { "Adding Arg[$i]<${args[i]}>" }
            if (!inserted && args[i] == commandName) {
                Log.iIf(LOG_DEBUG) { "Inserted" }
                inserted = true
                out.add(ASK_CLI_NAME)
            }
        }
        if (!inserted) {
            Log.iIf(LOG_DEBUG) { "Add ask CLI" }
            out.add(askSubCommand)
        }
        theArgs = (out + args).toTypedArray()
    }

    Log.iIf(LOG_DEBUG) { "ArgsOut: [${theArgs.joinToString()}]" }
    return theArgs
}