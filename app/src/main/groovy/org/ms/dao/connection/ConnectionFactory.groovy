package org.ms.dao.connection

import java.sql.Connection

interface ConnectionFactory {
    Connection getConnection()
}