import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.*
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

private const val TOP_K = 10

fun search(indexPath: String, field: String?, queryString: String): List<Document> {
    TextIndexSearcher(indexPath).use { searcher ->
        return search(searcher, field, queryString)
    }
}

fun search(searcher: TextIndexSearcher, field: String?, queryString: String): List<Document> {
    return if (field == null) {
        searcher.searchFullText(queryString)
    } else {
        searcher.searchByPhraseQuery(field, queryString)
    }
}

class TextIndexSearcher(indexPath: String) : AutoCloseable {
    private val directory = FSDirectory.open(Paths.get(indexPath))
    private val searcher = IndexSearcher(DirectoryReader.open(directory))

    private val fields = run {
        val results = mutableListOf<String>()
        DirectoryReader.open(directory).use { reader ->
            reader.leaves().forEach { leafReaderCxt ->
                leafReaderCxt.reader().fieldInfos.forEach { results.add(it.name) }
            }
        }
        results.toList()
    }

    fun searchFullText(queryString: String, topK: Int = TOP_K): List<Document> {
        return searchByPhraseQuery(FULLTEXT_FIELD, queryString, topK)
    }

    fun searchByPhraseQuery(field: String, queryString: String, topK: Int = TOP_K): List<Document> {
        val phraseQuery = exactPhraseQuery(field, queryString)
        return getDocs(phraseQuery, topK)
    }

    private fun exactPhraseQuery(field: String, queryString: String): PhraseQuery {
        val builder = PhraseQuery.Builder()
        tokenize(field, queryString, StandardAnalyzer()).forEach { builder.add(it) }

        return builder.build()
    }

    private fun tokenize(field: String, queryString: String, analyzer: Analyzer): List<Term> {
        val terms = mutableListOf<Term>()
        analyzer.tokenStream(field, queryString).use { tokenStream ->
            val attr = tokenStream.addAttribute(CharTermAttribute::class.java)
            tokenStream.reset()
            while (tokenStream.incrementToken()) {
                terms.add(Term(field, attr.toString()))
            }
            tokenStream.end()
        }

        return terms.toList()
    }

    private fun getDocs(q: Query, topK: Int = TOP_K): List<Document> {
        val results = searcher.search(q, topK).scoreDocs
        val docs = mutableListOf<Document>()
        results.forEach { docs.add(searcher.doc(it.doc)) }

        return docs.toList()
    }

    fun getTotalDocuments() = searcher.indexReader.numDocs()

    fun searchByTermQuery(field: String, queryString: String, topK: Int = TOP_K): List<Document> {
        val q = TermQuery(tokenize(field, queryString, StandardAnalyzer())[0])
        return getDocs(q, topK)
    }

    fun searchByPrefixQuery(field: String, queryString: String, topK: Int = TOP_K): List<Document> {
        val query = PrefixQuery(tokenize(field, queryString, StandardAnalyzer())[0])
        return getDocs(query, topK)
    }

    fun searchByWildcardQuery(field: String, queryString: String, topK: Int = TOP_K): List<Document> {
        val query = WildcardQuery(Term(field, queryString))
        return getDocs(query, topK)
    }

    fun searchByFuzzyQuery(field: String, queryString: String, topK: Int = TOP_K): List<Document> {
        val query = FuzzyQuery(Term(field, queryString))
        return getDocs(query, topK)
    }

    override fun close() {
        directory.close()
    }
}
