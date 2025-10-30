package com.gener.qlbh.context;

import com.gener.qlbh.models.Company;

public final class TenantContext {

    private static final ThreadLocal<Company> HOLDER = new ThreadLocal<>();

    private TenantContext() {} // chặn khởi tạo

    public static void set(Company company) {
        HOLDER.set(company);
    }

    public static Company get() {
        return HOLDER.get();
    }

    public static Company required() {
        Company c = HOLDER.get();
        if (c == null) {
            throw new IllegalStateException("No company found in current context");
        }
        return c;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
