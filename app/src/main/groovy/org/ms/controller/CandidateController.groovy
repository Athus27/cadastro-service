package org.ms.controller

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import org.ms.dao.user.CandidateDAO

class CandidateController implements HttpHandler {
    private static candidateDAO

    CandidateController(CandidateDAO candidateDAO) {
        this.candidateDAO = candidateDAO
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
                    def matcher = (remainPath =~ /^\/([0-9]+)$/)
                    if (!matcher.matches()) {
                        responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                        return
                    }
                    id = matcher[0][1].toInteger()
                    resposta = candidateDAO.buscarCandidatoPorId(id)
                    if (resposta == null) {
                        responder(exchange, 404, '{"erro":"Candidato não encontrado"}')
                        return
                    }
                    status = 200
                    responseText = JsonOutput.toJson(resposta)

                } else {
                    resposta = candidateDAO.listarCandidatos()
                    status = 200
                    responseText = JsonOutput.toJson(resposta)
                }

                responder(exchange, status, responseText)
                break

            default:
                status = 405
                responseText = '{"erro":"Método não permitido"}'
                exchange.responseHeaders.set("Allow", "GET", "POST", "PUT", "DELETE")
                break
        }
    }

    private static void responder(
            HttpExchange exchange,
            int status,
            String responseText
    ) {
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
