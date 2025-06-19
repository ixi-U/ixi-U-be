package com.ixi_U.common.converter;

import com.ixi_U.plan.entity.PlanType;
import java.util.Set;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

// 1-1. 쓰기 전용: PlanType → Value
public class PlanTypeWritingConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(PlanType.class, Value.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor srcType, TypeDescriptor tgtType) {
        PlanType planType = (PlanType) source;
        // enum.name() 대신 ordinal() 등 다른 로직도 가능
        return Values.value(planType.name());
    }
}