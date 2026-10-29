package org.ms.dao.user


import org.ms.dao.competence.CandidateCompetenceDAO
import org.ms.dao.competence.CompetenceDAO
import org.ms.dao.connection.DatabaseConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CandidateDAO {
    Map buscarCandidatoPorId(int idCandidato) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        try {
            connection = DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection().getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT
                            candidate.id_candidato AS id,
                            user_data.nome AS nome,
                            candidate.sobrenome AS sobrenome,
                            candidate.data_nascimento AS data_nascimento,
                            candidate.cpf AS cpf,
                            candidate.descricao AS descricao,
                            user_data.email AS email,
                            candidate.formacao AS formacao,
                            user_data."país" AS pais,
                            user_data.cep AS cep
                        FROM "Candidate" AS candidate
                        JOIN "User" AS user_data
                            ON user_data.id = candidate.id_candidato
                        WHERE candidate.id_candidato = ?
                    """
            )
            statement.setInt(1, idCandidato)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                def competenceDAO = new CandidateCompetenceDAO()
                List<String> competencias = competenceDAO.listarCompetenciasDoCandidato(idCandidato)

                return [id            : resultSet.getInt("id"),
                        nome          : resultSet.getString("nome"),
                        sobrenome     : resultSet.getString("sobrenome"),
                        dataNascimento: resultSet.getDate("data_nascimento"),
                        cpf           : resultSet.getString("cpf"),
                        descricao     : resultSet.getString("descricao"),
                        email         : resultSet.getString("email"),
                        formacao      : resultSet.getString("formacao"),
                        pais          : resultSet.getString("pais"),
                        cep           : resultSet.getString("cep"),
                        competencias  : competencias]
            } else {
                return null
            }

        } catch (Exception e) {
            println("Erro ao buscar candidato por ID: ${e.message}")
            return null
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
    }

    Map listarCandidatos() {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        def candidatos = []

        try {
            connection = DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection().getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT
                            candidate.id_candidato AS id,
                            user_data.nome AS nome,
                            candidate.sobrenome AS sobrenome,
                            candidate.data_nascimento AS data_nascimento,
                            candidate.cpf AS cpf,
                            candidate.descricao AS descricao,
                            user_data.email AS email,
                            candidate.formacao AS formacao,
                            user_data."país" AS pais,
                            user_data.cep AS cep
                        FROM "Candidate" AS candidate
                        JOIN "User" AS user_data
                            ON user_data.id = candidate.id_candidato
                    """
            )
            resultSet = statement.executeQuery()

            def competenceDAO = new CandidateCompetenceDAO()

            while (resultSet.next()) {
                int idCandidato = resultSet.getInt("id")

                //Listando as competências do candidato usando o CandidateCompetenceDAO(MOSTRE O SQL)
                List<String> competencias = competenceDAO.listarCompetenciasDoCandidato(idCandidato)

                candidatos << [id            : idCandidato,
                               nome          : resultSet.getString("nome"),
                               sobrenome     : resultSet.getString("sobrenome"),
                               dataNascimento: resultSet.getDate("data_nascimento"),
                               cpf           : resultSet.getString("cpf"),
                               descricao     : resultSet.getString("descricao"),
                               email         : resultSet.getString("email"),
                               formacao      : resultSet.getString("formacao"),
                               pais          : resultSet.getString("pais"),
                               cep           : resultSet.getString("cep"),
                               competencias  : competencias
                ]
            }

        } catch (Exception e) {
            println("Erro ao listar candidatos: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return [candidatos: candidatos]
    }

    List<Map> feedCandidatos() {
        List<Map> candidatos = listarCandidatos().candidatos

        return candidatos.collect { candidato ->
            [
                    descricao   : candidato.descricao,
                    formacao    : candidato.formacao,
                    competencias: candidato.competencias
            ]
        }
    }

    boolean adicionarCandidato(Map candidato) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection().getConnection()
            connection.setAutoCommit(false)

            // Inserindo dados na tabela User
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "User" (nome, email, "país", cep,senha)
                        VALUES (?, ?, ?, ?,crypt(?, gen_salt('bf')))
                        RETURNING id
                    """
            )
            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.email)
            statement.setString(3, candidato.pais)
            statement.setString(4, candidato.cep)
            statement.setString(5, candidato.senha)

            def resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                throw new SQLException("banco n retornou id")
            }
            int userId = resultSet.getInt("id")

            resultSet.close()
            statement.close()

            // Inserindo dados na tabela Candidate
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "Candidate" (id_candidato, sobrenome, data_nascimento, cpf, descricao, formacao)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """
            )
            statement.setInt(1, userId)
            statement.setString(2, candidato.sobrenome)

            DateTimeFormatter formatoData =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate dataNascimento =
                    LocalDate.parse(candidato.dataNascimento, formatoData)
            statement.setDate(3, java.sql.Date.valueOf(dataNascimento))

            statement.setString(4, candidato.cpf)
            statement.setString(5, candidato.descricao)
            statement.setString(6, candidato.formacao)

            int rowsAffected = statement.executeUpdate()
            sucesso = rowsAffected > 0

            if (sucesso) {
                CompetenceDAO competenceDAO = new CompetenceDAO()
                CandidateCompetenceDAO candidateCompetenceDAO =
                        new CandidateCompetenceDAO()

                List<String> nomesCompetencias = candidato.competencias ?: []

                nomesCompetencias.each { String nomeCompetencia ->
                    Map competenciaSalva = competenceDAO.buscarOuCriarCompetencia(connection, nomeCompetencia)

                    boolean vinculada = candidateCompetenceDAO.adicionarCompetenciaAoCandidato(connection, userId,competenciaSalva.id as int)

                    if (!vinculada) {
                        throw new SQLException(
                                "Não foi possível vincular '${nomeCompetencia}' ao candidato"
                        )
                    }
                }

                connection.commit()
                println("Candidato adicionado com sucesso!")
            } else {
                connection.rollback()
                println("Falha ao adicionar candidato. Rolando de volta.")
            }

        } catch (Exception e) {
            println("Erro ao adicionar candidato: ${e.message}")
            try {
                if (connection != null) {
                    connection.rollback()
                    println("Rolando de volta devido a erro.")
                }
            } catch (Exception rollbackEx) {
                println("Erro ao tentar rolar de volta: ${rollbackEx.message}")
            }
        } finally {
            try {
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return sucesso
    }

    boolean atualizarCandidato(Map candidato) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection().getConnection()
            connection.setAutoCommit(false)

            statement = connection.prepareStatement(
                    """
                        UPDATE "User"
                        SET nome = ?, email = ?, "país" = ?, cep = ?,
                            senha = crypt(?, gen_salt('bf'))
                        WHERE id = ?
                    """
            )
            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.email)
            statement.setString(3, candidato.pais)
            statement.setString(4, candidato.cep)
            statement.setString(5, candidato.senha)
            statement.setInt(6, candidato.id as int)

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Candidato não encontrado")
            }

            statement.close()
            statement = connection.prepareStatement(
                    """
                        UPDATE "Candidate"
                        SET sobrenome = ?, data_nascimento = ?, cpf = ?,
                            descricao = ?, formacao = ?
                        WHERE id_candidato = ?
                    """
            )
            statement.setString(1, candidato.sobrenome)

            DateTimeFormatter formatoData =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate dataNascimento =
                    LocalDate.parse(candidato.dataNascimento, formatoData)
            statement.setDate(2, java.sql.Date.valueOf(dataNascimento))

            statement.setString(3, candidato.cpf)
            statement.setString(4, candidato.descricao)
            statement.setString(5, candidato.formacao)
            statement.setInt(6, candidato.id as int)

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Dados do candidato não encontrados")
            }

            CandidateCompetenceDAO candidateCompetenceDAO =
                    new CandidateCompetenceDAO()
            CompetenceDAO competenceDAO = new CompetenceDAO()

            candidateCompetenceDAO.removerCompetenciasDoCandidato(
                    connection,
                    candidato.id as int
            )

            List<String> competencias = candidato.competencias ?: []
            competencias.each { String nomeCompetencia ->
                Map competencia = competenceDAO.buscarOuCriarCompetencia(
                        connection,
                        nomeCompetencia
                )

                if (!candidateCompetenceDAO.adicionarCompetenciaAoCandidato(
                        connection,
                        candidato.id as int,
                        competencia.id as int
                )) {
                    throw new SQLException(
                            "Não foi possível vincular '${nomeCompetencia}' ao candidato"
                    )
                }
            }

            connection.commit()
            sucesso = true
            println("Candidato atualizado com sucesso!")
        } catch (Exception e) {
            println("Erro ao atualizar candidato: ${e.message}")
            if (connection != null) connection.rollback()
        } finally {
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

    boolean removerCandidato(int idCandidato) {
        Connection connection = null
        boolean sucesso = false

        try {
            connection = DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection().getConnection()
            connection.setAutoCommit(false)

            List<String> comandos = [
                    'DELETE FROM "Curtida" WHERE id_candidato = ?',
                    'DELETE FROM "CandidatoCompetencia" WHERE id_candidato = ?',
                    'DELETE FROM "Candidate" WHERE id_candidato = ?',
                    'DELETE FROM "User" WHERE id = ?'
            ]

            comandos.eachWithIndex { String sql, int indice ->
                PreparedStatement statement = connection.prepareStatement(sql)
                try {
                    statement.setInt(1, idCandidato)
                    int linhasAfetadas = statement.executeUpdate()
                    if (indice >= 2 && linhasAfetadas != 1) {
                        throw new SQLException("Candidato não encontrado")
                    }
                } finally {
                    statement.close()
                }
            }

            connection.commit()
            sucesso = true
            println("Candidato removido com sucesso!")
        } catch (Exception e) {
            println("Erro ao remover candidato: ${e.message}")
            if (connection != null) connection.rollback()
        } finally {
            if (connection != null) connection.close()
        }

        return sucesso
    }

}
