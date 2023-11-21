package dev.kensa.context

import dev.kensa.StateExtractor
import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object KotestThen {
    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        block(extractor.execute(testContext.interactions))
    }

    fun <T> then(testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        invokeMatcher(extractor.execute(testContext.interactions), match)
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        continually(duration) {
            invokeMatcher(extractor.execute(testContext.interactions), match)
        }
    }

    suspend fun <T> thenContinually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        continually(duration) {
            block(extractor.execute(testContext.interactions))
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, match: Matcher<T>) {
        eventually(duration) {
            invokeMatcher(extractor.execute(testContext.interactions), match)
        }
    }

    suspend fun <T> thenEventually(duration: Duration = 10.seconds, testContext: TestContext, extractor: StateExtractor<T>, block: T.() -> Unit) {
        eventually(duration) {
            block(extractor.execute(testContext.interactions))
        }
    }
}