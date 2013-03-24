package com.yammer.dropwizard.jersey;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.validation.InvalidEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

@Provider
public class InvalidEntityToJsonExceptionMapper implements ExceptionMapper<InvalidEntityException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidEntityToJsonExceptionMapper.class);

    @Override
    public Response toResponse(InvalidEntityException exception) {
        return Response.status(InvalidEntityExceptionMapper.UNPROCESSABLE_ENTITY)
                .type(MediaType.APPLICATION_JSON)
                .entity(mapViolationsToJson(exception))
                .build();
    }

    private List<ConstraintViolationJson> mapViolationsToJson(InvalidEntityException exception) {

        final ImmutableList<ConstraintViolation> violations = exception.getResult().getViolations();
        final List<ConstraintViolationJson> jsonList = new ArrayList<ConstraintViolationJson>(violations.size());
        for (ConstraintViolation violation : violations) {
            jsonList.add(translateViolationToJson(violation));
        }
        return jsonList;
    }

    private ConstraintViolationJson translateViolationToJson(ConstraintViolation violation) {
        String invalidValue;
        try {
            invalidValue = violation.getInvalidValue().toString();
        } catch (Exception e) {
            invalidValue = "Error converting invalid value to String: "+e;
        }
        return new ConstraintViolationJson(violation.getMessageTemplate(), violation.getMessage(), invalidValue);
    }

}
