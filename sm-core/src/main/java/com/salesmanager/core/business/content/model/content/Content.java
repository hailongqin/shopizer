package com.salesmanager.core.business.content.model.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.salesmanager.core.business.common.model.audit.AuditListener;
import com.salesmanager.core.business.common.model.audit.AuditSection;
import com.salesmanager.core.business.generic.model.SalesManagerEntity;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.constants.SchemaConstant;


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CONTENT", schema=SchemaConstant.SALESMANAGER_SCHEMA)
public class Content extends SalesManagerEntity<Long, Content> implements Serializable {

	

	private static final long serialVersionUID = 1772757159185494620L;
	
	@Id
	@Column(name = "CONTENT_ID", unique=true, nullable=false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_SEQ_NEXT_VAL")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;
	
	@Embedded
	private AuditSection auditSection = new AuditSection();
	
	@Valid
	@OneToMany(mappedBy="content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ContentDescription> descriptions = new ArrayList<ContentDescription>();
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MERCHANT_ID", nullable=false)
	private MerchantStore merchantSore;
	
	@NotEmpty
	@Column(name="CODE", unique=true, length=100, nullable=false)
	private String code;
	
	@Column(name = "VISIBLE")
	private boolean visible;
	
	//@Column(columnDefinition = "enum('BOX','PAGE')")//cannot create table with this 
	//@Enumerated(EnumType.STRING) 
	@Column(name = "CONTENT_TYPE")
	private ContentType contentType; 
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
		
	}

	public void setAuditSection(AuditSection auditSection) {
		this.auditSection = auditSection;
	}

	public AuditSection getAuditSection() {
		return auditSection;
	}

	public MerchantStore getMerchantSore() {
		return merchantSore;
	}

	public void setMerchantSore(MerchantStore merchantSore) {
		this.merchantSore = merchantSore;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public List<ContentDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<ContentDescription> descriptions) {
		this.descriptions = descriptions;
	}

}