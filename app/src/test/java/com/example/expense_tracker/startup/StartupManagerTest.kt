package com.example.expense_tracker.startup

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StartupManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private class TestStartupTask(
        override val id: String,
        override val phase: StartupPhase,
        override val timeoutMs: Long = 1000L,
        private val block: suspend () -> Unit = {}
    ) : StartupTask {
        var isExecuted: Boolean = false
            private set

        override suspend fun invoke() {
            block()
            isExecuted = true
        }
    }

    private class TestIdempotentTask(
        override val id: String,
        override val phase: StartupPhase = StartupPhase.NORMAL,
        private val counter: AtomicInteger
    ) : IdempotentStartupTask() {
        override suspend fun execute() {
            counter.incrementAndGet()
        }
    }

    @Test
    fun `runCriticalTasks executes only CRITICAL tasks`() = runTest(testDispatcher) {
        val criticalTask = TestStartupTask("critical", StartupPhase.CRITICAL)
        val normalTask = TestStartupTask("normal", StartupPhase.NORMAL)
        val deferredTask = TestStartupTask("deferred", StartupPhase.BACKGROUND)

        val manager = StartupManager(
            tasks = listOf(criticalTask, normalTask, deferredTask),
            appScope = testScope,
            defaultDispatcher = testDispatcher
        )

        manager.runCriticalTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(criticalTask.isExecuted)
        assertTrue(!normalTask.isExecuted)
        assertTrue(!deferredTask.isExecuted)
    }

    @Test
    fun `runNormalTasks executes only NORMAL tasks`() = runTest(testDispatcher) {
        val criticalTask = TestStartupTask("critical", StartupPhase.CRITICAL)
        val normalTask = TestStartupTask("normal", StartupPhase.NORMAL)
        val deferredTask = TestStartupTask("deferred", StartupPhase.BACKGROUND)

        val manager = StartupManager(
            tasks = listOf(criticalTask, normalTask, deferredTask),
            appScope = testScope,
            defaultDispatcher = testDispatcher
        )

        manager.runNormalTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(!criticalTask.isExecuted)
        assertTrue(normalTask.isExecuted)
        assertTrue(!deferredTask.isExecuted)
    }

    @Test
    fun `runDeferredTasks executes only BACKGROUND tasks`() = runTest(testDispatcher) {
        val criticalTask = TestStartupTask("critical", StartupPhase.CRITICAL)
        val normalTask = TestStartupTask("normal", StartupPhase.NORMAL)
        val deferredTask = TestStartupTask("deferred", StartupPhase.BACKGROUND)

        val manager = StartupManager(
            tasks = listOf(criticalTask, normalTask, deferredTask),
            appScope = testScope,
            defaultDispatcher = testDispatcher
        )

        manager.runDeferredTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(!criticalTask.isExecuted)
        assertTrue(!normalTask.isExecuted)
        assertTrue(deferredTask.isExecuted)
    }

    @Test
    fun `IdempotentStartupTask only executes once even with concurrent calls`() = runTest(testDispatcher) {
        val executionCounter = AtomicInteger(0)
        val idempotentTask = TestIdempotentTask("idempotent_test", StartupPhase.NORMAL, executionCounter)

        // Invoke simultaneously from multiple coroutines
        val jobs = (1..10).map {
            async(testDispatcher) {
                idempotentTask()
            }
        }
        jobs.awaitAll()

        assertEquals(1, executionCounter.get())
    }

    @Test
    fun `task timeout does not hang StartupManager`() = runTest(testDispatcher) {
        val hangingTask = TestStartupTask("hanging", StartupPhase.CRITICAL, timeoutMs = 50L) {
            delay(500L)
        }

        val manager = StartupManager(
            tasks = listOf(hangingTask),
            appScope = testScope,
            defaultDispatcher = testDispatcher
        )

        // Should complete safely after timeout without throwing or hanging
        manager.runCriticalTasks()
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
