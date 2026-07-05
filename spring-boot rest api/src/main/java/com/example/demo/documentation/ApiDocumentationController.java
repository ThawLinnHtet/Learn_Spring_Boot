package com.example.demo.documentation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiDocumentationController {

	@GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> apiDocs() {
		return object(
				"openapi", "3.1.0",
				"info", object(
						"title", "Products REST API",
						"version", "v1",
						"description", "Learning CRUD API with PostgreSQL, Redis, Flyway, and validation."),
				"paths", object(
						"/api/products", object(
								"get", operation("List products", "Search, filter, sort, and paginate products.",
										List.of(parameter("name", "string"), parameter("minPrice", "number"),
												parameter("maxPrice", "number"), parameter("minQuantity", "integer"),
												parameter("maxQuantity", "integer"), parameter("page", "integer"),
												parameter("size", "integer"), parameter("sort", "string"),
												parameter("direction", "string")),
										response("200", "Products page", ref("PageResponseProductResponse"))),
								"post", operation("Create product", "Create a new product.", List.of(),
										requestBody("ProductCreateRequest"),
										response("201", "Created product", ref("ProductResponse")))),
						"/api/products/{id}", object(
								"get", operation("Get product", "Get a product by id.", List.of(pathId()),
										response("200", "Product", ref("ProductResponse"))),
								"put", operation("Update product", "Replace an existing product.", List.of(pathId()),
										requestBody("ProductUpdateRequest"),
										response("200", "Updated product", ref("ProductResponse"))),
								"delete", operation("Delete product", "Delete a product by id.", List.of(pathId()),
										response("204", "Product deleted", null)))),
				"components", object(
						"schemas", object(
								"ProductCreateRequest", productRequestSchema(),
								"ProductUpdateRequest", productRequestSchema(),
								"ProductResponse", productResponseSchema(),
								"PageResponseProductResponse", pageResponseSchema(),
								"ApiErrorResponse", apiErrorResponseSchema())));
	}

	private static Map<String, Object> operation(String summary, String description, List<Map<String, Object>> parameters,
			Map<String, Object> response) {
		return operation(summary, description, parameters, null, response);
	}

	private static Map<String, Object> operation(String summary, String description, List<Map<String, Object>> parameters,
			Map<String, Object> requestBody, Map<String, Object> response) {
		Map<String, Object> operation = object(
				"tags", List.of("Products"),
				"summary", summary,
				"description", description,
				"parameters", parameters,
				"responses", object("default", response("default", "Error", ref("ApiErrorResponse"))));
		operation.put("responses", object(responseCode(response), response, "default", response("default", "Error", ref("ApiErrorResponse"))));
		if (requestBody != null) {
			operation.put("requestBody", requestBody);
		}
		return operation;
	}

	private static String responseCode(Map<String, Object> response) {
		return (String) response.remove("x-response-code");
	}

	private static Map<String, Object> response(String code, String description, Map<String, Object> schema) {
		Map<String, Object> response = object("x-response-code", code, "description", description);
		if (schema != null) {
			response.put("content", object("application/json", object("schema", schema)));
		}
		return response;
	}

	private static Map<String, Object> requestBody(String schemaName) {
		return object(
				"required", true,
				"content", object("application/json", object("schema", ref(schemaName))));
	}

	private static Map<String, Object> pathId() {
		return object(
				"name", "id",
				"in", "path",
				"required", true,
				"schema", object("type", "integer", "format", "int64"));
	}

	private static Map<String, Object> parameter(String name, String type) {
		return object(
				"name", name,
				"in", "query",
				"required", false,
				"schema", object("type", type));
	}

	private static Map<String, Object> productRequestSchema() {
		return object(
				"type", "object",
				"required", List.of("name", "price", "quantity"),
				"properties", object(
						"name", object("type", "string", "maxLength", 120),
						"description", object("type", "string", "maxLength", 500),
						"price", object("type", "number", "minimum", 0),
						"quantity", object("type", "integer", "minimum", 0)));
	}

	private static Map<String, Object> productResponseSchema() {
		Map<String, Object> schema = productRequestSchema();
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
		properties.put("id", object("type", "integer", "format", "int64"));
		return schema;
	}

	private static Map<String, Object> pageResponseSchema() {
		return object(
				"type", "object",
				"properties", object(
						"content", object("type", "array", "items", ref("ProductResponse")),
						"page", object("type", "integer"),
						"size", object("type", "integer"),
						"totalElements", object("type", "integer", "format", "int64"),
						"totalPages", object("type", "integer"),
						"first", object("type", "boolean"),
						"last", object("type", "boolean")));
	}

	private static Map<String, Object> apiErrorResponseSchema() {
		return object(
				"type", "object",
				"properties", object(
						"timestamp", object("type", "string", "format", "date-time"),
						"status", object("type", "integer"),
						"error", object("type", "string"),
						"message", object("type", "string"),
						"path", object("type", "string"),
						"fieldErrors", object("type", "object")));
	}

	private static Map<String, Object> ref(String schemaName) {
		return object("$ref", "#/components/schemas/" + schemaName);
	}

	private static Map<String, Object> object(Object... keyValues) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			map.put((String) keyValues[i], keyValues[i + 1]);
		}
		return map;
	}
}
