import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CellMatrixTest {
    @Test
    fun borderCells() {
        val matrix = CellMatrix.fromLines(
            "+++",
            "+*+",
            "+++",
        )

        assertEquals(setOf(0 to 1, 1 to 0, 1 to 2, 2 to 1), matrix.borderCells())
    }

    @Test
    fun rows() {
        val matrix = CellMatrix.fromLines(
            "++",
            "++",
            "++",
        )

        assertEquals(3, matrix.numRows)
    }

    @Test
    fun columns() {
        val matrix = CellMatrix.fromLines(
            "++",
            "++",
            "++",
        )

        assertEquals(2, matrix.numColumns)
    }

    @Test
    fun fromStringConsistentWithToString() {
        val string = "++\n++\n++"
        val matrix = CellMatrix.fromString(string)
        assertEquals(string, matrix.toString())
    }

    @Test
    fun fromStringHandlesWrongChars() {
        assertThrows<IllegalArgumentException> { CellMatrix.fromString(" ") }
    }

    @Test
    fun copy() {
        val mat = CellMatrix.fromLines("++", "+*", "++")
        assertTrue(mat.contentEquals(mat.copy()))
    }

    @Test
    fun matrixChecksConsistency() {
        assertThrows<IllegalArgumentException> { CellMatrix.fromLines("+++", "++", "+++") }
    }

    @Test
    fun matrixCannotBeEmpty() {
        assertThrows<IllegalArgumentException> { CellMatrix.fromLines() }
        assertThrows<IllegalArgumentException> { CellMatrix.empty(0, 0) }
        assertThrows<IllegalArgumentException> { CellMatrix.empty(1, 0) }
        assertThrows<IllegalArgumentException> { CellMatrix.empty(0, 1) }
    }

    @Test
    fun set() {
        val mat = CellMatrix.fromString("++\n+*\n++")
        mat[0, 0] = true
        assertEquals(true, mat[0, 0])
    }

    @Test
    fun get() {
        val mat = CellMatrix.fromString("+")
        assertEquals(false, mat[0, 0])
    }

    @Test
    fun insertWithAnchor() {
        var m1 = CellMatrix.fromLines(
            "++++",
            "++++",
            "++++",
            "++++",
        )

        val fig = CellMatrix.fromLines(
            "**",
            "**",
        )

        val exp = CellMatrix.fromLines("++++", "**++", "**++", "++++")
        assertTrue(exp.contentEquals(m1.insertWithAnchor(1 to 1, fig, 0 to 1)!!))
    }

    @Test
    fun insertRightBottom() {
        val m1 = CellMatrix.fromLines(
            "+++",
            "+++",
            "+++",
        )

        val fig = CellMatrix.fromLines("*+", "**")
        val exp = CellMatrix.fromLines("*++", "**+", "+++")
        assertTrue(exp.contentEquals(m1.insertRightBottom(0 to 0, fig)!!))
    }

    @Test
    fun shrink() {
        val m = CellMatrix.fromLines(
            "+++++",
            "++*++",
            "++**+",
            "+**++",
            "+++++",
        )

        val exp = CellMatrix.fromLines(
            "+*+",
            "+**",
            "**+",
        )

        assertTrue(exp.contentEquals(m.shrink()))
    }
}
