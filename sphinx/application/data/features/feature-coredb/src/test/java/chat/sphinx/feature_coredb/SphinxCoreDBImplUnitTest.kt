package chat.sphinx.feature_coredb

import chat.sphinx.conceptcoredb.SphinxDatabaseQueries
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.concept_encryption_key.EncryptionKeyHandler
import io.matthewnelson.k_openssl_common.clazzes.HashIterations
import io.matthewnelson.k_openssl_common.clazzes.Password
import io.matthewnelson.test_concept_coroutines.CoroutineTestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SphinxCoreDBImplUnitTest: CoroutineTestHelper() {

    private companion object {
        const val TEST_PRIV_KEY = "TEST_PRIV_KEY"
        const val TEST_PUB_KEY = "TEST_PUB_KEY"
    }

    private class TestEncryptionKeyHandler: EncryptionKeyHandler() {
        override suspend fun generateEncryptionKey(): EncryptionKey {
            return copyAndStoreKey(TEST_PRIV_KEY.toCharArray(), TEST_PUB_KEY.toCharArray())
        }

        override fun validateEncryptionKey(
            privateKey: CharArray,
            publicKey: CharArray
        ): EncryptionKey {
            return copyAndStoreKey(privateKey, publicKey)
        }

        override fun getTestStringEncryptHashIterations(privateKey: Password): HashIterations {
            return HashIterations(1)
        }
    }

    private inner class TestSphinxCoreDBImpl: SphinxCoreDBImpl() {
        override fun getSqlDriver(encryptionKey: EncryptionKey): SqlDriver {
            return driver
        }
    }

    private val testHandler: TestEncryptionKeyHandler by lazy {
        TestEncryptionKeyHandler()
    }

    private val driver: SqlDriver by lazy {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }

    private val testCoreDB: SphinxCoreDBImpl by lazy {
        TestSphinxCoreDBImpl()
    }

    @Before
    fun setup() {
        setupCoroutineTestHelper()
    }

    @After
    fun tearDown() {
        tearDownCoroutineTestHelper()
        driver.close()
    }

    @Test
    fun `getSphinxDatabaseQueries suspends until initialization is complete`() =
        testDispatcher.runBlockingTest {
            var queries: SphinxDatabaseQueries? = null
            launch {
                queries = testCoreDB.getSphinxDatabaseQueries()
            }
            delay(500L)
            Assert.assertNull(queries)

            testCoreDB.initializeDatabase(testHandler.generateEncryptionKey())
            delay(500L)
            Assert.assertNotNull(queries)
        }

    @Test
    fun `getSphinxDatabaseQueries cancels quietly when scope is cancelled`() =
        testDispatcher.runBlockingTest {
            var queries: SphinxDatabaseQueries? = null
            val newScope = CoroutineScope(dispatchers.main)
            newScope.launch {
                queries = testCoreDB.getSphinxDatabaseQueries()
                Assert.fail()
            }
            delay(500L)
            Assert.assertNull(queries)
            newScope.cancel()

            testCoreDB.initializeDatabase(testHandler.generateEncryptionKey())
            delay(500L)
            Assert.assertNull(queries)
            Assert.assertNotNull(testCoreDB.getSphinxDatabaseQueries())
        }
}