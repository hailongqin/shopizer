package com.salesmanager.core.business.user.dao;

import java.util.List;
import java.util.Set;

import com.salesmanager.core.business.generic.dao.SalesManagerEntityDao;
import com.salesmanager.core.business.user.model.Permission;

public interface PermissionDao extends SalesManagerEntityDao<Integer, Permission> {

	List<Permission> list();

	Permission getById(Integer permissionId);

	List<Permission> getPermissionsListByGroups(Set permissionIds);



}
