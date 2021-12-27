import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.system.measureTimeMillis

const val FULLTEXT_FIELD = "content"

fun buildIndex(dataPath: Path, indexPath: Path) = runBlocking {
    if (dataPath.isRegularFile()) {
        TextIndexWriter(indexPath.toString()).use { writer ->
            recordFlow(dataPath.toString())
                .take(2)
                .collect { row ->
//                    log("in Main")
                    writer.add(row)
                }
        }

    } else {
        val dbFiles = dataPath.toFile().listFiles()?.filter { it.name.endsWith(".db") }
            ?: throw Exception("No sqlite db files found!")

        val flows = mutableListOf<Flow<Record>>()
        dbFiles.forEach { flows.add(recordFlow(it.toString())) }

        TextIndexWriter(indexPath.toString()).use { writer ->
            val timeElapsed = measureTimeMillis {
                flows.merge()
                    .take(10)
                    .collect { row ->
//                        log("${row["datasetname"]} ${row["id"]}")
                        writer.add(row)
                    }
            }
            println("Time elapsed: $timeElapsed ms")
        }
    }
}

class TextIndexWriter(private val indexPath: String) : AutoCloseable {
    // https://issues.apache.org/jira/browse/LUCENE-843
    private val ramBufferSize = 96.0

    private val writer = run {
        val directory = FSDirectory.open(Paths.get(indexPath))
        val config = IndexWriterConfig(StandardAnalyzer())
        config.ramBufferSizeMB = ramBufferSize
        IndexWriter(directory, config)
    }

    fun add(record: Map<String, String>) {
        val doc = Document().apply {
            record.forEach { (k, v) ->
                if (k == "id" || k == "datasetname") {
                    add(StoredField(k, v))
                } else {
                    add(TextField(k, v, Field.Store.NO))
                }
            }
        }

        // a field for full-text search
        doc.add(
            TextField(
                FULLTEXT_FIELD,
                record.values.joinToString(" "),
                Field.Store.NO
            )
        )

        writer.addDocument(doc)
    }

    override fun close() {
        writer.commit()
        writer.close()
    }
}