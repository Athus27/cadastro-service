package org.ms


import com.sun.net.httpserver.HttpServer
import org.ms.controller.CandidateController
import org.ms.controller.CompanyController
import org.ms.dao.user.CandidateDAO
import org.ms.dao.user.CompanyDAO


class Main {
    static void main(String[] args) {
        int port = 8080

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0)

        CandidateDAO candidateDAO = new CandidateDAO()
        CandidateController candidateController = new CandidateController(candidateDAO)

        server.createContext("/candidates", candidateController)

        CompanyDAO companyDAO = new CompanyDAO()
        CompanyController companyController = new CompanyController(companyDAO)

        server.createContext("/companies", companyController)


        server.start()
        println("Servidor rodando em http://localhost:${port}/")
    }
}
