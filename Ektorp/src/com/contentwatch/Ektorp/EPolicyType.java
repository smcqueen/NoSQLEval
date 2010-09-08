package com.contentwatch.Ektorp;

public enum EPolicyType {
	POLICY_TYPE_INVALID(0),
	POLICY_TYPE_ORG(1),
	POLICY_TYPE_PRODUCT(2),
	POLICY_TYPE_MACHINE(3),
	POLICY_TYPE_GROUP(4),
	POLICY_TYPE_USER(5);
	
	int value;
	
	EPolicyType(int val) {
		value = val;
	}
}
