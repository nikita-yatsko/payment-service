package com.payment.service.mapper;

import com.payment.service.model.dto.PaymentDto;
import com.payment.service.model.dto.PaymentRequest;
import com.payment.service.model.entity.Payment;
import com.payment.service.model.enums.PaymentStatus;
import org.bson.types.Decimal128;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.time.Instant;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {Instant.class, PaymentStatus.class}
)
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "timestamp", expression = "java(Instant.now())")
    Payment createPayment(PaymentRequest request, PaymentStatus status);

    PaymentDto toPaymentDto(Payment payment);

    default Decimal128 map(BigDecimal value)
    {
        return value == null ? null : new Decimal128(value);
    }

    default BigDecimal map(Decimal128 value)
    {
        return value == null ? null : value.bigDecimalValue();
    }
}
