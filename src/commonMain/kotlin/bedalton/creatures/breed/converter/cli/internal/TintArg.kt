package bedalton.creatures.breed.converter.cli.internal

//import com.bedalton.log.ConsoleColors.BOLD
//import com.bedalton.log.ConsoleColors.RESET
//import bedalton.creatures.common.structs.GameVariant
//import bedalton.creatures.common.util.IOException
//import bedalton.creatures.genetics.gene.GeneFlags
//import bedalton.creatures.genetics.gene.GeneHeader
//import bedalton.creatures.genetics.gene.PigmentGene
//import kotlinx.cli.ArgType
//
//class PigmentArg(private val replace: kotlin.Int) : ArgType<PigmentOption>(true) {
//    val replaceText: Pair<kotlin.String, kotlin.String>
//        get() = when (replace) {
//            0 -> Pair("with", "Replace or Add")
//            1 -> Pair("replace-or-ignore", "Replace (or add if replaceable gene not found)")
//            2 -> Pair("add", "Adding")
//            else -> throw IOException("Invalid gene add-replace flag. Expected value 0..2")
//        }
//
//    val modifierText = ""
//
//    @Suppress("SpellCheckingInspection")
//    override val description: kotlin.String
//        get() = """
//            ${replaceText.second} a set of pigment genes described by color. (R,G,B represent different genes)
//            Format:{opt1}:{value1},{opt2}:{value2}{SCOPE}, etc.
//            Example: --${replaceText.first}-pigment-gene ${BOLD}r:255,a:0;s:1;mut:t,del:f,w:240$RESET -
//            ${BOLD}Modifier$RESET:
//                - $BOLD+$RESET - Add a new pigment gene
//                - $BOLD<NOTHING>/?$RESET - Replace or add if no replaceable gene can be found
//                - $BOLD?$RESET - Replace only, ignoring command if no replaceable can be found
//            ${BOLD}Options$RESET:" +
//                - [${BOLD}r$RESET]ed: integer
//                - [${BOLD}g$RESET]reen: integer
//                - [${BOLD}b$RESET]lue: integer
//                - [${BOLD}a$RESET]ge: integer - turn on age
//                - [${BOLD}s$RESET]ex
//                - mut-[${BOLD}w$RESET]ieght: integer - the mutation weight
//                - [${BOLD}mut$RESET]able: [t]rue, [f]alse - can mutable
//                - [${BOLD}dup$RESET]licable: [t]rue, [f]alse - gene can duplicicate
//                - [${BOLD}del$RESET]etable: [t]rue, [f]alse - gene can be deleted when crossed
//        """.trimIndent()
//
//    override fun convert(value: kotlin.String, name: kotlin.String): PigmentOption {
//        TODO("Not yet implemented")
//    }
//}
//
//data class PigmentOption(
//    val color: Int,
//    val amount: Int,
//    val replace: Int,
//    val age: Int = 0,
//    val gender: Int = 0,
//    val mutable: Boolean? = null,
//    val deletable: Boolean? = null,
//    val duplicable: Boolean? = null,
//    val mutationWeighting: Int? = null,
//    val geneVariant: Int? = null
//
//) {
//    fun gene(variant: GameVariant): PigmentGene {
//        val version = when (variant) {
//            GameVariant.C1 -> 1
//            GameVariant.C2 -> 2
//            else -> 3
//        }
//        val default = PigmentGene(version)
//        PigmentGene(
//
//        )
//    }
//
//    fun flags(version: Int): GeneHeader {
//        var flags = GeneFlags()
//        if (mutable != null) {
//            flags = flags.copy(mutable = mutable)
//        }
//        if (duplicable != null) {
//            flags = flags.copy(duplicable = duplicable)
//        }
//        if (deletable != null) {
//            flags = flags.copy(deletable = deletable)
//        }
//
//        var header = GeneHeader(version, flags = flags)
//        if (mutationWeighting != null) {
//            header = header.copy(
//                mutationWeighting = mutationWeighting
//            )
//        }
//        if ()
//    }
//}
