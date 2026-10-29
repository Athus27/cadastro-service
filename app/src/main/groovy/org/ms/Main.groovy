package org.ms
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.ms.controller.CandidateController
import org.ms.dao.user.CandidateDAO


class Main {
    static void main(String[] args) {
        int port = 8080

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0)
        CandidateDAO candidateDAO = new CandidateDAO()
        CandidateController candidateController = new CandidateController(candidateDAO)

        server.createContext("/candidates", candidateController)


        server.start()
        println("Servidor rodando em http://localhost:${port}/")
    }
}
