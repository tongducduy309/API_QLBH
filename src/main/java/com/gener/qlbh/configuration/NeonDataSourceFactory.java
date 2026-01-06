package com.gener.qlbh.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class NeonDataSourceFactory {

    private final Environment env;
    // cache DataSource để reuse pool; null = chưa khởi tạo
    private final AtomicReference<HikariDataSource> dsRef = new AtomicReference<>();

    public NeonDataSourceFactory(Environment env) {
        this.env = env;
    }

    // lazy init DataSource (thread-safe)
    public DataSource getOrCreateDataSource() {
        HikariDataSource ds = dsRef.get();
        if (ds != null) return ds;

        synchronized (dsRef) {
            ds = dsRef.get();
            if (ds != null) return ds;

            String url = env.getProperty("neon.datasource.url");
            String user = env.getProperty("neon.datasource.username");
            String pass = env.getProperty("neon.datasource.password");
            int maxPool = Integer.parseInt(env.getProperty("neon.datasource.maximum-pool-size", "5"));

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            if (user != null && !user.isEmpty()) cfg.setUsername(user);
            if (pass != null && !pass.isEmpty()) cfg.setPassword(pass);
            cfg.setMaximumPoolSize(maxPool);
            // timeout / validation settings (tùy chỉnh)
            cfg.setConnectionTimeout(10_000);
            cfg.setValidationTimeout(3_000);
            cfg.setIdleTimeout(60_000);
            cfg.setMaxLifetime(10 * 60_000);

            HikariDataSource hds = new HikariDataSource(cfg);
            dsRef.set(hds);
            return hds;
        }
    }

    public JdbcTemplate createJdbcTemplate() {
        return new JdbcTemplate(getOrCreateDataSource());
    }

    public TransactionTemplate createTransactionTemplate() {
        DataSource ds = getOrCreateDataSource();
        DataSourceTransactionManager txm = new DataSourceTransactionManager(ds);
        return new TransactionTemplate(txm);
    }

    // nếu muốn đóng pool khi app stop
    @PreDestroy
    public void close() {
        HikariDataSource ds = dsRef.getAndSet(null);
        if (ds != null) ds.close();
    }

    // optional: force close pool (nếu bạn muốn tạo/close cho mỗi sync run)
    public void shutdownPool() {
        HikariDataSource ds = dsRef.getAndSet(null);
        if (ds != null) ds.close();
    }
}

