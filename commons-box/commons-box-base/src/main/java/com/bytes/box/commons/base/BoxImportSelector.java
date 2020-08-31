package com.bytes.box.commons.base;

import com.bytes.bfs.support.common.box.annocation.EnableBox;
import com.bytes.bfs.support.common.box.encrypt.resolve.ClientBodyResolve;
import com.bytes.bfs.support.common.box.encrypt.resolve.FeignClientBodyResolve;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoxImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {

        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(EnableBox.class.getName()));

        List<Class<?>> imports = Lists.newArrayList();

        if (BooleanUtils.isTrue(attributes.getBoolean("encrypt"))) {
            imports.addAll(ImmutableList.of(ClientBodyResolve.class));
        }

        if (BooleanUtils.isTrue(attributes.getBoolean("feignEncrypt"))) {
            imports.addAll(ImmutableList.of(FeignClientBodyResolve.class));
        }

        imports.addAll(Arrays.asList(attributes.getClassArray("imports")));

        return imports.stream().map(Class::getName).collect(Collectors.toList()).toArray(new String[]{});
    }
}
