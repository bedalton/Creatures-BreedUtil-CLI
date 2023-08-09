package com.bedalton.creatures.breed.converter.cli

import com.bedalton.creatures.breed.converter.cli.internal.AlterAppearanceSubCommand
import com.bedalton.creatures.breed.converter.cli.internal.defaultCommandName
import com.bedalton.creatures.breed.converter.cli.internal.getArgsWithAsking
import com.bedalton.common.util.like
import com.bedalton.app.exitNative
import com.bedalton.app.exitNativeOk
import com.bedalton.common.coroutines.mapAsync
import com.bedalton.app.setIsCLI
import com.bedalton.common.util.Platform
import com.bedalton.common.util.platform
import com.bedalton.log.*
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
    if (args.any { it like "--verbose" }) {
        Log.setMode(LOG_DEBUG, true)
        Log.setMode(LOG_VERBOSE, true)
    }
    var theArgs = args.filterNot { it like "--debug" || it like "--verbose" }.toTypedArray()
    Log.iIf(LOG_VERBOSE) { "runMain" }
    setIsCLI(true)
    Log.iIf(LOG_VERBOSE) { "Set IS CLI" }
    val parser = ArgParser(commandName)
    Log.iIf(LOG_VERBOSE) { "Did init Arg Parser" }
    // Create subcommand instances
    val convertBreedSubcommand = ConvertBreedSubcommand(coroutineContext, jobs)
    val alterAppearanceSubCommand = AlterAppearanceSubCommand(coroutineContext, jobs)
    val printGeneData = PrintGeneDataCLI(coroutineContext, jobs)
    val convertBreedAskSubCommand = ConvertBreedAskCli(coroutineContext, jobs)
    val printSchemaCommand = ProtoSchemaCLI(coroutineContext, jobs)
    // Add subcommands to parse
    val subcommands = arrayOf(
        convertBreedSubcommand,
        convertBreedAskSubCommand,
        alterAppearanceSubCommand,
        printGeneData,
        printSchemaCommand
    )

    Log.iIf(LOG_VERBOSE) { "Setting subcommands" }
    parser.subcommands(*subcommands)
    Log.iIf(LOG_VERBOSE) { "Set subcommands" }

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
    if (platform == Platform.WINDOWS) {
        Log.i { "* Press enter to exit *" }
        readln()
    }
    CompletableDeferred(code)
}.await()
