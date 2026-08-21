/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.entity.read;

/**
 *
 * @author lenovo
 */
public enum TransactionType {
    DEPOSIT("deposito"),
    WITHDRAW("saque"),
    TRANSFER("transferencia");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}