package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.common.util.getGenusInt
import com.bedalton.app.exitNativeWithError
import kotlinx.cli.ArgType


internal class PartBreedArg(
    override val description: kotlin.String = "Expected pattern  \"norn:a\" or \"n:g\"; Remember 'g:' is grendel. use 's:' for geat",
) : ArgType<Pair<Int, Char>>(true) {
    override fun convert(value: kotlin.String, name: kotlin.String): Pair<kotlin.Int, Char> {
        val regex = "^([^:\\-]+)[\\-:]([a-z\\d])".toRegex()
        val parts = regex.matchEntire(value)?.groupValues?.drop(1)
            ?: exitNativeWithError(1) { "Invalid part breed. Expected pattern \"norn-a\" or \"norn:a\"" }
        val genus = getGenusInt(parts[0])
        return Pair(genus, parts[1][0])
    }
}
