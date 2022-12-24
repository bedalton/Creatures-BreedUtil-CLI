package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.cli.internal.AlterAppearanceSubCommand
import bedalton.creatures.breed.converter.cli.internal.defaultCommandName
import bedalton.creatures.breed.converter.cli.internal.getArgsWithAsking

import bedalton.creatures.cli.*
import bedalton.creatures.common.util.*
import com.bedalton.app.exitNative
import com.bedalton.app.exitNativeOk
import com.bedalton.app.mapAsync
import com.bedalton.app.setIsCLI
import kotlinx.cli.ArgParser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope


suspend fun runMain(args: Array<String>, commandName: String = defaultCommandName): Int = coroutineScope {

    setIsCLI(true)

    val jobs = mutableListOf<Deferred<Int>>()
    // Enable colors on Windows CLI
    enableCLIColors()
    if (args.any { it like "--debug" }) {
        Log.setMode(LOG_DEBUG, true)
    }
    var theArgs = args.filterNot { it like "--debug" }.toTypedArray()
    Log.iIf(LOG_DEBUG) { "runMain" }
    setIsCLI(true)
    Log.iIf(LOG_DEBUG) { "Set IS CLI" }
    val parser = ArgParser(commandName)
    Log.iIf(LOG_DEBUG) { "Did init Arg Parser" }
    // Create subcommand instances
    val convertBreedSubcommand = ConvertBreedSubcommand(coroutineContext, jobs)
    Log.iIf(LOG_DEBUG) { "Convert Breed Added" }
    val alterAppearanceSubCommand = AlterAppearanceSubCommand(coroutineContext, jobs)
    Log.iIf(LOG_DEBUG) { "Alter Appearance Added" }
    val convertBreedAskSubCommand = ConvertBreedAskCli(coroutineContext, jobs)
    Log.iIf(LOG_DEBUG) { "Convert breed Ask Added" }

    // Add subcommands to parse
    val subcommands = arrayOf(convertBreedSubcommand, convertBreedAskSubCommand, alterAppearanceSubCommand)

    Log.iIf(LOG_DEBUG) { "Setting subcommands" }
    parser.subcommands(*subcommands)
    Log.iIf(LOG_DEBUG) { "Set subcommands" }

    // Get command names to possibly insert the ASK cli sub command to args
    theArgs = getArgsWithAsking(
        commandName,
        theArgs,
        subcommands,
        convertBreedAskSubCommand.name
    )

    Log.iIf(LOG_DEBUG) { "Args: [${args.joinToString()}]" }
    val code = try {
        parser.parse(theArgs)
        val results = jobs.mapAsync { it.await() }
        if (results.all { it == 0 }) {
            0
        } else if (results.size == 1) {
            results[0]
        } else {
            8000
        }
    } catch (e: IllegalStateException) {
        if (e.message?.trim() like "Not implemented for JS!") {
            Log.i { "..." }
            exitNativeOk()
        }
        Log.e { "Hmmn... ${e.message}" }
        exitNative(5000)
    } catch (e: Exception) {
        Log.e {
            e::class.simpleName + ": " + e.message + "\n\tArgs: ${theArgs.joinToString()}"
        }
        1
    }
    Log.i { "Done.." }
    Log.i { "* Press enter to exit *" }
    readln()
    CompletableDeferred(code)
}.await()
