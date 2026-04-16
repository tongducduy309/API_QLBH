package com.gener.qlbh.services;

import com.gener.qlbh.dtos.response.SearchSuggestionRes;
import com.gener.qlbh.models.*;
import com.gener.qlbh.models.Inventory;
import com.gener.qlbh.repositories.*;
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
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryLotRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseReceiptsRepository purchaseReceiptsRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<SearchSuggestionRes> globalSearch(String keyword, int limit) {
        String q = normalize(keyword);
        if (q.isBlank()) return Collections.emptyList();

        List<SearchSuggestionRes> orders = searchOrders(q);
        List<SearchSuggestionRes> products = searchProducts(q);
        List<SearchSuggestionRes> variants = searchProductVariants(q);
        List<SearchSuggestionRes> inventories = searchInventories(q);
        List<SearchSuggestionRes> customers = searchCustomers(q);
        List<SearchSuggestionRes> receipts = searchReceipts(q);

        return Stream.of(orders, products,variants,inventories, customers, receipts)
                .flatMap(Collection::stream)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<SearchSuggestionRes> searchOrders(String q) {
        return orderRepository.findAll().stream()
                .filter(o -> containsAny(q,
                        o.getCode(),
                        o.getNameCustomer(),
                        o.getPhoneCustomer(),
                        o.getAddressCustomer()))
                .map(this::mapOrder)
                .toList();
    }

    private List<SearchSuggestionRes> searchProducts(String q) {
        Map<Long, Product> foundProducts = new LinkedHashMap<>();

        // 1. Tìm trực tiếp theo Product
        productRepository.findAll().stream()
                .filter(p -> containsAny(q,
                        p.getName(),
                        p.getCategoryName(),
                        p.getBaseUnit(),
                        p.getDescription()))
                .forEach(p -> foundProducts.put(p.getId(), p));

        productVariantRepository.findAll().stream()
                .filter(v -> containsAny(q,
                        v.getSku(),
                        v.getVariantCode(),
                        v.getWeight(),
                        toMoneySearch(v.getRetailPrice()),
                        toMoneySearch(v.getStorePrice())))
                .map(ProductVariant::getProduct)
                .filter(Objects::nonNull)
                .forEach(p -> foundProducts.put(p.getId(), p));

        return foundProducts.values().stream()
                .map(this::mapProduct)
                .toList();
    }

    private List<SearchSuggestionRes> searchProductVariants(String q) {
        Map<Long, ProductVariant> foundVariants = new LinkedHashMap<>();

        productVariantRepository.findAll().stream()
                .filter(v -> containsAny(q,
                        v.getSku(),
                        v.getVariantCode(),
                        v.getWeight(),
                        toMoneySearch(v.getRetailPrice()),
                        toMoneySearch(v.getStorePrice())))
                .forEach(p -> foundVariants.put(p.getId(), p));

        inventoryLotRepository.findAll().stream()
                .filter(i -> containsAny(q,
                        i.getInventoryCode()
                ))
                .map(Inventory::getVariant)
                .filter(Objects::nonNull)
                .forEach(p -> foundVariants.put(p.getId(), p));


        return foundVariants.values().stream()
                .map(this::mapVariant)
                .toList();
    }

    private List<SearchSuggestionRes> searchInventories(String q) {
        Map<Long, Inventory> foundInventories = new LinkedHashMap<>();

        inventoryLotRepository.findAll().stream()
                .filter(i -> containsAny(q,
                        i.getInventoryCode()
                ))
                .forEach(p -> foundInventories.put(p.getId(), p));


        return foundInventories.values().stream()
                .map(this::mapInventory)
                .toList();
    }

    private List<SearchSuggestionRes> searchCustomers(String q) {
        return customerRepository.findAll().stream()
                .filter(c -> containsAny(q,
                        c.getName(),
                        c.getPhone(),
                        c.getEmail(),
                        c.getAddress(),
                        c.getTaxCode()))
                .map(this::mapCustomer)
                .toList();
    }

    private List<SearchSuggestionRes> searchReceipts(String q) {
        return purchaseReceiptsRepository.findAll().stream()
                .filter(r -> containsAny(q,
                        String.valueOf(r.getId()),
                        r.getSupplier(),
                        r.getNote(),
                        r.getName()))
                .map(this::mapReceipt)
                .toList();
    }

    private SearchSuggestionRes mapOrder(Order o) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(o.getId()))
                .entityType("ORDER")
                .entityLabel("Hóa đơn")
                .title(o.getCode())
                .subtitle(join(" • ", o.getNameCustomer(), o.getPhoneCustomer()))
                .meta(join(" • ",
                        money(o.getTotal()),
                        o.getCreatedAt() != null ? o.getCreatedAt().format(DTF) : null))
                .build();
    }

    private SearchSuggestionRes mapProduct(Product p) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(p.getId()))
                .entityType("PRODUCT")
                .entityLabel("Sản phẩm")
                .title(p.getName())
                .subtitle(join(" • ", p.getCategoryName(), p.getBaseUnit()))
                .meta(p.getWarningQuantity() != null ? "Cảnh báo tồn: " + trimNumber(p.getWarningQuantity()) : null)
                .build();
    }

    private SearchSuggestionRes mapVariant(ProductVariant p) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(p.getProduct().getId()))
                .entityType("PRODUCT")
                .entityLabel("Biến thể")
                .title(p.getProduct().getName() + (p.getSku()!=null&&!p.getSku().isEmpty()?("("+p.getSku()+")"):""))
                .subtitle(p.getVariantCode())
                .meta(join(" • ",
                        p.getRetailPrice() != null ? "Giá bản lẻ: " + trimNumber(p.getRetailPrice()) : null,
                        p.getStorePrice() != null ? "Giá cửa hàng: " + trimNumber(p.getStorePrice()) : null))
                .build();
    }

    private SearchSuggestionRes mapInventory(Inventory p) {
        return SearchSuggestionRes.builder()
                .entityId(String.valueOf(p.getVariant().getProduct().getId()))
                .entityType("PRODUCT")
                .entityLabel("Tồn kho")
                .title(p.getVariant().getProduct().getName() + (p.getVariant().getSku()!=null&&!p.getVariant().getSku().isEmpty()?("("+p.getVariant().getSku()+")"):""))
                .subtitle(p.getInventoryCode())
//                .meta(p.getWarningQuantity() != null ? "Cảnh báo tồn: " + trimNumber(p.getWarningQuantity()) : null)
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
                .title("Số: #" + r.getId() + " • " + r.getName())
                .subtitle(r.getSupplier())
                .meta(join(" • ",
                        r.getTotalQuantity() != null ? "SL: " + trimNumber(r.getTotalQuantity()) : null,
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
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.joining(delimiter));
    }

    private String money(Double value) {
        if (value == null) return null;
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    private String toStr(Double value) {
        if (value == null) return null;
        return trimNumber(value);
    }

    private String toMoneySearch(Double value) {
        if (value == null) return null;
        String plain = trimNumber(value);
        String formatted = NumberFormat.getInstance(new Locale("vi", "VN")).format(value);
        return plain + " " + formatted + " " + formatted + "đ";
    }

    private String trimNumber(Double value) {
        if (value == null) return null;
        if (value % 1 == 0) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }
}