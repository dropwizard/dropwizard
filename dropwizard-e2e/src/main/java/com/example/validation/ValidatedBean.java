package com.example.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Collection;

public class ValidatedBean {
    @JsonProperty("string")
    @NotBlank
    private final String string;

    @JsonProperty("number")
    @PositiveOrZero
    private final Long number;

    @JsonProperty("list")
    @Size(min = 1, max = 3)
    private final Collection<@NotBlank String> list;

    @JsonCreator
    public ValidatedBean(@JsonProperty("string") String string,
                         @JsonProperty("number") Long number,
                         @JsonProperty("list") Collection<String> list) {
        this.string = string;
        this.number = number;
        this.list = list;
    }

    public String getString() {
        return string;
    }

    public Long getNumber() {
        return number;
    }

    public Collection<String> getList() {
        return list;
    }
}
