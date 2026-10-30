package org.ms.controller

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpExchange
import org.ms.dao.user.CompanyDAO
import spock.lang.Specification

class CompanyControllerTest extends Specification {
    private CompanyController companyController
    private CompanyDAO companyDAO
    private HttpExchange exchange
    private HttpContext context
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody

    def setup() {
        companyDAO = Mock(CompanyDAO)
        companyController = new CompanyController(companyDAO)

        exchange = Mock(HttpExchange)
        context = Mock(HttpContext)

        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()

        exchange.requestMethod >> "GET"
        exchange.httpContext >> context
        context.path >> "/companies"
        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 404 caso o id da empresa não seja encontrado"() {
        exchange.requestURI >> URI.create("/companies/999")

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody

        when:
        exchange.requestMethod >> "GET"
        exchange.httpContext.path >> "/companies"
        exchange.requestURI.path >> "/companies/999" // ID que não existe
        companyController.handle(exchange)

        then:
        1 * companyDAO.buscarEmpresaPorId(999) >> null
        1 * exchange.sendResponseHeaders(404, _)
    }
}
