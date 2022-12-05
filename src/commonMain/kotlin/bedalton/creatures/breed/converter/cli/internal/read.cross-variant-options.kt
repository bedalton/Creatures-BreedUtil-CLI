package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.cli.ConsoleColors
import bedalton.creatures.common.structs.GameVariant
import bedalton.creatures.common.structs.isC1e
import bedalton.creatures.common.structs.isC2e
import bedalton.creatures.common.util.FileNameUtil
import bedalton.creatures.common.util.Log
import bedalton.creatures.breed.converter.breed.getFromGame

internal suspend fun readProgressiveArms(task: ConvertBreedTask, toGame: GameVariant, fromGame: GameVariant?): ConvertBreedTask {

    // If C1e -> C2e, convert arms progressive?
    if (toGame.isC2e && (fromGame == null || fromGame.isC1e)) {
        Log.i {
            "${ConsoleColors.WHITE_BACKGROUND}${ConsoleColors.BLACK}If converting from C1e to C2e, front facing arm poses can be mimicked using side arm poses.\n" +
                    "\t*This feature is experimental and may require manual ATT editing for best results*${ConsoleColors.RESET}"
        }
        if (yes("${ConsoleColors.BOLD}Use experimental C1e to C2e arm conversion feature?${ConsoleColors.RESET}")) {
            task.withProgressive(true)
        }
    }
    return task
}

internal suspend fun readGenerateTails(task: ConvertBreedTask, toGame: GameVariant, breedFiles: List<String>): ConvertBreedTask {
    // Add tail if C1 -> C2+
    if (task.getFromGame() == GameVariant.C1 && toGame != GameVariant.C1 && breedFiles.none {
            val part = FileNameUtil.getLastPathComponent(it)?.getOrNull(0)?.lowercaseChar()
            part == 'n' || part == 'm'
        }) {
        val noTails = !yes("${ConsoleColors.BOLD}Would you like to create tail files?${ConsoleColors.RESET}", true)
        task.withNoTails(noTails)
    }
    return task
}

internal suspend fun readProgressAges(task: ConvertBreedTask, toGame: GameVariant, fromGame: GameVariant?): ConvertBreedTask {
    // Should progress game ages from C1 to C2 or vice-versa
    if (toGame.isC1e != fromGame?.isC1e) {
        val noProgression = !yes(
            "${ConsoleColors.BOLD}Would you like to progress age number to match target game${ConsoleColors.RESET}",
            true
        )
        task.withNoAgeProgression(noProgression)
    }
    return task
}