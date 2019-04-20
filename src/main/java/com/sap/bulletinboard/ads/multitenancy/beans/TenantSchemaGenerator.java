package com.sap.bulletinboard.ads.multitenancy.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;

public class TenantSchemaGenerator implements InitializingBean {

    private final DataSource dataSource;
    private final List<String> schemaNames;

    public TenantSchemaGenerator(DataSource dataSource, List<String> schemaNames) {
        this.dataSource = dataSource;
        this.schemaNames = schemaNames;
    }

    @Override
    public void afterPropertiesSet() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            for (String schemaName : schemaNames) {
                ensureSchemaExists(connection, schemaName);
            }
        }
    }

    private void ensureSchemaExists(Connection connection, String schemaName) throws SQLException {
        if (!schemaExists(connection, schemaName)) {
            createSchema(connection, schemaName);
        }

        if (!schemaExists(connection, schemaName)) {
            throw new SQLException(
                    "Schema " + schemaName + " could not be created. Maybe case-(in)sensitivity is the issue?");
        }
    }

    private boolean schemaExists(Connection connection, String schemaName) throws SQLException {
        boolean exists = false;
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT COUNT(*) FROM information_schema.schemata WHERE SCHEMA_NAME = ?")) {
            statement.setString(1, schemaName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count == 1) {
                        exists = true;
                    }
                }
            }
        }
        return exists;
    }

    private void createSchema(Connection connection, String schemaName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // TODO: no sql injection must happen here! (cannot use prepared statement)
            // use quotes around schema name to care for case-sensitivity
            statement.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");
        }
    }
}