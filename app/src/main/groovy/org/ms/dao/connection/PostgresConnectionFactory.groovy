package org.ms.dao.connection

import groovy.transform.PackageScope

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class PostgresConnectionFactory implements ConnectionFactory {
    private final Properties props

    PostgresConnectionFactory(Properties props) {
        this.props = props
    }

    Connection getConnection() {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        )
    }

    @PackageScope
    boolean mostrarTabelas() {
        Connection connection = null
        Statement statement = null
        ResultSet resultSet = null
        boolean result = false

        try {
            connection = getConnection()
            statement = connection.createStatement()

            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
            resultSet = statement.executeQuery(sql)

            println "--- Tabelas do Sistema ---"
            while (resultSet.next()) {
                println resultSet.getString("table_name")
            }

            result = true

        } catch (Exception e) {
            println "Erro ao mostrar tabelas: ${e.message}"
            result = false
        } finally {
            if (resultSet != null) {
                resultSet.close()
            }
            if (statement != null) {
                statement.close()
            }
            if (connection != null) {
                connection.close()
            }
        }

        return result
    }
}