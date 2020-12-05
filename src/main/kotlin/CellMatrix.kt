import gnu.trove.strategy.HashingStrategy
import java.util.*

data class Point(val first: Int, val second: Int)

infix fun Int.to(other: Int) = Point(this, other)

data class Neighbors(val top: Point?, val bottom: Point?, val left: Point?, val right: Point?)

fun Neighbors.toList() = listOf(top, bottom, left, right)


interface ReadOnlyCellMatrix {
    val numRows: Int
    val numColumns: Int
    val indices: Sequence<Point>

    operator fun get(i: Int, j: Int): Boolean

    fun copy(): CellMatrix
    fun contentHashCode(): Int
    fun contentEquals(other: ReadOnlyCellMatrix): Boolean
}

fun ReadOnlyCellMatrix.borderCells(): Set<Point> = indices
    .filter(::get)
    .map(::neighbors)
    .flatMap(Neighbors::toList)
    .filterNotNull()
    .filterNotTo(hashSetOf(), ::get)

fun ReadOnlyCellMatrix.center() = numRows / 2 to numColumns / 2

fun ReadOnlyCellMatrix.neighbors(i: Int, j: Int) = Neighbors(
    (i - 1 to j).takeIf(::contains),
    (i + 1 to j).takeIf(::contains),
    (i to j - 1).takeIf(::contains),
    (i to j + 1).takeIf(::contains)
)

fun ReadOnlyCellMatrix.neighbors(point: Point) = neighbors(point.first, point.second)

operator fun ReadOnlyCellMatrix.contains(point: Point): Boolean =
    point.first in 0 until numRows && point.second in 0 until numColumns

fun ReadOnlyCellMatrix.getOrNull(i: Int, j: Int): Boolean? = if (i in 0 until numRows && j in 0 until numColumns)
    this[i, j]
else
    null

fun ReadOnlyCellMatrix.getOrElse(i: Int, j: Int, defaultValue: (Point) -> Boolean): Boolean =
    if (i in 0 until numRows && j in 0 until numColumns)
        this[i, j]
    else
        defaultValue(i to j)

operator fun ReadOnlyCellMatrix.get(point: Point) = this[point.first, point.second]


