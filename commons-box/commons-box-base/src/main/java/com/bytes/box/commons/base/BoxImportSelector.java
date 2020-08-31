package com.bytes.box.commons.base;

import com.bytes.box.commons.base.annocation.EnableBox;
import com.google.common.collect.Lists;
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

        imports.addAll(Arrays.asList(attributes.getClassArray("imports")));

        return imports.stream().map(Class::getName).collect(Collectors.toList()).toArray(new String[]{});
    }
}
