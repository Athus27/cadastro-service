package org.ms.controller

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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
                    id = extrairId(remainPath)
                    if (id == null) {
                        responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                        return
                    }
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

            case "POST":
                if (remainPath && remainPath != "/") {
                    responder(exchange, 404, '{"erro":"Rota não encontrada"}')
                    return
                }

                Map candidato
                try {
                    candidato = new JsonSlurper().parse(exchange.requestBody) as Map
                } catch (Exception ignored) {
                    responder(exchange, 400, '{"erro":"JSON inválido"}')
                    return
                }

                try {
                    boolean cadastrado = candidateDAO.adicionarCandidato(candidato)
                    status = cadastrado ? 201 : 400
                    responseText = cadastrado
                            ? '{"mensagem":"Candidato cadastrado com sucesso"}'
                            : '{"erro":"Não foi possível cadastrar o candidato"}'
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

                Map candidato
                try {
                    candidato = new JsonSlurper().parse(exchange.requestBody) as Map
                } catch (Exception ignored) {
                    responder(exchange, 400, '{"erro":"JSON inválido"}')
                    return
                }

                try {
                    candidato.id = id
                    boolean atualizado = candidateDAO.atualizarCandidato(candidato)
                    status = atualizado ? 200 : 404
                    responseText = atualizado
                            ? '{"mensagem":"Candidato atualizado com sucesso"}'
                            : '{"erro":"Candidato não encontrado"}'
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

                if (candidateDAO.removerCandidato(id)) {
                    responder(exchange, 204, "")
                } else {
                    responder(exchange, 404, '{"erro":"Candidato não encontrado"}')
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
