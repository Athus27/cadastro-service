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

        exchange.requestMethod >> "GET"
        exchange.httpContext >> context
        context.path >> "/candidates"
        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 404 caso o id do candidato não seja encontrado"() {
        exchange.requestURI >> URI.create("/candidates/999")

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody

        when:
        exchange.requestMethod >> "GET"
        exchange.httpContext.path >> "/candidates"
        exchange.requestURI.path >> "/candidates/999" // ID que não existe
        candidateController.handle(exchange)

        then:
        1 * candidateDAO.buscarCandidatoPorId(999) >> null
        1 * exchange.sendResponseHeaders(404, _)
    }
}
