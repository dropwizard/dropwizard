package io.dropwizard.logging.json.layout;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

public class ExceptionFormat {
    private boolean rootFirst = true;
    private String depth = "full";
    private List<String> evaluators = Collections.emptyList();

    @JsonProperty
    public boolean isRootFirst() {
        return rootFirst;
    }

    @JsonProperty
    public void setRootFirst(boolean rootFirst) {
        this.rootFirst = rootFirst;
    }

    @JsonProperty
    public String getDepth() {
        return depth;
    }

    @JsonProperty
    public void setDepth(String depth) {
        this.depth = depth;
    }

    @JsonProperty
    public List<String> getEvaluators() {
        return evaluators;
    }

    @JsonProperty
    public void setEvaluators(List<String> evaluators) {
        this.evaluators = evaluators;
    }
}
