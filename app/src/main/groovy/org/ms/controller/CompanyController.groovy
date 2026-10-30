package org.ms.controller

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.ms.dao.user.CompanyDAO

class CompanyController implements HttpHandler {
    private static CompanyDAO companyDAO

    CompanyController(CompanyDAO companyDAO) {
        this.companyDAO = companyDAO
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        String method = exchange.requestMethod
        String base = exchange.httpContext.path
        String completePath = exchange.requestURI.path
        String remainPath = completePath.substring(base.length())

        String responseText
        int status

        Integer id = null

        switch (method) {
            case "GET":
                Map resposta

                if (remainPath && remainPath != "/") {
                    id = extrairId(remainPath)
                    if (id == null) {
                        responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                        return
                    }
                    resposta = companyDAO.buscarEmpresaPorId(id)
                    if (resposta == null) {
                        responder(exchange, 404, '{"erro":"Empresa não encontrada"}')
                        return
                    }
                    status = 200
                    responseText = JsonOutput.toJson(resposta)

                } else {
                    resposta = companyDAO.listarEmpresas()
                    status = 200
                    responseText = JsonOutput.toJson(resposta)
                }

                responder(exchange, status, responseText)
                break

            case "POST":
                if (remainPath && remainPath != "/") {
                    responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                    return
                }

                Map empresa
                try {
                    empresa = new JsonSlurper().parse(exchange.requestBody) as Map
                } catch (Exception ignored) {
                    responder(exchange, 400, '{"erro":"JSON inválido"}')
                    return
                }

                try {
                    boolean cadastrada = companyDAO.adicionarEmpresa(empresa)
                    status = cadastrada ? 201 : 400
                    responseText = cadastrada
                            ? '{"mensagem":"Empresa cadastrada com sucesso"}'
                            : '{"erro":"Não foi possível cadastrar a empresa"}'
                } catch (Exception ignored) {
                    status = 500
                    responseText = '{"erro":"Erro interno do servidor"}'
                }
                responder(exchange, status, responseText)
                break

            case "PUT":
                id = extrairId(remainPath)
                if (id == null) {
                    responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                    return
                }

                Map empresa
                try {
                    empresa = new JsonSlurper().parse(exchange.requestBody) as Map
                } catch (Exception ignored) {
                    responder(exchange, 400, '{"erro":"JSON inválido"}')
                    return
                }

                try {
                    empresa.id = id
                    boolean atualizada = companyDAO.atualizarEmpresa(empresa)
                    status = atualizada ? 200 : 404
                    responseText = atualizada
                            ? '{"mensagem":"Empresa atualizada com sucesso"}'
                            : '{"erro":"Empresa não encontrada"}'
                } catch (Exception ignored) {
                    status = 500
                    responseText = '{"erro":"Erro interno do servidor"}'
                }
                responder(exchange, status, responseText)
                break

            case "DELETE":
                id = extrairId(remainPath)
                if (id == null) {
                    responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                    return
                }

                if (companyDAO.removerEmpresa(id)) {
                    responder(exchange, 204, "")
                } else {
                    responder(exchange, 404, '{"erro":"Empresa não encontrada"}')
                }
                break

            default:
                status = 405
                responseText = '{"erro":"Método não permitido"}'
                exchange.responseHeaders.set("Allow", "GET, POST, PUT, DELETE")
                responder(exchange, status, responseText)
                break
        }
    }

    private static Integer extrairId(String remainPath) {
        def matcher = (remainPath =~ /^\/([0-9]+)$/)
        if (!matcher.matches()) {
            return null
        }

        try {
            return matcher[0][1].toInteger()
        } catch (NumberFormatException ignored) {
            return null
        }
    }

    private static void responder(
            HttpExchange exchange,
            int status,
            String responseText
    ) {
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1)
            exchange.close()
            return
        }

        byte[] responseBytes = responseText.getBytes("UTF-8")

        exchange.responseHeaders.set(
                "Content-Type",
                "application/json; charset=UTF-8"
        )

        exchange.sendResponseHeaders(status, responseBytes.length)

        exchange.responseBody.withStream { out ->
            out.write(responseBytes)
        }
    }
}
