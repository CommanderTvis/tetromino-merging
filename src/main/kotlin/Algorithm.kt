import gnu.trove.set.hash.TCustomHashSet

const val SIZE = 25


fun comparisonGrade(f1: ReadOnlyCellMatrix, f2: ReadOnlyCellMatrix): Int {
    var n = 0

    for (idx in f1.indices) {
        if (idx in f2 && f1[idx] == f2[idx]) n++
    }

    return n
}


fun mergesFor(f: ReadOnlyCellMatrix, insert: ReadOnlyCellMatrix = f): Set<CellMatrix> {
    var field = CellMatrix.empty(SIZE, SIZE)
    field = field.insertRightBottom(field.center(), insert)!!
    val borderOfCurrentState = field.borderCells()
    val occupiedCellsInFigure: Iterable<Point> = f.indices.filterTo(hashSetOf(), f::get)
    val states: MutableSet<CellMatrix> = TCustomHashSet(TroveHashingStrategy)

    for (borderCell in borderOfCurrentState)
        for (occupiedCellInFigure in occupiedCellsInFigure) {
            val success = field.insertWithAnchor(borderCell, f, occupiedCellInFigure)
            if (success != null) states += success.shrink()
        }

    return states
}

private const val threshold = 1.0 / 2.0

fun solveFor(f1: ReadOnlyCellMatrix, f2: ReadOnlyCellMatrix, n: Int = 2): ReadOnlyCellMatrix? {
//    var mergesFor1 = mergesFor(f1)
//    var mergesFor2 = mergesFor(f2)
//    var rs: ReadOnlyCellMatrix? = null
//
//    l@ for (it1 in mergesFor1) {
//        var max = Int.MIN_VALUE
//
//        for (it2 in mergesFor2) {
//            val grade = comparisonGrade(it1, it2)
//            if (grade > max) max = grade
//
//            if (it1 == it2) {
//                rs = it1
//                break@l
//            }
//        }
//
//        if (max / (4 * n) < threshold)
//    }
//
////    var rs = mergesFor1.firstOrNull { it in mergesFor2 }

    var mergesFor1: Collection<ReadOnlyCellMatrix>? = null
    var mergesFor2: Collection<ReadOnlyCellMatrix>? = null
    var rs: ReadOnlyCellMatrix? = null
    var i = 0

    while (rs == null && i < n) {
        mergesFor1 = (mergesFor1 ?: mergesFor(f1)).run {
            if (i != 0) flatMapTo(TCustomHashSet(TroveHashingStrategy)) { mergesFor(it, f1) } else this
        }

        mergesFor2 = (mergesFor2 ?: mergesFor(f2)).run {
            if (i != 0) flatMapTo(TCustomHashSet(TroveHashingStrategy)) { mergesFor(it, f2) } else this
        }

        l@ for (it1 in mergesFor1)
            for (it2 in mergesFor2)
                if (it1.contentEquals(it2)) {
                    rs = it1
                    break@l
                }

        i++
    }

    return rs
}
