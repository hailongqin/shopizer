package com.salesmanager.core.business.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.generic.exception.ServiceException;
import com.salesmanager.core.business.generic.service.SalesManagerEntityServiceImpl;
import com.salesmanager.core.business.merchant.service.MerchantStoreService;
import com.salesmanager.core.business.reference.language.service.LanguageService;
import com.salesmanager.core.business.user.dao.PermissionDao;
import com.salesmanager.core.business.user.model.Permission;

@Service("permissionService")
public class PermissionServiceImpl extends
		SalesManagerEntityServiceImpl<Integer, Permission> implements
		PermissionService {

	PermissionDao permissionDao;

	@Autowired
	private LanguageService languageService;

	@Autowired
	private MerchantStoreService merchantService;

	@Autowired
	public PermissionServiceImpl(PermissionDao permissionDao) {
		super(permissionDao);
		this.permissionDao = permissionDao;

	}

	@Override
	public List<Permission> getByName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Permission> listByStore() throws ServiceException {
		try {
			return permissionDao.listByStore();
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

}
