package org.openmrs.module.openhmis.inventory.api.impl;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.module.openhmis.commons.api.PagingInfo;
import org.openmrs.module.openhmis.commons.api.entity.impl.BaseCustomizableObjectDataServiceImpl;
import org.openmrs.module.openhmis.commons.api.f.Action1;
import org.openmrs.module.openhmis.inventory.api.IStockRoomTransactionDataService;
import org.openmrs.module.openhmis.inventory.api.model.StockRoom;
import org.openmrs.module.openhmis.inventory.api.model.StockRoomTransaction;
import org.openmrs.module.openhmis.inventory.api.model.StockRoomTransactionStatus;
import org.openmrs.module.openhmis.inventory.api.model.StockRoomTransactionType;
import org.openmrs.module.openhmis.inventory.api.security.TransactionAuthorizationPrivileges;

import java.util.List;

public class StockRoomTransactionDataServiceImpl
		extends BaseCustomizableObjectDataServiceImpl<StockRoomTransaction, TransactionAuthorizationPrivileges>
		implements IStockRoomTransactionDataService {
	@Override
	protected TransactionAuthorizationPrivileges getPrivileges() {
		return new TransactionAuthorizationPrivileges();
	}

	@Override
	protected void validate(StockRoomTransaction object) throws APIException {
	}

	@Override
	public StockRoomTransaction getTransactionByNumber(String transactionNumber) {
		if (StringUtils.isEmpty(transactionNumber)) {
			throw new IllegalArgumentException("The transaction number to find must be defined.");
		}
		if (transactionNumber.length() > 50) {
			throw new IllegalArgumentException("The transaction number must be less than 51 characters.");
		}

		Criteria criteria = repository.createCriteria(getEntityClass());
		criteria.add(Restrictions.eq("transactionNumber", transactionNumber));

		return repository.selectSingle(getEntityClass(), criteria);
	}

	@Override
	public List<StockRoomTransaction> getTransactionsByRoom(final StockRoom stockRoom, PagingInfo paging) {
		if (stockRoom == null) {
			throw new NullPointerException("The stock room must be defined.");
		}

		return executeCriteria(StockRoomTransaction.class, paging, new Action1<Criteria>() {
			@Override
			public void apply(Criteria criteria) {
				criteria.add(Restrictions.or(
						Restrictions.eq("source", stockRoom),
						Restrictions.eq("destination", stockRoom)
				));
			}
		});
	}

	@Override
	public List<StockRoomTransaction> getUserPendingTransactions(final User user, PagingInfo paging) {
		if (user == null) {
			throw new NullPointerException("The user must be defined.");
		}

		return executeCriteria(StockRoomTransaction.class, paging, new Action1<Criteria>() {
			@Override
			public void apply(Criteria criteria) {
				// First find any transactions types that have attribute types which are for the specified user or role
				DetachedCriteria subQuery = DetachedCriteria.forClass(StockRoomTransactionType.class);
				subQuery.createAlias("attributeTypes", "at")
						.add(Restrictions.or(
								// Transaction types that require user approval
								Restrictions.eq("at.user", user),
								// Transaction types that require role approval
								Restrictions.in("at.role", user.getRoles())
						))
						.setProjection(Property.forName("id"));

				// Join the above criteria as a sub-query to the transaction transaction type
				criteria.add(Restrictions.and(
						Restrictions.eq("status", StockRoomTransactionStatus.PENDING),
						Restrictions.or(
								// Transactions created by the user
								Restrictions.eq("creator", user),
								Property.forName("transactionType").in(subQuery)
						)
				));
			}
		});
	}
}
