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
}

fun ReadOnlyCellMatrix.borderCells(): Set<Point> = indices
    .filter(::get)
    .map(::neighbors)
    .flatMap(Neighbors::toList)
    .filterNotNull()
    .filterNot(::get)
    .toHashSet()

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


class CellMatrix private constructor(private var rows: Array<BooleanArray>) : ReadOnlyCellMatrix {
    constructor(i: Int, j: Int) : this(Array(i) { BooleanArray(j) { false } })

    override val numRows
        get() = rows.size

    override val numColumns
        get() = rows[0].size

    override val indices: Sequence<Point> by lazy {
        sequence { repeat(numRows) { x -> repeat(numColumns) { y -> yield(x to y) } } }
    }

    init {
        require(numRows > 0) { "The created matrix has no rows." }
        require(numColumns > 0) { "The created matrix has no columns." }
        val lenOfFirst = rows.first().size
        require(rows.drop(1).all { it.size == lenOfFirst }) { "The rows in matrix have different quantity of rows." }
    }

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
    fun flipHorizontally() {
        rows = rows.map(BooleanArray::reversedArray).toTypedArray()
    }

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
    fun flipVertically() {
        rows = columnsToRows(columns().map(BooleanArray::reversedArray).toTypedArray())
    }

    fun rotateLeft90() {
        TODO()
    }

    fun rotateRight90() {
        TODO()
    }

    fun shrink() {
        val rowsLs = LinkedList(rows.toList())

        while (rowsLs.size != 1 && true !in rowsLs.first)
            rowsLs.removeFirst()

        while (rowsLs.size != 1 && true !in rowsLs.last)
            rowsLs.removeLast()

        rows = rowsLs.toTypedArray()
        val columnsLs = LinkedList(columns().toList())

        while (columnsLs.size != 1 && true !in columnsLs.first)
            columnsLs.removeFirst()

        while (columnsLs.size != 1 && true !in columnsLs.last)
            columnsLs.removeLast()

        rows = columnsToRows(columnsLs)
    }

    fun insertRightBottom(point: Point, figure: ReadOnlyCellMatrix): Boolean {
        return try {
            val copy = copy()

            for ((fX, fY) in figure.indices) {
                if (figure[fX, fY]) {
                    if (copy[point.first + fX, point.second + fY])
                        return false
                    else
                        copy[point.first + fX, point.second + fY] = true
                }
            }

            this.rows = copy.rows
            true
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }

    private fun columns(): Array<BooleanArray> =
        Array(numColumns) { col -> BooleanArray(numRows) { row -> this[row, col] } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CellMatrix) return false
        if (!rows.contentDeepEquals(other.rows)) return false
        return true
    }

    override fun hashCode(): Int = rows.contentDeepHashCode()

    companion object {
        private fun columnsToRows(columns: Array<BooleanArray>): Array<BooleanArray> =
            Array(columns[0].size) { row -> BooleanArray(columns.size) { col -> columns[col][row] } }

        private fun columnsToRows(columns: List<BooleanArray>): Array<BooleanArray> =
            Array(columns[0].size) { row -> BooleanArray(columns.size) { col -> columns[col][row] } }

        fun fromLines(lines: List<String>): CellMatrix = CellMatrix(
            rows = lines
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
        )

        fun fromLines(vararg lines: String) = fromLines(lines.toList())

        fun fromString(string: String): CellMatrix = fromLines(string.split("\n"))
    }
}

fun CellMatrix.toImmutable(): ReadOnlyCellMatrix = copy()

operator fun CellMatrix.set(point: Point, value: Boolean) = set(point.first, point.second, value)

fun CellMatrix.insertWithAnchor(point: Point, figure: ReadOnlyCellMatrix, anchor: Point): Boolean {
    val topLeftCorner = (point.first - anchor.first) to (point.second - anchor.second)
    return insertRightBottom(topLeftCorner, figure)
}
