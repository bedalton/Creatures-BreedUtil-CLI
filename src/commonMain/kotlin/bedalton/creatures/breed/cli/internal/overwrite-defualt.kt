package bedalton.creatures.breed.cli.internal

import bedalton.creatures.cli.shouldWriteFile
import com.bedalton.vfs.ShouldOverwrite
import com.bedalton.vfs.ShouldOverwriteCallbackAsync

fun createOverwriteCallback(
    overwriteExisting: Boolean? = null,
    overwriteNone: Boolean? = null,
    default: Boolean? = null
) : ShouldOverwriteCallbackAsync {
    return { path ->
        if (overwriteExisting == true) {
            ShouldOverwrite.YES
        } else if (overwriteNone == true) {
            ShouldOverwrite.NO
        } else {
            shouldWriteFile(
                path,
                default
            )
        }
    }
}