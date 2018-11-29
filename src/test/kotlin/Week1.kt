import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

typealias Map2D = List<List<Char>> // List of rows which are a list of chars

fun Map2D.getPositionOrNull(coordinate: Coordinate) = getOrNull(coordinate.y)?.getOrNull(coordinate.x)

data class Coordinate(val x: Int, val y: Int)

data class Path(var coordinates: List<Coordinate> = emptyList(), var length: Double = 0.0) {
    operator fun plus(coordinate: Coordinate) =
        Path(coordinates + coordinate, 0.0)
}

val neighborCoordinatesOffset = (-1..1).flatMap { x ->
    (-1..1).mapNotNull {  y ->
        if (x == 0 && y == 0) null
        else Coordinate(x, y)
    }
}

fun parseMapString(mapString: String): Map2D =
    mapString.split('\n')
        .map { lines ->
            lines.map { it }
        }

fun getNeighborCoordinates(coordinate: Coordinate) = neighborCoordinatesOffset.map {
    Coordinate(coordinate.x + it.x, coordinate.y + it.y)
}

fun findShortestPath(map2D: Map2D): Path {
    val start: Coordinate = map2D.findCoordinate('S')!!
    val end = map2D.findCoordinate('X')!!
    val startPaths = mapOf(start to Path(listOf(start), 0.0))
    return doFindShortestPath(map2D, startPaths, end)
}

fun doFindShortestPath(map2D: Map2D, interimPathes: Map<Coordinate, Path>, end: Coordinate): Path {
    val nextPathes = interimPathes.flatMap {(interimEnd, interimPath) ->
        val neighbors = getNeighborCoordinates(interimEnd)
        neighbors.mapNotNull {
            if (map2D.getPositionOrNull(it) == '.') it to interimPath.plus(it)
            else null
        }
    }.toMap()
    val foundSolution = nextPathes[end]
    return if (foundSolution != null) foundSolution
    else TODO() //doFindShortestPath(map2D, nextPathes, end)
}

fun printSolution(map2D: Map2D, path: Path) = map2D.mapIndexed { y, row ->
    row.mapIndexed { x, c ->
        if (Coordinate(x, y) in path.coordinates) '*'
        else c
    }.joinToString("")
}.joinToString("\n")

fun addPath(mapString: String): String {
    val map2D = parseMapString(mapString)
    val path = findShortestPath(map2D)
    return printSolution(map2D, path)
}

private fun Map2D.findCoordinate(toFind: Char): Coordinate? {
    this.withIndex().forEach { (y, rows) ->
        rows.withIndex().forEach { (x, c) ->
            if (c == toFind) return Coordinate(x, y)

        }
    }
    return null
}

