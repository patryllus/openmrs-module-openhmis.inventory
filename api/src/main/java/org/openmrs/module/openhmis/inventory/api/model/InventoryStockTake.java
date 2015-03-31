package org.openmrs.module.openhmis.inventory.api.model;

import java.util.List;

import org.openmrs.BaseOpenmrsMetadata;

public class InventoryStockTake extends BaseOpenmrsMetadata {

	public static final long serialVersionUID = 0L;

	private String operationNumber;
	private List <InventoryStockTakeEntity> inventoryStockTakeList;

	public String getOperationNumber() {
		return operationNumber;
	}

	public void setOperationNumber(String operationNumber) {
		this.operationNumber = operationNumber;
	}

	public List<InventoryStockTakeEntity> getInventoryStockTakeList() {
		return inventoryStockTakeList;
	}

	public void setInventoryStockTakeList(List<InventoryStockTakeEntity> inventoryStockTakeList) {
		this.inventoryStockTakeList = inventoryStockTakeList;
	}

	@Override
	public Integer getId() {
		return null;
	}

	@Override
	public void setId(Integer id) {

	}

}