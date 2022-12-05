@file:Suppress("SpellCheckingInspection")

package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.cli.ConsoleColors.BOLD
import bedalton.creatures.cli.ConsoleColors.RESET
import bedalton.creatures.common.util.IOException
import bedalton.creatures.common.util.nullIfEmpty
import bedalton.creatures.genetics.gene.Gene
import bedalton.creatures.genetics.gene.GeneFlags
import bedalton.creatures.genetics.gene.GeneHeader
import kotlinx.cli.ArgType


class GeneArg<GeneT : Gene>(
    private val params: List<GeneParam>,
    private val commandDescription: kotlin.String,
    private val example: kotlin.String,
    private val make: (header: GeneHeader, opts: Map<kotlin.String, kotlin.String>) -> GeneT
) : ArgType<GeneOption<GeneT>>(true) {

    override val description: kotlin.String by lazy {
        val options = "\n\t- " + (this.params + headerParams).joinToString("\n\t- ") { param ->
            val optionText = param.optionText?.let { if (it.contains(BOLD)) it else "$BOLD${param.name}$RESET" }
                ?: "$BOLD${param.name}$RESET/${param.shortName}"
            val description = param.description.nullIfEmpty()?.let { " - $it" } ?: ""
            "$optionText$description"
        }
        val out = """
            $commandDescription
            Format:{opt1}:{value1},{opt2}:{value2}{SCOPE}, etc. 
            Example: $example
            ${BOLD}Modifier$RESET:
                - $BOLD+$RESET - Add a new pigment gene
                - $BOLD<NOTHING>/?$RESET - Replace or add if no replaceable gene can be found
                - $BOLD!$RESET - Replace only, ignoring command if no replaceable can be found
            ${BOLD}Options$RESET:$options
        """.trimIndent()
        out
    }

    override fun convert(value: kotlin.String, name: kotlin.String): GeneOption<GeneT> {
        return parse(params, value, make)
    }


    companion object {
        internal val paramSeparator = "[,;]".toRegex()
        internal val equalSign = "[:=]".toRegex()

        private val headerParams = listOf(
            GeneParam("age", "age", "${BOLD}[a]${RESET}ge", "Gene activation age"),
            GeneParam(
                "gender",
                "g",
                "${BOLD}[g]${RESET}ender",
                "Gender constraint for gene. Values: 1 or [m]ale; 2 or [f]emale; [a]ny"
            ),
            GeneParam(
                "mut-weight",
                "w",
                "mut-${BOLD}[w]${RESET}eight",
                "[t]rue or [f]alse - Allow deletion of gene on cross"
            ),
            GeneParam(
                "mutable",
                "mut",
                "${BOLD}[mut]${RESET}able",
                "[t]rue or [f]alse - Allow gene to mutate on cross"
            ),
            GeneParam(
                "duplicable",
                "dup",
                "${BOLD}[dup]${RESET}licable",
                "[t]rue or [f]alse - Allow duplication of gene on cross"
            ),
            GeneParam(
                "duplicable",
                "dup",
                "${BOLD}[del]${RESET}letable",
                "[t]rue or [f]alse - Allow deletion of gene on cross"
            )
        )
    }
}


data class GeneOption<GeneT : Gene>(
    val gene: GeneT,
    val replace: Int,
)

data class GeneParam(
    val name: String,
    val shortName: String? = null,
    val optionText: String? = null,
    val description: String? = null
)


private fun <GeneT: Gene> parse(params: List<GeneParam>, value: String, make: (geneHeader: GeneHeader, params: Map<String, String>) -> GeneT): GeneOption<GeneT> {
    val all = params.flatMap { listOfNotNull(it.name.lowercase(), it.shortName?.lowercase()) }
    if (all.size != all.distinct().size) {
        val duplicate = all.filter { param -> all.count { it == param } > 1 }
        throw IOException("Duplicate option: [${duplicate.joinToString(", ")}]")
    }
    if (value.isBlank()) {
        throw IOException("Gene value cannot be blank")
    }

    val replace = when (value.last()) {
        '?' -> 0
        '!' -> 1
        '+' -> 2
        else -> {
            when (value.first()) {
                '?' -> 0
                '!' -> 1
                '+' -> 2
                else -> 0
            }
        }
    }
    val values = value.trim(' ', '\t', '\n', '\r', '\\', ';', ',', '?', '+', '!').split(GeneArg.paramSeparator)
        .filterNot { it.isBlank() }.map { it.trim() }

    val paramNames = params.map { it.name }
    val paramNamesLower = paramNames.map { it.lowercase() }
    val shortCodes = params.filter { it.shortName != null }.associate {
        it.shortName!!.lowercase() to it.name
    }

    val out: MutableMap<String, String> = values.associate { param ->
        val parts: List<String> = param.split(GeneArg.equalSign)
        if (parts.size != 2) {
            throw Exception("Invalid option $values. Expected format: {opt}:{value}; Found: $value")
        }
        var paramName: String = (shortCodes[parts[0].lowercase()] ?: parts[0])
        if (paramName !in paramNames) {
            if (paramName.lowercase() in paramNamesLower) {
                paramName = paramName.lowercase()
                paramName = params.firstOrNull { it.name.lowercase() == paramName }?.name ?: parts[0]
            }
        }
        paramName to parts[1]
    }.toMutableMap()

    // Update flags
    var flags = GeneFlags()
    // Deletable
    parseTrueFalse(out["deletable"])?.let {
        flags = flags.copy(
            deletable = it
        )
    }
    out.remove("deletable")

    // Mutable
    parseTrueFalse(out["mutable"])?.let {
        flags = flags.copy(
            mutable = it
        )
    }
    out.remove("mutable")

    // Duplicable
    parseTrueFalse(out["duplicable"])?.let {
        flags = flags.copy(
            duplicable = it
        )
    }
    out.remove("duplicable")

    (out["gender"] ?: out["sex"])?.let {
        val gender = parseMaleFemale(it)
            ?: throw IOException("Invalid gender selected $it; Expected: [m]ale, [f]emale, [a]ny")
        flags = flags.copy(
            maleOnly = gender == 1,
            femaleOnly = gender == 2
        )
    }
    out.remove("gender")

    var header = GeneHeader(
        3,
        flags
    )
    // Age
    out["age"]?.let {
        val age = it.toIntOrNull()
            ?: throw IOException("Invalid age value $it passed to gene constructor")
        header = header.copy(
            switchOnTimeInt = age
        )
    }
    out.remove("age")

    // MutationWeight
    (out["mut-weight"] ?: out["mutweight"] ?: out["mutation-weight"] ?: out["mutationweight"])?.let {
        val mutationWeight = it.toIntOrNull()
            ?: throw IOException("Invalid mutation weight \"$it\" passed to gene constructor; Expected integer")
        header = header.copy(
            mutationWeighting = mutationWeight
        )
    }

    return GeneOption(
        gene = make(header, out),
        replace = replace
    )
}



internal fun parseMaleFemale(value: String?): Int? {
    if (value == null) {
        return null
    }
    return when (value.lowercase()) {
        "m", "mal", "male", "1" -> 1
        "f", "fem", "female", "2" -> 2
        "0", "?", "any", "3" -> 0
        else -> null
    }
}

internal fun parseTrueFalse(value: String?): Boolean? {
    if (value == null) {
        return null
    }
    return when (value.lowercase()) {
        "t", "true", "1" -> true
        "f", "false", "0" -> false
        else -> null
    }
}