package com.gener.qlbh.configuration;

import com.gener.qlbh.context.TenantContext;
import com.gener.qlbh.models.Company;
import com.gener.qlbh.repositories.CompanyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CompanyContextFilter extends OncePerRequestFilter {
    @Autowired
    private CompanyRepository companyRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Ví dụ lấy companyId từ header
            String header = request.getHeader("X-Company-Id");
            if (header != null) {
                Long companyId = Long.parseLong(header);
                Company company = companyRepo.getReferenceById(companyId);
                TenantContext.set(company);
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear(); // tránh rò rỉ ThreadLocal
        }
    }
}
