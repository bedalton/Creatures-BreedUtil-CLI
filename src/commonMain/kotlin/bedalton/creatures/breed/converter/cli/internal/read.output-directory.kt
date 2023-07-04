package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.breed.converter.cli.ConvertBreedSubcommandBase
import com.bedalton.app.exitNativeWithError
import com.bedalton.cli.unescapeCLIPathAndQualify
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.log.ConsoleColors
import com.bedalton.log.Log
import com.bedalton.vfs.ERROR_CODE__BAD_OUTPUT_DIRECTORY
import com.bedalton.vfs.unescapePath

internal suspend fun readOutputDirectory(task: ConvertBreedTask, baseDirectory: String, subcommand: ConvertBreedSubcommandBase): ConvertBreedTask {

    // Get output directory
    Log.i {
        "${ConsoleColors.BOLD}Output Folder: \n\t${ConsoleColors.RESET}(type or drag in the folder where your ${ConsoleColors.BOLD}output${ConsoleColors.RESET} files should go, then press enter)"
    }
    val outputDirectory: String = (subcommand.outputDirectory ?: readLineCancellable("\t-"))
        ?.trim()
        ?.unescapePath()
        ?.nullIfEmpty()
        ?.let {
            unescapeCLIPathAndQualify(it, baseDirectory)
        }
        ?: baseDirectory

    if (!PathUtil.isAbsolute(outputDirectory)) {
        exitNativeWithError(ERROR_CODE__BAD_OUTPUT_DIRECTORY) {
            "Output directory cannot be null or non-absolute"
        }
    }
    task.withOutputDirectory(outputDirectory)
    return task
}