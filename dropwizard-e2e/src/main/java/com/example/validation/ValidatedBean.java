package com.example.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

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
    public ValidatedBean(
            @JsonProperty("string") String string,
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
