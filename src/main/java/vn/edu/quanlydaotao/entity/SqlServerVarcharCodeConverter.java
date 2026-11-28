package vn.edu.quanlydaotao.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SqlServerVarcharCodeConverter implements AttributeConverter<String, String> {

	// Keep Vietnamese codes readable despite the existing SQL Server VARCHAR encoding.
	private static final String REQUESTED_CODE = "TMĐT";
	private static final String SQL_SERVER_VARCHAR_CODE = "TMÐT";

	@Override
	public String convertToDatabaseColumn(String code) {
		return REQUESTED_CODE.equalsIgnoreCase(code) ? SQL_SERVER_VARCHAR_CODE : code;
	}

	@Override
	public String convertToEntityAttribute(String code) {
		return SQL_SERVER_VARCHAR_CODE.equalsIgnoreCase(code) ? REQUESTED_CODE : code;
	}
}
