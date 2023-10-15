package com.axreng.backend.enums;

public enum SearchStatus {
    ACTIVE("active"),
    DONE("done");

    private final String nome;

    public String getNome() {
        return nome;
    }

    SearchStatus(String nome) {
        this.nome = nome;
    }

}