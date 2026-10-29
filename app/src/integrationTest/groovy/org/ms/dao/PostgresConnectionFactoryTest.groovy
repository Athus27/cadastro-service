package org.ms.dao

import org.ms.dao.connection.PostgresConnectionFactory
import spock.lang.Specification

import java.sql.Connection

class PostgresConnectionFactoryTest extends Specification {
    private PostgresConnectionFactory conexaoDB
    private Connection conexao

    private static final Properties DB_PROPS = carregarPropriedades()

    private static Properties carregarPropriedades() {
        String resourceName = "db.properties"
        InputStream resourceStream = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream(resourceName)

        if (resourceStream == null) {
            throw new FileNotFoundException(
                    "Arquivo de propriedades '${resourceName}' não encontrado no classpath."
            )
        }

        Properties properties = new Properties()
        resourceStream.withCloseable {
            properties.load(it)
        }
        return properties
    }

    def setup() {
        conexaoDB = new PostgresConnectionFactory(DB_PROPS)
        conexao = conexaoDB.getConnection()
    }

    def "deve estabelecer conexao com o banco de dados com sucesso"() {
        expect:
        conexao != null
        !conexao.isClosed()
    }

    def cleanup() {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close()
        }
    }
}
