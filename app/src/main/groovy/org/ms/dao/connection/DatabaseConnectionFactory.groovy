package org.ms.dao.connection

class DatabaseConnectionFactory {
    private ConnectionFactory connectionFactory
    private static Properties DbProps = new Properties()

    static {
        def resourceName = "db.properties"
        def classLoader = Thread.currentThread().getContextClassLoader()
        def resourceStream = classLoader.getResourceAsStream(resourceName)

        if (resourceStream) {
            try {
                DbProps.load(resourceStream)
            } finally {
                resourceStream.close()
            }
        } else {
            throw new FileNotFoundException("Arquivo de propriedades '${resourceName}' não encontrado no classpath.")
        }

    }
    private static DatabaseConnectionFactory instance = new DatabaseConnectionFactory()

    private DatabaseConnectionFactory() {}
    public static DatabaseConnectionFactory getInstance(){
        return instance
    }

    public ConnectionFactory getFactoryDatabaseConnection(){
        String tipo = DbProps.getProperty("db.type")
        if (tipo=="postgres" && this.connectionFactory==null){
            this.connectionFactory = new PostgresConnectionFactory(DbProps)
            return this.connectionFactory
        }
        return this.connectionFactory
    }
}
