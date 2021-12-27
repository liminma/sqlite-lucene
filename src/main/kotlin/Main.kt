import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

class SqliteLucene : CliktCommand() {
    override fun run() = Unit
}

class BuildIndex : CliktCommand(help = "Indexing sqlite tables") {
    private val dataPath by argument(name = "data-path", help = "Path of sqlite db files")
        .path(mustExist = true, mustBeReadable = true)
    private val indexPath by argument(name = "index-path", help = "Path of the index directory")
        .path(canBeFile = false)

    override fun run() {
        buildIndex(dataPath, indexPath)
    }
}

class Search : CliktCommand(help = "Search index") {
    private val indexPath by argument(name = "index-path", help = "Path of the index directory")
        .path(canBeFile = false)

    override fun run() {
        TextIndexSearcher(indexPath.toString()).use { searcher ->
            println("Total documents in the index: ${searcher.getTotalDocuments()}")
            while (true) {
                print("[field :]query-string ")
                val input = readln()
                if (input.isNotEmpty()) {
                    val tokens = input.split(":")
                    if (tokens.size == 1) {
                        search(searcher, null, tokens[0]).forEach { println(it) }
                    } else {
                        search(searcher, tokens[0], tokens[1]).forEach { println(it) }
                    }
                } else {
                    break
                }
            }
        }
    }
}

fun main(args: Array<String>) = SqliteLucene()
    .subcommands(BuildIndex(), Search())
    .main(args)