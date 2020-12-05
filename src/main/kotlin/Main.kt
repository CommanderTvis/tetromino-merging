import java.io.File
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTime

fun main(args: Array<String>) {
    val n = args.firstOrNull()?.toIntOrNull() ?: 5
    val numFile = File("./num-n$n.txt")
    if (!numFile.exists()) numFile.createNewFile()
    val num = numFile.readText().toIntOrNull() ?: 0
    val outFile = File("./out-n$n.txt")
    if (!outFile.exists()) outFile.createNewFile()

    outFile.outputStream().use { out ->
        PrintStream(out).use { str ->
            val operations = AtomicInteger(0)
            val success = AtomicInteger(num)

            measureTime {
                FixedTetrominoes
                    .Combinations
                    .drop(num)
                    .parallelStream()
                    .forEach { (f, s) ->
                        val solution = solveFor(f, s, n)

                        numFile.writeText(operations.incrementAndGet().toString())
                        if (solution != null) {
                            str.println("p1=\n${f}\np2=\n${s}\n\nsolution=\n$solution\n\n")
                            str.println(success.incrementAndGet())
                        } else {
                            str.println("p1=\n${f}\np2=\n${s}\n\nfail :(\n\n")
                        }
                    }
            }.also(str::println)

            str.println("Successful: ${success.get()}")
        }
    }
}
