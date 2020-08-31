package com.bytes.box.commons.base.response;

import org.apache.commons.lang3.tuple.Pair;

public interface DefineRestCode {

    Pair<String, String> getPair();

    @lombok.Builder
    class NewRestCode implements DefineRestCode {

        private String code;

        private String message;

        @Override
        public Pair<String, String> getPair() {
            return Pair.of(this.code, this.message);
        }
    }
}