@Suppress("DEPRECATION")
inline class CellMatrix @Deprecated("For internal use only.", level = DeprecationLevel.WARNING)
constructor(val rows: Array<BooleanArray>) :
    ReadOnlyCellMatrix {
    override val numRows
        get() = rows.size

    override val numColumns
        get() = rows[0].size

    override val indices: Sequence<Point>
        get() = sequence { repeat(numRows) { x -> repeat(numColumns) { y -> yield(x to y) } } }

    operator fun set(i: Int, j: Int, value: Boolean) = rows[i].set(j, value)

    override operator fun get(i: Int, j: Int) = rows[i][j]

    override fun toString(): String =
        rows.joinToString("\n") { row -> row.joinToString("") { cell -> if (cell) "*" else "+" } }

    override fun copy() = CellMatrix(rows.map(BooleanArray::copyOf).toTypedArray())

    /**
     * Flips this matrix horizontally, i.e.:
     * ```
     * *
     * *
     * **
     * ```
     * ->
     * ```
     *   *
     *   *
     *  **
     * ```
     */
    fun flipHorizontally(): CellMatrix = CellMatrix(rows.map(BooleanArray::reversedArray).toTypedArray())

    /**
     * Flips this matrix vertically, i.e.:
     *
     * ```
     * *
     * *
     * **
     * ```
     * ->
     * ```
     * **
     * *
     * *
     * ```
     */
    fun flipVertically(): CellMatrix =
        CellMatrix(columnsToRows(rowsToColumns(rows).map(BooleanArray::reversedArray).toTypedArray()))

    fun shrink(): CellMatrix {
        val rowsLs = LinkedList(rows.toList())

        while (rowsLs.size != 1 && true !in rowsLs.first)
            rowsLs.removeFirst()

        while (rowsLs.size != 1 && true !in rowsLs.last)
            rowsLs.removeLast()

        var rows = rowsLs.toTypedArray()
        val columnsLs = LinkedList(rowsToColumns(rows).toList())

        while (columnsLs.size != 1 && true !in columnsLs.first)
            columnsLs.removeFirst()

        while (columnsLs.size != 1 && true !in columnsLs.last)
            columnsLs.removeLast()

        rows = columnsToRows(columnsLs)
        return CellMatrix(rows)
    }

    fun insertRightBottom(point: Point, figure: ReadOnlyCellMatrix): CellMatrix? {
        return try {
            val copy = copy()

            for ((fX, fY) in figure.indices) {
                if (figure[fX, fY]) {
                    if (copy[point.first + fX, point.second + fY])
                        return null
                    else
                        copy[point.first + fX, point.second + fY] = true
                }
            }

            CellMatrix(copy.rows)
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    override fun contentEquals(other: ReadOnlyCellMatrix): Boolean {
        if (this === other) return true
        if (other is CellMatrix) return rows.contentDeepEquals(other.rows)
        val thisIndicesList = indices.toList()
        val otherIndicesList = other.indices.toList()
        if (otherIndicesList != otherIndicesList) return false
        return thisIndicesList.map(this@CellMatrix::get) == otherIndicesList.map(other::get)
    }

    override fun contentHashCode(): Int = rows.contentDeepHashCode()

    companion object {
        private fun columnsToRows(columns: Array<BooleanArray>): Array<BooleanArray> =
            Array(columns[0].size) { row -> BooleanArray(columns.size) { col -> columns[col][row] } }

        private fun rowsToColumns(rows: Array<BooleanArray>): Array<BooleanArray> =
            Array(rows[0].size) { col -> BooleanArray(rows.size) { row -> rows[row][col] } }

        private fun columnsToRows(columns: List<BooleanArray>): Array<BooleanArray> =
            Array(columns[0].size) { row -> BooleanArray(columns.size) { col -> columns[col][row] } }

        fun fromLines(lines: List<String>): CellMatrix {
            val rows = lines
                .map { line ->
                    line.map {
                        when (it) {
                            '*' -> true
                            '+' -> false
                            else -> throw IllegalArgumentException("Illegal symbol '$it'.")
                        }
                    }
                }
                .map(List<Boolean>::toBooleanArray)
                .toTypedArray()

            require(rows.isNotEmpty()) { "The created matrix has no rows." }
            require(rows[0].isNotEmpty()) { "The created matrix has no columns." }
            val lenOfFirst = rows.first().size
            require(rows.drop(1).all { it.size == lenOfFirst }) {
                "The rows in matrix have different quantity of rows."
            }

            return CellMatrix(
                rows = rows
            )
        }

        fun fromLines(vararg lines: String) = fromLines(lines.toList())

        fun empty(numRows: Int, numColumns: Int): CellMatrix {
            require(numRows != 0) { "The created matrix has no rows." }
            require(numColumns != 0) { "The created matrix has no columns." }
            return CellMatrix(Array(numRows) { BooleanArray(numColumns) { false } })
        }

        fun fromString(string: String): CellMatrix = fromLines(string.split("\n"))
    }
}

fun CellMatrix.toImmutable(): ReadOnlyCellMatrix = copy()

operator fun CellMatrix.set(point: Point, value: Boolean) = set(point.first, point.second, value)

fun CellMatrix.insertWithAnchor(point: Point, figure: ReadOnlyCellMatrix, anchor: Point): CellMatrix? {
    val topLeftCorner = (point.first - anchor.first) to (point.second - anchor.second)
    return insertRightBottom(topLeftCorner, figure)
}


object TroveHashingStrategy : HashingStrategy<ReadOnlyCellMatrix> {
    override fun equals(o1: ReadOnlyCellMatrix, o2: ReadOnlyCellMatrix): Boolean = o1.contentEquals(o2)
    override fun computeHashCode(`object`: ReadOnlyCellMatrix): Int = `object`.contentHashCode()
}
