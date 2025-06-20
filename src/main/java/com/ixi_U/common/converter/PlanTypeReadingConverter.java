package com.ixi_U.common.converter;

import com.ixi_U.plan.entity.PlanType;
import java.util.Set;
import org.neo4j.driver.Value;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

// 1-2. 읽기 전용: Value → PlanType
public class PlanTypeReadingConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(Value.class, PlanType.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor srcType, TypeDescriptor tgtType) {
        Value value = (Value) source;
        return PlanType.valueOf(value.asString());
    }
}