package com.sap.bulletinboard.ads.multitenancy.beans;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

    private static final long serialVersionUID = 1L;

    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        connection.setSchema(tenantIdentifier);
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        // TODO: set schema back to default?
        connection.close();
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return connection;
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

}
