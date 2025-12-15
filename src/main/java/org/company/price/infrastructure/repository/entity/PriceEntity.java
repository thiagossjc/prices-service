package org.company.price.infrastructure.repository.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity mapping the PRICES table.
 * Fields:
 * - brandId: foreign key to the brand/chain (e.g. 1 = ZARA)
 * - startDate / endDate: inclusive range when the price applies
 * - priceList: identifier of the price list / tariff
 * - productId: product identifier
 * - priority: tiebreaker when several rates overlap (higher value wins)
 * - price: final sale price (use BigDecimal for exact decimals)
 * - currency: ISO currency code (EUR, USD, ...)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("prices")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceEntity {

    @Id
    Long id;

    /**
     * BRAND_ID: foreign key of the chain (e.g. 1 = ZARA)
     */
    @Column("brand_id")
    Integer brandId;

    /**
     * PRODUCT_ID: product code / identifier
     */
    @Column("product_id")
    Integer productId;

    /**
     * PRICE_LIST: identifier of the applicable price list / tariff
     */
    @Column("price_list")
    Integer priceList;

    /**
     * START_DATE: start of the validity period (inclusive)
     */
    @Column("start_date")
    LocalDateTime startDate;

    /**
     * END_DATE: end of the validity period (inclusive)
     */
    @Column("end_date")
    LocalDateTime endDate;

    /**
     * PRIORITY: higher numeric value wins when ranges overlap
     */
    @Column("priority")
    Integer priority;

    /**
     * PRICE: final sale price, use BigDecimal for precise currency values
     */
    @Column("price")
    private BigDecimal price;

    /**
     * CURR: ISO currency code (e.g. "EUR")
     */
    @Column("curr")
    private String currency;
}
