import bedalton.creatures.breed.converter.cli.runMain
import com.bedalton.common.util.PathUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlin.js.Promise

@OptIn(DelicateCoroutinesApi::class)
@JsExport
fun runBreedCli(): Promise<Int> {
    var args = process.argv.toList()

    // ========= USES endsWith() -> because args are absolute paths =====//
    if (args.isNotEmpty() && args[0].lowercase().endsWith("npm")) {
        args = args.drop(1)
    }
    if (args.isNotEmpty() && args[0].lowercase().endsWith("node")) {
        args = args.drop(1)
    }
    if (args.isNotEmpty() && args[0].lowercase().endsWith("nodejs")) {
        args = args.drop(1)
    }
    if (args.isNotEmpty() && PathUtil.isAbsolute(args[0])) {
        args = args.drop(1)
    }
    if (args.isNotEmpty() && args[0].lowercase().endsWith(".exe")) {
        args = args.drop(1)
    }
    if (args.isNotEmpty() && args[0].lowercase().endsWith(".js")) {
        args = args.drop(1)
    }
    if (args.isNotEmpty() && (args[0].endsWith("/breed-util") || args[0].endsWith("\\breed-util"))) {
        args = args.drop(1)
    }
    return GlobalScope.async {
        runMain(args.toTypedArray(), "breed-util")
    }.asPromise()
}

@JsExport
fun runCliWithArray(args: Array<String>): Promise<Boolean> {
    return GlobalScope.async {
        runMain(args) == 0
    }.asPromise()
}
