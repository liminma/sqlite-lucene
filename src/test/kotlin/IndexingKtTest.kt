import kotlinx.coroutines.runBlocking
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.assertEquals

const val INDEX_FOLDER = "lucene-index"

internal class TextIndexWriterTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun add() {
    }
}

internal class IndexingKtTest {
    val dataPath = Paths.get(javaClass.classLoader.getResource(SAMPLE_DB_FILE)!!.toURI())
    val indexAbsLocation = dataPath.toFile().parent + "/" + INDEX_FOLDER

    @AfterEach
    fun tearDown() {
        File(indexAbsLocation).deleteRecursively()
    }

    @Test
    fun buildIndex_withSignleDBFile() = runBlocking {
        buildIndex(dataPath, indexAbsLocation)
        val indexPath = Paths.get(indexAbsLocation)
        assertTrue { indexPath.exists() }
        assertEquals(4, DirectoryReader.open(FSDirectory.open(indexPath)).numDocs())
    }

    @Test
    fun buildIndex_withDataFolder() = runBlocking {
        buildIndex(dataPath.parent, indexAbsLocation)
        val indexPath = Paths.get(indexAbsLocation)
        assertTrue { indexPath.exists() }
        assertEquals(4, DirectoryReader.open(FSDirectory.open(indexPath)).numDocs())
    }
}
