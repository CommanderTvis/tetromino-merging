const val SIZE = 25

fun mergedStates(field: ReadOnlyCellMatrix, figure: ReadOnlyCellMatrix): Set<CellMatrix> {
    val borderOfCurrentState = field.borderCells()
    val occupiedCellsInFigure = figure.indices.filter(figure::get).toHashSet()
    val states = hashSetOf<CellMatrix>()

    for (borderCell in borderOfCurrentState) {
        for (occupiedCellInFigure in occupiedCellsInFigure) {
            val copy = field.copy()
            val success = copy.insertWithAnchor(borderCell, figure, occupiedCellInFigure)
            if (success) states += copy.also(CellMatrix::shrink)
        }
    }

    return states
}

fun mergesFor(f: ReadOnlyCellMatrix, insert: ReadOnlyCellMatrix = f): Set<CellMatrix> {
    val field = CellMatrix(SIZE, SIZE)
    field.insertRightBottom(field.center(), insert)
    return mergedStates(field, f)
}

fun solveFor(f1: ReadOnlyCellMatrix, f2: ReadOnlyCellMatrix, n: Int = 0): CellMatrix? {
    var mergesFor1 = mergesFor(f1)
    var mergesFor2 = mergesFor(f2)
    var rs = mergesFor1.firstOrNull { it in mergesFor2 }

    repeat(n) {
        if (rs == null) {
            mergesFor1 = mergesFor1.asSequence().flatMap { mergesFor(it, f1) }.toHashSet()
            mergesFor2 = mergesFor2.asSequence().flatMap { mergesFor(it, f2) }.toHashSet()
            rs = mergesFor1.firstOrNull { it in mergesFor2 }
        }
    }

    return rs
}
