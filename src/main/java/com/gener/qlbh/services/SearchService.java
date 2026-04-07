package com.gener.qlbh.services;

import com.gener.qlbh.dtos.response.SearchSuggestionRes;
import com.gener.qlbh.models.Customer;
import com.gener.qlbh.models.Order;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.PurchaseReceipts;
import com.gener.qlbh.repositories.CustomerRepository;
import com.gener.qlbh.repositories.OrderRepository;
import com.gener.qlbh.repositories.ProductRepository;
import com.gener.qlbh.repositories.PurchaseReceiptsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseReceiptsRepository purchaseReceiptsRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public List<SearchSuggestionRes> globalSearch(String keyword, int limit) {
        String q = normalize(keyword);
        if (q.isBlank()) return Collections.emptyList();

        List<SearchSuggestionRes> orders = orderRepository.findAll().stream()
                .filter(o -> containsAny(q,
                        o.getCode(),
                        o.getNameCustomer(),
                        o.getPhoneCustomer(),
                        o.getAddressCustomer()))
                .map(this::mapOrder)
                .toList();

        List<SearchSuggestionRes> products = productRepository.findAll().stream()
                .filter(p -> containsAny(q,
                        p.getName(),
                        p.getCategoryName(),
                        p.getBaseUnit(),
                        p.getDescription()))
                .map(this::mapProduct)
                .toList();

        List<SearchSuggestionRes> customers = customerRepository.findAll().stream()
                .filter(c -> containsAny(q,
                        c.getName(),
                        c.getPhone(),
                        c.getEmail(),
                        c.getAddress(),
                        c.getTaxCode()))
                .map(this::mapCustomer)
                .toList();

        List<SearchSuggestionRes> receipts = purchaseReceiptsRepository.findAll().stream()
                .filter(r -> containsAny(q,
                        String.valueOf(r.getId()),
                        r.getSupplier(),
                        r.getNote(),
                        r.getName()))
                .map(this::mapReceipt)
                .toList();

        return Stream.of(orders, products, customers, receipts)
                .flatMap(Collection::stream)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private SearchSuggestionRes mapOrder(Order o) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(o.getId()))
                .entityType("ORDER")
                .entityLabel("Hóa đơn")
                .title(String.valueOf(o.getId()))
                .subtitle(join(" • ", o.getNameCustomer(), o.getPhoneCustomer()))
                .meta(join(" • ", money(o.getTotal()), o.getCreatedAt() != null ? o.getCreatedAt().format(DTF) : null))
                .build();
    }

    private SearchSuggestionRes mapProduct(Product p) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(p.getId()))
                .entityType("PRODUCT")
                .entityLabel("Sản phẩm")
                .title(p.getName())
                .subtitle(join(" • ", p.getCategoryName(), p.getBaseUnit()))
                .meta(p.getWarningQuantity() != null ? "Cảnh báo tồn: " + p.getWarningQuantity() : null)
                .build();
    }

    private SearchSuggestionRes mapCustomer(Customer c) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(c.getId()))
                .entityType("CUSTOMER")
                .entityLabel("Khách hàng")
                .title(c.getName())
                .subtitle(join(" • ", c.getPhone(), c.getEmail()))
                .meta(join(" • ", c.getAddress(), c.getTaxCode()))
                .build();
    }

    private SearchSuggestionRes mapReceipt(PurchaseReceipts r) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(r.getId()))
                .entityType("PURCHASE_RECEIPT")
                .entityLabel("Phiếu nhập")
                .title("PN" + r.getId() + " • " + r.getName())
                .subtitle(r.getSupplier())
                .meta(join(" • ",
                        r.getTotalQuantity() != null ? "SL: " + r.getTotalQuantity() : null,
                        r.getCost() != null ? money(r.getCost()) : null,
                        r.getCreatedAt() != null ? r.getCreatedAt().format(DTF) : null))
                .build();
    }

    private boolean containsAny(String keyword, String... values) {
        for (String value : values) {
            if (normalize(value).contains(keyword)) return true;
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }

    private String join(String delimiter, String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .filter(v -> !v.isBlank())
                .collect(Collectors.joining(delimiter));
    }

    private String money(Double value) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + "đ";
    }
}