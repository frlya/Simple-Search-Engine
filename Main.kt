package search

import java.io.File
import kotlin.text.Charsets.UTF_8


const val menu = """
1. Find a person.
2. Print all people.
3. Exit.
"""
const val exit = "e"
const val path = "Simple Search Engine/task/src/search/"

fun search(lines: List<String>, invertedIndex: Map<String, List<Int>>,
           query: String): List<String> {
    val found = invertedIndex[query.lowercase()]?.map { lines[it] }
    return found ?: listOf()
}

fun load(filename: String): List<String> {
    return File(filename).readLines(UTF_8)
}

fun buildInvertedIndex(lines: List<String>): Map<String, List<Int>> {
    return lines.mapIndexed { i, line ->
        line.lowercase()
            .split(" ")
            .map { it to i }
    }.flatten().groupBy({it.first}, {it.second})
}

fun findFileName(args: Array<String>): String {
   return if(args.contains("--data")) {
       val nextInd = args.indexOf("--data") + 1
       args[nextInd]
   } else { "We are fucked" }
}

fun formatResults(results: List<String>): String {
    val s = if (results.size == 1) "" else "s"
   return """
${results.size} person$s found:
${results.joinToString("\n")}
""".trimIndent()
}

fun searchDispatch( query: String, data: List<String>,
    invertedIndex: Map<String, List<Int>>, searchType: String): String {
    val queryLines = query.split(" ")
    val toNormalize = queryLines.map {search(data, invertedIndex, it)}.flatten()
    val response: List<String> = when(SearchType.resolve(searchType)) {
        SearchType.ALL -> toNormalize
            .groupBy { it }.mapValues { it.value.size }
            .filter { it.value >= queryLines.size }.map { it.key }
        SearchType.ANY -> toNormalize.distinct()
        SearchType.NONE -> data.filter { !toNormalize.contains(it) }
        else -> listOf()
    }

    return if(response.isEmpty()) {
        "No matching people found."
    } else {
        formatResults(response)
    }
}

enum class SearchType(val type: String) {
    FAILED("failed"),
    ALL("all"),
    NONE("none"),
    ANY("any");

    companion object {
        fun resolve(type: String): SearchType {
            return try {
                valueOf(type.uppercase())
            } catch (e: IllegalArgumentException) {
                FAILED
            }
        }
    }
}

fun main(args: Array<String>) {
    val data = load(findFileName(args))
    val invertedIndex = buildInvertedIndex(data)
    while(true) {
        println(menu)
        val result = when(readln().toInt())  {
            1 -> {
                println("Select a matching strategy: ALL, ANY, NONE")
                val strategy = readln()
                println("Enter a name or email to search all suitable people:")
                val query = readln()
                searchDispatch(query, data, invertedIndex, strategy)
            }
            2 -> data.joinToString("\n")
            0 -> exit
            else -> "Incorrect option! Try again."
        }
        if(result == exit) break
        println(result)
    }
}