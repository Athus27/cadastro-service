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

        exchange.httpContext >> context
        context.path >> "/companies"
        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 404 caso o id da empresa não seja encontrado"() {
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/companies/999")

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody

        when:
        exchange.httpContext.path >> "/companies"
        exchange.requestURI.path >> "/companies/999" // ID que não existe
        companyController.handle(exchange)

        then:
        1 * companyDAO.buscarEmpresaPorId(999) >> null
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve cadastrar empresa recebida como JSON"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/companies")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","cnpj":"12.345.678/0001-00"}')

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.adicionarEmpresa({ Map empresa ->
            empresa.nome == "Tapetes Bolinho" && empresa.cnpj == "12.345.678/0001-00"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve atualizar empresa usando o id da URL"() {
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/companies/8")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho atualizada"}')

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.atualizarEmpresa({ Map empresa ->
            empresa.id == 8 && empresa.nome == "Tapetes Bolinho atualizada"
        }) >> true
        1 * exchange.sendResponseHeaders(200, _)
    }

    def "deve remover empresa usando o id da URL"() {
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/companies/8")

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.removerEmpresa(8) >> true
        1 * exchange.sendResponseHeaders(204, -1)
    }

    def "deve responder 400 para JSON inválido no cadastro"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/companies")
        exchange.requestBody >> jsonBody('{"nome":')

        when:
        companyController.handle(exchange)

        then:
        0 * companyDAO.adicionarEmpresa(_)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 404 ao atualizar empresa sem id"() {
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/companies")

        when:
        companyController.handle(exchange)

        then:
        0 * companyDAO.atualizarEmpresa(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 404 quando o DAO não remover a empresa"() {
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/companies/999")

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.removerEmpresa(999) >> false
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 404 para id de empresa fora do limite de Integer"() {
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/companies/99999999999999999999")

        when:
        companyController.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 500 quando o DAO falhar ao cadastrar empresa"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/companies")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho"}')

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.adicionarEmpresa(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 400 quando o DAO não cadastrar a empresa"() {
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/companies")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho"}')

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.adicionarEmpresa(_) >> false
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 404 quando o DAO não atualizar a empresa"() {
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/companies/999")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho"}')

        when:
        companyController.handle(exchange)

        then:
        1 * companyDAO.atualizarEmpresa(_) >> false
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 404 ao remover empresa sem id"() {
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/companies")

        when:
        companyController.handle(exchange)

        then:
        0 * companyDAO.removerEmpresa(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    def "deve responder 405 para método não permitido"() {
        exchange.requestMethod >> "PATCH"
        exchange.requestURI >> URI.create("/companies/1")

        when:
        companyController.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST, PUT, DELETE"
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
