
package com.salesmanager.web.populator.manufacturer;

import java.util.Set;

import com.salesmanager.core.business.catalog.product.model.manufacturer.ManufacturerDescription;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.utils.AbstractDataPopulator;
import com.salesmanager.web.entity.catalog.Manufacturer;


/**
 * @author Carl Samson
 *
 */


public class ManufacturerPopulator extends AbstractDataPopulator<com.salesmanager.core.business.catalog.product.model.manufacturer.Manufacturer,com.salesmanager.web.entity.catalog.Manufacturer>
{



	@Override
	public com.salesmanager.core.business.catalog.product.model.manufacturer.Manufacturer populateToEntity(
			com.salesmanager.core.business.catalog.product.model.manufacturer.Manufacturer source,
			Manufacturer target, MerchantStore store) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Manufacturer populateFromEntity(
			com.salesmanager.core.business.catalog.product.model.manufacturer.Manufacturer source,
			Manufacturer target, MerchantStore store, Language language) {
		target.setId(source.getId());
		if(source.getDescriptions()!=null && source.getDescriptions().size()>0) {
			
				Set<ManufacturerDescription> descriptions = source.getDescriptions();
				ManufacturerDescription description = null;
				for(ManufacturerDescription desc : descriptions) {
					if(desc.getLanguage().getCode().equals(language.getCode())) {
						description = desc;
						break;
					}
				}
				
				if (description != null) {
					target.setName(description.getName());
				}

		}
		
		target.setLanguage(language.getCode());
		return target;
	}

}