class Week1 : DescribeSpec({
    describe("Parse Map") {
        context("A Map") {
            val mapString = """
        .....
        ..XS.
        .....
        """.trimIndent()

            it("should be parsed to a map as a list of lists") {
                parseMapString(mapString) shouldBe listOf(
                    listOf('.', '.', '.', '.', '.'),
                    listOf('.', '.', 'X', 'S', '.'),
                    listOf('.', '.', '.', '.', '.')
                )
            }
        }
    }
    describe("Neighbour Coordinate") {
        it("should have the right coordinates") {
            neighborCoordinatesOffset.size shouldBe 8
            neighborCoordinatesOffset.shouldContain(Coordinate(-1, -1))
            neighborCoordinatesOffset.shouldContain(Coordinate(-1, 0))
            neighborCoordinatesOffset.shouldNotContain(Coordinate(0, 0))
        }
    }
    describe("Find in map") {
        context("A map") {
            val map2D = parseMapString("""
        .....
        ..XS.
        .....
        """.trimIndent())

            it("find X in map") {
                map2D.findCoordinate('X') shouldBe Coordinate(2, 1)
            }
        }
    }

    describe("Shortest Path") {

        context("Markes start and end as part of way") {
            val mapString = """
        ....................
        .........XS.........
        ....................
        """.trimIndent()

            val marked = """
        ....................
        .........**.........
        ....................
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

        context("Straight way is straight") {
            val mapString = """
        ....................
        .....X..........S...
        ....................
        """.trimIndent()

            val marked = """
        ....................
        .....************...
        ....................
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

        context("Use cross moves") {
            val mapString = """
        ...........
        .......S...
        ...........
        ...........
        ...........
        ...........
        ..X........
        """.trimIndent()

            val marked = """
        ...........
        .......*...
        ......*....
        .....*.....
        ....*......
        ...*.......
        ..*........
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

        context("Mark way around wall") {
            val mapString = """
        ....................
        ......X...B.........
        ..........B.........
        ........BBB....S....
        ....................
        """.trimIndent()

            val marked = """
        ........***.........
        ......**..B*........
        ..........B.*.......
        ........BBB..***....
        ....................
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

        context("Mark way around wall 2") {
            val mapString = """
        ..........B.........
        ......X...B.........
        ..........B.........
        ........BBB....S....
        ....................
        """.trimIndent()

            val marked = """
        ..........B.........
        ......*...B.........
        ......*...B.........
        .......*BBB*****....
        ........***.........
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

        context("Mark way on labirynth") {
            val mapString = """
        BB..B...B...BBBBB...
        ....B.X.BBB.B...B.B.
        ..BBB.B.B.B.B.B.B.B.
        ....B.BBB.B.B.BS..B.
        BBB.B...B.B.BBBBBBB.
        ..B...B.............
        """.trimIndent()

            val marked = """
        BB..B...B...BBBBB.*.
        ....B.*.BBB.B...B*B*
        ..BBB*B.B.B.B.B.B*B*
        ....B*BBB.B.B.B**.B*
        BBB.B.**B.B.BBBBBBB*
        ..B...B.***********.
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

        context("Navigate on forest") {
            val mapString = """
..B....B..B.....BB.BBB......B..B.......B....BBB.....BB.B....BB.BB..BB.......BBB...BBBBB.....B...B..B
....BB.....B.B.B....BBB..........BBB.B....BB..B.B..B...BBBBBBBB..BB.B..........B..B.......B.BB..B..B
B.......S.B.B.....B.........B.B..B...B...BB.BB.B...B.B.B...B..BB.BB.....BB..B........BB.B..B......BB
.......B..BBBB.B.....B....B...B.B..B...B...BB.B.BBB..BBB....B......B....B..B.B.BB......BB....BB....B
......B.BB.B.B......B..BBBB.B..BBBBB..B.B.BB.BBBBBB.BB.B..B.BBB..B......B......B....B........B.B..B.
B.....B......BB..BBB..B.B....BB.BBBBB.....BB..B..B..BB..BB..BB.BB......BB....B.BB.B...B..B......B...
B.B.B.................BB.B.B....BB.....BB.BB...BB.BB..B.B.......B......BB...BB.BB...BB...BB..B...B..
BB...BB.......BBBB........BBB..BBB.B.B...B..B....B...BBB..........B..B..B..BB..BB...BB..B...BBBBB..B
....B..B....B...B.B.B...B.BB........B....BBB..........B...B.BB.B..BB..B.......BB...B.B..B...B..B....
.........B.....BB.B.....B...BB.B.B.BBBB....B.....B.......BBBBB....B.B............BB.....B........B..
.B..B.B.B...............B..B.B.....B.BB.BB.B...B.BBB....BB.......BB...B.B.BBBB......B...B.....B.....
.B..B.....B.B..BB....BB.......B.B.BBB.B.....B..BB.B...B.....B..B...B..B.B..B.BB......B..B..B.....B.B
.......B.BBB.BB.......B.BBB....B...BB...B..........B.......B...B.....BBBB.B....B..B..BB.B.B.B...B.B.
B.B..BB.........BB.......BB.....B.BB..BB..BBB..B...BB....B.BBB..B....B.B....B.BBBB..B..BB.B.........
.B..BB......B...B..BB..BBB.B.B.B.BB.B.B..BBB.BBBB.B..B......BB.....BB.....B....BB....B..BB..........
B.B..B...B.B.B..B.B..B......B.B.B.BBB.B.B.B....B........BB..B....BB....BBB.BB.B....B...B....BBB.....
.B..BB...B.....B.B.B.BBB.B.........B...B..B.B...BBBBB....B.BBB..BB...........B.....BBBB....B.....BB.
.B......BB...BB.......B..B.B....B.BBB...BB.B......B.BBB..B....BB....BB....B.........B.B.B...BB....B.
B..B...B...B......B.....B......B..B..B....B.B.B.........B.BBB.B..B..BB..B..BB..B.....B..BB.BB.B....B
B.BB...B..BB..B.BB.B........B.BB......B...B.B..BB.BB.B.B......B...B..B.B...BBB...BBB.........X..B..B
.B.B.B.....B.BB..BB...BB.....B.BB.....B...BB...BBB..B..BBB..B.B.....B......B.....BB.....B..B.B..B...
BB...B.B..BB..B.B......B.....B....BBB...BBB.B...B.....B..BBBB.B..B..BBBBBB....BB.BB.B....BB.BB.BBB..
..B....B..BB........B..BB..BB..B...B.B..BB.B....BBB..B.....B......B...........BBBB.......B.BB....B..
BB....BBB...B.B...B.....B.B..B.B....B.B.B...B.B..BBBBBB.B....B...BB..BBB...B.BB....B.........BB..B..
        """.trimIndent()

            val marked = """
..B....B..B.....BB.BBB......B..B.......B....BBB.....BB.B....BB.BB..BB.......BBB...BBBBB.....B...B..B
....BB.....B.B.B....BBB..........BBB.B....BB..B.B..B...BBBBBBBB..BB.B..........B..B.......B.BB..B..B
B.......*.B.B.....B.........B.B..B...B...BB.BB.B...B.B.B...B..BB.BB.....BB..B........BB.B..B......BB
.......B.*BBBB.B.....B....B...B.B..B...B...BB.B.BBB..BBB....B......B....B..B.B.BB......BB....BB....B
......B.BB*B.B......B..BBBB.B..BBBBB..B.B.BB.BBBBBB.BB.B..B.BBB..B......B......B....B........B.B..B.
B.....B....**BB..BBB..B.B....BB.BBBBB.....BB..B..B..BB..BB..BB.BB......BB....B.BB.B...B..B......B...
B.B.B........*********BB.B.B....BB.....BB.BB...BB.BB..B.B.......B......BB...BB.BB...BB...BB..B...B..
BB...BB.......BBBB....***.BBB..BBB.B*B...B..B....B...BBB..........B..B..B..BB..BB...BB..B...BBBBB..B
....B..B....B...B.B.B...B*BB********B****BBB..........B...B.BB.B..BB..B.......BB...B.B..B...B..B....
.........B.....BB.B.....B.**BB.B.B.BBBB..*.B.....B.......BBBBB....B.B............BB.....B........B..
.B..B.B.B...............B..B.B.....B.BB.BB*B...B.BBB....BB.......BB...B.B.BBBB......B...B.....B.....
.B..B.....B.B..BB....BB.......B.B.BBB.B....*B..BB.B***B....*B..B...B..B.B..B.BB......B..B..B.....B.B
.......B.BBB.BB.......B.BBB....B...BB...B...*******B..*****B***B.....BBBB.B....B..B..BB.B.B.B...B.B.
B.B..BB.........BB.......BB.....B.BB..BB..BBB..B...BB....B.BBB.*B....B.B....B.BBBB..B..BB.B.........
.B..BB......B...B..BB..BBB.B.B.B.BB.B.B..BBB.BBBB.B..B......BB..***BB.....B....BB....B..BB..........
B.B..B...B.B.B..B.B..B......B.B.B.BBB.B.B.B....B........BB..B....BB****BBB.BB.B....B...B....BBB.....
.B..BB...B.....B.B.B.BBB.B.........B...B..B.B...BBBBB....B.BBB..BB.....******B.....BBBB....B.....BB.
.B......BB...BB.......B..B.B....B.BBB...BB.B......B.BBB..B....BB....BB....B..*******B.B.B...BB....B.
B..B...B...B......B.....B......B..B..B....B.B.B.........B.BBB.B..B..BB..B..BB..B....*B..BB.BB.B....B
B.BB...B..BB..B.BB.B........B.BB......B...B.B..BB.BB.B.B......B...B..B.B...BBB...BBB.*********..B..B
.B.B.B.....B.BB..BB...BB.....B.BB.....B...BB...BBB..B..BBB..B.B.....B......B.....BB.....B..B.B..B...
BB...B.B..BB..B.B......B.....B....BBB...BBB.B...B.....B..BBBB.B..B..BBBBBB....BB.BB.B....BB.BB.BBB..
..B....B..BB........B..BB..BB..B...B.B..BB.B....BBB..B.....B......B...........BBBB.......B.BB....B..
BB....BBB...B.B...B.....B.B..B.B....B.B.B...B.B..BBBBBB.B....B...BB..BBB...B.BB....B.........BB..B..
        """.trimIndent()

            it("should find path") {
                addPath(mapString) shouldBe marked
            }
        }

    }
})


