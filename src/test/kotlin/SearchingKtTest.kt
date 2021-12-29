import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SearchingKtTest {
    private val dataPath: Path = Paths.get(javaClass.classLoader.getResource(SAMPLE_DB_FILE)!!.toURI())
    private val indexAbsLocation = dataPath.toFile().parent + "/" + INDEX_FOLDER
    private lateinit var searcher: TextIndexSearcher

    @BeforeAll
    fun setUp() {
        buildIndex(dataPath, indexAbsLocation)
        searcher = TextIndexSearcher(indexAbsLocation)
    }

    @AfterAll
    fun tearDown() {
        searcher.close()
        File(indexAbsLocation).deleteRecursively()
    }

    @Test
    fun search() {
        assertEquals(
            1,
            search(indexAbsLocation, "recnum", "Record").size
        )
    }

    @Test
    fun searchFullText() {
        assertEquals(
            2,
            searcher.searchFullText("Ontario").size
        )
    }

    @Test
    fun searchByPhraseQuery() {
        assertEquals(
            2,
            searcher.searchByPhraseQuery("edu", "Bachelor's degree").size
        )
    }

    @Test
    fun getTotalDocuments() {
        assertEquals(
            4,
            searcher.getTotalDocuments()
        )
    }

    @Test
    fun searchByTermQuery() {
        assertEquals(
            1,
            searcher.searchByTermQuery("employed", "No").size
        )
    }

    @Test
    fun searchByPrefixQuery() {
        assertEquals(
            1,
            searcher.searchByPrefixQuery("gender", "Fem").size
        )
    }

    @Test
    fun searchByWildcardQuery() {
        assertEquals(
            2,
            searcher.searchByWildcardQuery("edu", "bache*").size
        )
    }

    @Test
    fun searchByFuzzyQuery() {
        assertEquals(
            2,
            searcher.searchByFuzzyQuery("imm_status", "imigrant").size
        )
    }
}