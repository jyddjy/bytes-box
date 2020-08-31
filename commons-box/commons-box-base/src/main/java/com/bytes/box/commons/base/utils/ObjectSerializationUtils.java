package com.bytes.box.commons.base.utils;

import lombok.SneakyThrows;

import java.io.*;

public class ObjectSerializationUtils {

    @SneakyThrows
    public static byte[] toByteArray(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    @SneakyThrows
    public static Object toObject(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
}
