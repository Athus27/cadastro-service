package org.ms.controller

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpExchange
import org.ms.dao.user.CandidateDAO
import spock.lang.Specification

class CandidateControllerTest extends Specification {
    private CandidateController candidateController
    private CandidateDAO candidateDAO
    private HttpExchange exchange
    private HttpContext context
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody

    def setup() {
        candidateDAO = Mock(CandidateDAO)
        candidateController = new CandidateController(candidateDAO)

        exchange = Mock(HttpExchange)
        context = Mock(HttpContext)

        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()

        exchange.httpContext >> context
        context.path >> "/candidates"
        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 404 caso o id do candidato não seja encontrado"() {
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/candidates/999")

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody

        when:
        exchange.httpContext.path >> "/candidates"
        exchange.requestURI.path >> "/candidates/999" // ID que não existe
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.buscarCandidatoPorId(999) >> null
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve cadastrar candidato recebido como JSON"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/candidates")
        exchange.requestBody >> jsonBody('{"nome":"Batatiudo","email":"batata@email.com"}')

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.adicionarCandidato({ Map candidato ->
            candidato.nome == "Batatiudo" && candidato.email == "batata@email.com"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve atualizar candidato usando o id da URL"() {
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/candidates/7")
        exchange.requestBody >> jsonBody('{"nome":"Batatiudo atualizado"}')

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.atualizarCandidato({ Map candidato ->
            candidato.id == 7 && candidato.nome == "Batatiudo atualizado"
        }) >> true
        1 * exchange.sendResponseHeaders(200, _)
    }

    def "deve remover candidato usando o id da URL"() {
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/candidates/7")

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.removerCandidato(7) >> true
        1 * exchange.sendResponseHeaders(204, -1)
    }

    def "deve responder 400 para JSON inválido no cadastro"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/candidates")
        exchange.requestBody >> jsonBody('{"nome":')

        when:
        candidateController.handle(exchange)

        then:
        0 * candidateDAO.adicionarCandidato(_)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 404 ao atualizar candidato sem id"() {
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/candidates")

        when:
        candidateController.handle(exchange)

        then:
        0 * candidateDAO.atualizarCandidato(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 404 quando o DAO não remover o candidato"() {
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/candidates/999")

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.removerCandidato(999) >> false
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 404 para id de candidato fora do limite de Integer"() {
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/candidates/99999999999999999999")

        when:
        candidateController.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 500 quando o DAO falhar ao cadastrar candidato"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/candidates")
        exchange.requestBody >> jsonBody('{"nome":"Batatiudo"}')

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.adicionarCandidato(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 400 quando o DAO não cadastrar o candidato"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/candidates")
        exchange.requestBody >> jsonBody('{"nome":"Batatiudo"}')

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.adicionarCandidato(_) >> false
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 404 quando o DAO não atualizar o candidato"() {
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/candidates/999")
        exchange.requestBody >> jsonBody('{"nome":"Batatiudo"}')

        when:
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.atualizarCandidato(_) >> false
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 404 ao remover candidato sem id"() {
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/candidates")

        when:
        candidateController.handle(exchange)

        then:
        0 * candidateDAO.removerCandidato(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 405 para método não permitido"() {
        exchange.requestMethod >> "PATCH"
        exchange.requestURI >> URI.create("/candidates/1")

        when:
        candidateController.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST, PUT, DELETE"
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
