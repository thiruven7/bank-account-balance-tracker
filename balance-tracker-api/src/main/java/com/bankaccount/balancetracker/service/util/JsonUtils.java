package com.bankaccount.balancetracker.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to
 */
@Slf4j
public class JsonUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	private JsonUtils(){

	}

	public static String toJson(Object object) {
		try {
			mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return ("Failed to serialise submission message : " + e.getMessage());
		}
	}

}
