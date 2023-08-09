@file:Suppress("SpellCheckingInspection")

package com.bedalton.creatures.breed.converter.cli

import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.Flag
import com.bedalton.common.util.PathUtil
import com.bedalton.creatures.breed.converter.cli.internal.MapOptionArg
import com.bedalton.creatures.breed.converter.cli.internal.outputAllProtobufSchemas
import com.bedalton.log.Log
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class ProtoSchemaCLI(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
) : Subcommand(name = "proto-schema", "Outputs the genome support file proto3 schema files") {

    private val output by option(
        ArgType.String,
        "output",
        "o",
        "File or directory to write proto schemas to"
    )

    private val packageName by option(
        ArgType.String,
        "package",
        shortName = "p",
        description = "The package to use for protofile output"
    )

    private val options by option(
        MapOptionArg,
        "option",
        description = "Optional proto buf schema generator options"
    ).multiple()

    private val includeGeneBytePairs by option(
        Flag,
        "include-gene-byte-pairs-protos",
        description = "Output schemas for gene byte pairs, which separate genes by header and data"
    ).default(false)

    override fun execute() {
        jobs.add(GlobalScope.async(coroutineContext) {
            run()
        })
    }


    private suspend fun run(): Int {
        var output = output
        if (output != null && !PathUtil.isAbsolute(output)) {
            val cwd = getCurrentWorkingDirectory()
                ?: exitNativeWithError(1, "Cannot parse genome without absolute path")
            output = PathUtil.combine(cwd, output)
        }
        val files = outputAllProtobufSchemas(
            output,
            packageName = packageName,
            options.toMap(),
            includeGeneBytePairs = includeGeneBytePairs
        )
        Log.i { "Wrote Proto Files:\n\t" + files.joinToString("\n\t") }
        return 0

    }
}