package server

import di.appModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class ApplicationTest :
    FunSpec({

        beforeTest {
            stopKoin()
            startKoin { modules(appModule) }
        }
        afterTest {
            stopKoin()
        }

        test("GET / возвращает HTML") {
            testApplication {
                application { module() }

                val resp = client.get("/")
                resp.status shouldBe HttpStatusCode.OK
                resp.bodyAsText() shouldContain "<!DOCTYPE html>"
            }
        }

        test("GET /api/evaluate без joke → 400") {
            testApplication {
                application { module() }
                val resp = client.get("/api/evaluate")
                resp.status shouldBe HttpStatusCode.BadRequest
                resp.bodyAsText() shouldContain "Параметр 'joke' обязателен"
            }
        }
    })
