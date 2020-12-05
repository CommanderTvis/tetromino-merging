import CellMatrix.Companion.fromLines

val FixedTetrominoesList by lazy {
    listOf(
        FixedTetrominoes.L1,
        FixedTetrominoes.L2,
        FixedTetrominoes.L3,
        FixedTetrominoes.L4,
        FixedTetrominoes.L5,
        FixedTetrominoes.L6,
        FixedTetrominoes.L7,
        FixedTetrominoes.L8,
        FixedTetrominoes.Z1,
        FixedTetrominoes.Z2,
        FixedTetrominoes.S1,
        FixedTetrominoes.S2,
        FixedTetrominoes.T1,
        FixedTetrominoes.T2,
        FixedTetrominoes.T3,
        FixedTetrominoes.T4,
        FixedTetrominoes.I1,
        FixedTetrominoes.I2,
        FixedTetrominoes.O1,
    )
}

object FixedTetrominoes {
    val L1 = fromLines("+*", "+*", "**").toImmutable()
    val L2 = fromLines("**", "+*", "+*").toImmutable()
    val L3 = fromLines("***", "++*").toImmutable()
    val L4 = fromLines("++*", "***").toImmutable()
    val L5 = fromLines("**", "*+", "*+").toImmutable()
    val L6 = fromLines("*+", "*+", "**").toImmutable()
    val L7 = fromLines("*++", "***").toImmutable()
    val L8 = fromLines("***", "*++").toImmutable()
    val Z1 = fromLines("+**", "**+").toImmutable()
    val Z2 = fromLines("**+", "+**").toImmutable()
    val S1 = fromLines("*+", "**", "+*").toImmutable()
    val S2 = fromLines("+*", "**", "*+").toImmutable()
    val T1 = fromLines("***", "+*+").toImmutable()
    val T2 = fromLines("+*+", "***").toImmutable()
    val T3 = fromLines("*+", "**", "*+").toImmutable()
    val T4 = fromLines("+*", "**", "+*").toImmutable()
    val I1 = fromLines("****").toImmutable()
    val I2 = fromLines("*", "*", "*", "*").toImmutable()
    val O1 = fromLines("**", "**").toImmutable()

    val Combinations: List<Pair<ReadOnlyCellMatrix, ReadOnlyCellMatrix>> by lazy {
        val pairs = ArrayList<Pair<ReadOnlyCellMatrix, ReadOnlyCellMatrix>>(171)

        FixedTetrominoesList.forEach { i ->
            FixedTetrominoesList
                .asSequence()
                .filter { i != it && (it to i) !in pairs }
                .forEach { pairs += i to it }
        }

        pairs
    }
}
