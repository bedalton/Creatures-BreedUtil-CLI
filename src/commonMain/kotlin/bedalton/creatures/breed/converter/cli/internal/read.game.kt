package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.common.bytes.MemoryByteStreamReader
import bedalton.creatures.common.structs.GameVariant
import bedalton.creatures.common.util.FileNameUtil
import bedalton.creatures.sprite.parsers.SpriteParser
import com.bedalton.vfs.FileSystem
import bedalton.creatures.cli.ConsoleColors.BOLD
import bedalton.creatures.cli.ConsoleColors.RED
import bedalton.creatures.cli.ConsoleColors.RESET
import bedalton.creatures.cli.ConsoleColors.WHITE_BACKGROUND
import bedalton.creatures.common.util.Log
import bedalton.creatures.common.util.nullIfEmpty
import bedalton.creatures.breed.converter.breed.withToGame


/**
 * Read in game code [C1, C2, CV, C3, DS]
 */
internal suspend fun readGame(task: ConvertBreedTask): GameVariant {
    var variant: GameVariant? = null
    while (variant == null) {
        val variantString =
            readLineCancellable("${BOLD}Enter target game: ${BOLD}C1$RESET, ${BOLD}C2${RESET} or ${BOLD}C3$RESET")
                ?.trim()
                ?.uppercase()
                ?.nullIfEmpty()
                ?: continue
        variant = GameVariant.fromString(variantString)
            ?: continue
        if (variant == GameVariant.SM || variant == GameVariant.CV) {
            Log.e { WHITE_BACKGROUND + RED + "Cannot convert ${variant!!.code} breeds$RESET" }
            variant = null
            continue
        }
        if (variant == GameVariant.DS) {
            variant = GameVariant.C3
        }
    }
    task.withToGame(variant)
    return variant
}

@Suppress("SpellCheckingInspection")
internal suspend fun inferVariant(fs: FileSystem, filesIn: List<String>): GameVariant? {
    val files = filesIn.sortedBy {
        FileNameUtil.getFileNameWithoutExtension(it.trim())?.lowercase() ?: "ZZZZZZZZZ"
    }
    return files.firstNotNullOfOrNull map@{
        val file = it.trim()
        val fileName = FileNameUtil.getLastPathComponent(file)
            ?: return@map null
        try {
            val reader = MemoryByteStreamReader(fs.read(file))
            SpriteParser.getBodySpriteVariant(fileName, reader, null)
        } catch (_: Exception) {
            null
        }
    }
}

