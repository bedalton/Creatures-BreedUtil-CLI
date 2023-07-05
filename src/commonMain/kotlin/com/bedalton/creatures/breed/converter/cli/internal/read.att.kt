package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.breed.converter.breed.BreedRegexUtil.getBreedSpriteFileRegex
import com.bedalton.creatures.breed.converter.breed.ConvertBreedTask
import com.bedalton.creatures.common.util.getGenusInt
import com.bedalton.app.exitNativeWithError
import com.bedalton.cli.unescapeCLIPathAndQualify
import com.bedalton.common.util.ensureEndsWith
import com.bedalton.common.util.isNotNullOrEmpty
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.common.util.pathSeparatorChar
import com.bedalton.log.ConsoleColors
import com.bedalton.log.ConsoleColors.BOLD
import com.bedalton.log.Log
import com.bedalton.vfs.ERROR_CODE__FAILED
import com.bedalton.vfs.FileSystem
import com.bedalton.vfs.MissingFilesException
import com.bedalton.vfs.unpackPathsSafe


val askATTPrompt = "${BOLD}Enter source ATT directory${ConsoleColors.RESET}: (type or drag folder into window, then press enter)\n\t- "


internal suspend fun readAttDirectory(fs: FileSystem, task: ConvertBreedTask, basePath: String, filesIn: List<String>): ConvertBreedTask {

    // Check if we should convert atts
    if (!yes("${BOLD}Convert Atts?${ConsoleColors.RESET}")) {
        return task
    }

    val genusString = task.getInputBreedGenus()
        ?: exitNativeWithError(ERROR_CODE__FAILED) { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Input genus set failed in an earlier step${ConsoleColors.RESET}" }
    val genus = getGenusInt(genusString).let {
        Pair(it.digitToChar(), (it + 4).digitToChar())
    }
    val regex = getBreedSpriteFileRegex(genus, task.getInputBreed()!![0], setOf("att"))

    // processFilesArgs
    val attFilesInFolder = filesIn
        .firstOrNull { path ->
            listOf(path).unpackPathsSafe(fs, setOf("att"), regex, root = basePath).isNotEmpty()
        }
    if (attFilesInFolder.isNotNullOrEmpty()) {
        return task.withAttDirectory(attFilesInFolder)
    }
    var hits = 0
    while (true) {
        val temp = readLineCancellable(askATTPrompt)
            ?.trim()
            ?.nullIfEmpty()
            ?.let {
                unescapeCLIPathAndQualify(it, basePath)
            }
        if (temp == null) {
            if (yes("${BOLD}Cancel converting ATTs (sprite conversion will still continue)?${ConsoleColors.RESET}")) {
                return task
            }
            continue
        }
        val temp2 = if (!temp.endsWith(".att") && !temp.endsWith("*")) {
            temp.ensureEndsWith(pathSeparatorChar) + "*.att"
        } else null
        val atts = try {
            listOf(temp)
                .unpackPathsSafe(fs, setOf("att"), regex, root = basePath)
                .nullIfEmpty() ?: temp2?.let {
                listOf(temp2)
                    .unpackPathsSafe(fs, setOf("att"), regex)
                    .nullIfEmpty()
            }
        } catch (e: MissingFilesException) {
            Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Failed to locate matching ATTs in $temp" + ConsoleColors.RESET }
            if (hits++ > 2) {
                hits *= -1
                Log.i { wishToExit }
            }
            continue
        }
        if (atts == null) {
            Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "No matching ATTs found in $temp${ConsoleColors.RESET}" }
            continue
        }
        task.withAttDirectory(temp)
        return task
    }
}
