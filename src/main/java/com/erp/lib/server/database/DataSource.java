package com.erp.lib.server.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSource {

    private HikariDataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(DataSource.class);
    private final Provider<HttpServletRequest> httpServletRequestProvider;

    @Inject
    public DataSource(Provider<HttpServletRequest> httpServletRequestProvider) {
        this.httpServletRequestProvider = httpServletRequestProvider;
    }

    public void init(String server, String name, String username, String password) {
        try {
            logger.info("Initiliazing database pool ...");

            HikariConfig config = new HikariConfig();
            config.setMaximumPoolSize(20);
            config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
            if (server != null) {
                config.addDataSourceProperty("serverName", server);
                logger.info("DB server: " + server);
            }
            if (name != null) {
                config.addDataSourceProperty("databaseName", name);
                logger.info("DB name: " + name);
            }
            if (username != null) {
                config.addDataSourceProperty("user", username);
                logger.info("DB username: " + username);
            }
            if (password != null) {
                config.addDataSourceProperty("password", password);
            }

            dataSource = new HikariDataSource(config);

        } catch (Exception ex) {
            logger.error("Unable to create connection pool");
            ex.printStackTrace();
        }

    }

    public void saveTransactionState() throws SQLException {
        httpServletRequestProvider.get().setAttribute("transactionState", getConnection().getAutoCommit());
    }

    public void restoreTransactionState() throws SQLException {
        Boolean transactionState = (Boolean) httpServletRequestProvider.get().getAttribute("transactionState");
        if (transactionState != null) {
            getConnection().setAutoCommit(transactionState);
        }
    }

    public void activateLocale(boolean enabled) throws SQLException {
        Connection connection = null;
        HttpServletRequest request = httpServletRequestProvider.get();

        Object connectionAttribute = request.getAttribute("connection");
        String language = (String) request.getAttribute("language");
        request.setAttribute("localeEnabled", enabled);

        if (connectionAttribute != null) {
            connection = (Connection) connectionAttribute;
            connection.setAutoCommit(true);

            if (enabled) {
                try (PreparedStatement ps = connection.prepareStatement("SELECT set_locale(?)")) {
                    ps.setString(1, language);
                    ps.execute();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement("SELECT reset_locale()")) {
                    ps.execute();
                }
            }
        }
    }

    public String getConnectionLocale() {
        HttpServletRequest request = httpServletRequestProvider.get();
        return (String) request.getAttribute("language");
    }

    public Connection getConnection() throws SQLException {
        Connection connection = null;
        HttpServletRequest request = httpServletRequestProvider.get();

        Object connectionAttribute = request.getAttribute("connection");
        String language = (String) request.getAttribute("language");

        Boolean localeEnabled = (Boolean) request.getAttribute("localeEnabled");
        if (localeEnabled == null) {
            localeEnabled = true;
        }

        if (connectionAttribute == null) {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            request.setAttribute("connection", connection);

            if (localeEnabled) {
                try (PreparedStatement ps = connection.prepareStatement("SELECT set_locale(?)")) {
                    ps.setString(1, language);
                    ps.execute();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement("SELECT reset_locale()")) {
                    ps.execute();
                }
            }
        } else {
            connection = (Connection) connectionAttribute;
        }

        return connection;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void destroy() {
        logger.info("Closing pool of connections");
        dataSource.close();
    }
}
