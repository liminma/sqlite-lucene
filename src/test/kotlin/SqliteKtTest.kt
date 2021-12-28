import kotlinx.coroutines.runBlocking
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContains

const val SAMPLE_DB_FILE = "sqlite3_sample.db"

internal class SqliteKtTest {
    private val sampleDBFile = Paths.get(
        javaClass.classLoader.getResource(SAMPLE_DB_FILE)!!.toURI()
    ).toString()

    @Test
    fun recordFlow() = runBlocking {
        recordFlow(sampleDBFile).collect {
            assertContains(it, "recnum")
        }
    }
}