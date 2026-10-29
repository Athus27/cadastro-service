package org.ms.dao.competence

import org.ms.dao.connection.DatabaseConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class VagaCompetenceDAO {

    List<String> listarCompetenciasDaVaga(int idVaga) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null
        List<String> competencias = []

        try {
            connection = DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection().getConnection()

            statement = connection.prepareStatement(
                    """
                        SELECT competencia.competencia
                        FROM "VagaCompetencia" AS vaga_competencia
                        JOIN "Competencias" AS competencia
                            ON competencia.id = vaga_competencia.id_competencia
                        WHERE vaga_competencia.id_vaga = ?
                        ORDER BY competencia.competencia
                    """
            )

            statement.setInt(1, idVaga)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                competencias << resultSet.getString("competencia")
            }
        } finally {
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return competencias
    }

    boolean adicionarCompetenciaAVaga(
            Connection connection,
            int idVaga,
            int idCompetencia
    ) {
        PreparedStatement statement = null

        try {
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "VagaCompetencia"
                            (id_vaga, id_competencia)
                        VALUES (?, ?)
                    """
            )

            statement.setInt(1, idVaga)
            statement.setInt(2, idCompetencia)

            return statement.executeUpdate() == 1
        } finally {
            if (statement != null) statement.close()
        }
    }

    void removerCompetenciasDaVaga(Connection connection, int idVaga) {
        PreparedStatement statement = null

        try {
            statement = connection.prepareStatement(
                    """
                        DELETE FROM "VagaCompetencia"
                        WHERE id_vaga = ?
                    """
            )
            statement.setInt(1, idVaga)
            statement.executeUpdate()
        } finally {
            if (statement != null) statement.close()
        }
    }
}
