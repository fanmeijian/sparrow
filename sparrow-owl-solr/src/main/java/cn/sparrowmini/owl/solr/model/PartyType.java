package cn.sparrowmini.owl.solr.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.ReadOnlyProperty;
// 引入 SolrJ 注解
import org.apache.solr.client.solrj.beans.Field;

public class PartyType extends Concept implements IParty, ICustomPropertyAware {

    @Field("idSorted")
    private Double idSorted;

    @Field("legalName")
    private String legalName;

    @Field("lowercaseLegalName")
    private String lowercaseLegalName;

    @Field("*_brandName")
    private Map<String, String> brandName;

    @Field("*_origin")
    private Map<String, String> origin;

    @Field("certificateType")
    private Set<String> certificateType;

    @Field("circularEconomyCertificates")
    private Set<String> circularEconomyCertificates;

    @Field("ppapComplianceLevel")
    private Integer ppapComplianceLevel;

    @Field("*_ppapDocumentType")
    private Map<String, String> ppapDocumentType;

    @Field("trustScore")
    private Double trustScore;

    @Field("trustRating")
    private Double trustRating;

    @Field("locationLatitude")
    private Double locationLatitude;

    @Field("locationLongitude")
    private Double locationLongitude;

    @Field("trustTradingVolume")
    private Double trustTradingVolume;

    @Field("trustSellerCommunication")
    private Double trustSellerCommunication;

    @Field("trustFullfillmentOfTerms")
    private Double trustFullfillmentOfTerms;

    @Field("trustDeliveryPackaging")
    private Double trustDeliveryPackaging;

    @Field("trustNumberOfTransactions")
    private Double trustNumberOfTransactions;

    @Field("trustNumberOfEvaluations")
    private Double trustNumberOfEvaluations;

    @Field("logoId")
    private String logoId;

    @Field("businessType")
    private String businessType;

    @Field("*_activitySectors")
    private Map<String, Collection<String>> activitySectors;

    @Field("*_businessKeywords")
    protected Map<String, Collection<String>> businessKeywords;

    @Field("verified")
    private Boolean isVerified = false;

    @Field("website")
    private String website;

    @Field("basePlatformCompanyId")
    private String basePlatformCompanyId;

    @Field("*_is")
    private Map<String, Collection<Integer>> customIntValues;

    @Field("*_ds")
    private Map<String, Collection<Double>> customDoubleValues;

    @Field("*_ss")
    private Map<String, Collection<String>> customStringValues;

    @Field("*_b")
    private Map<String, Boolean> customBooleanValue;

    @Field("*_key")
    private Map<String, String> customPropertyKeys;

    private Map<String, PropertyType> customProperties;

    public PartyType() {
    }

    public String getCollection() {
        return "party";
    }

    public String getId() {
        return this.getUri();
    }

    public void setId(String id) {
        this.setUri(id);
        this.setIdSorted(Double.valueOf(id));
    }

    public Map<String, String> getOrigin() {
        return this.origin;
    }

    public void setOrigin(Map<String, String> originMap) {
        if (originMap != null) {
            for(String key : originMap.keySet()) {
                this.addOrigin(key, originMap.get(key));
            }
        } else {
            this.origin = null;
        }
    }

    public void addOrigin(String language, String label) {
        if (this.origin == null) {
            this.origin = new HashMap<>();
        }

        this.origin.put(language, label);
        this.addLanguage(language);
    }

    public Map<String, String> getBrandName() {
        return this.brandName;
    }

    public void setBrandName(Map<String, String> brandNameMap) {
        if (brandNameMap != null) {
            for(String key : brandNameMap.keySet()) {
                this.addBrandName(key, brandNameMap.get(key));
            }
        } else {
            this.brandName = null;
        }
    }

    public void addBrandName(String language, String label) {
        if (this.brandName == null) {
            this.brandName = new HashMap<>();
        }

        this.brandName.put(language, label);
        this.addLanguage(language);
    }

    public Set<String> getCertificateType() {
        return this.certificateType;
    }

    public void setCertificateType(Set<String> certificateType) {
        this.certificateType = certificateType;
    }

    public Set<String> getCircularEconomyCertificates() {
        return this.circularEconomyCertificates;
    }

    public void setCircularEconomyCertificates(Set<String> circularEconomyCertificates) {
        this.circularEconomyCertificates = circularEconomyCertificates;
    }

    public Integer getPpapComplianceLevel() {
        return this.ppapComplianceLevel;
    }

    public void setPpapComplianceLevel(Integer ppapComplianceLevel) {
        this.ppapComplianceLevel = ppapComplianceLevel;
    }

    public Double getTrustScore() {
        return this.trustScore;
    }

    public void setTrustScore(Double trustScore) {
        this.trustScore = trustScore;
    }

    public Double getLocationLatitude() {
        return this.locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return this.locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public Double getTrustRating() {
        return this.trustRating;
    }

    public void setTrustRating(Double trustRating) {
        this.trustRating = trustRating;
    }

    public Double getTrustTradingVolume() {
        return this.trustTradingVolume;
    }

    public void setTrustTradingVolume(Double trustTradingVolume) {
        this.trustTradingVolume = trustTradingVolume;
    }

    public Double getTrustSellerCommunication() {
        return this.trustSellerCommunication;
    }

    public void setTrustSellerCommunication(Double trustSellerCommunication) {
        this.trustSellerCommunication = trustSellerCommunication;
    }

    public Double getTrustFullfillmentOfTerms() {
        return this.trustFullfillmentOfTerms;
    }

    public void setTrustFullfillmentOfTerms(Double trustFullfillmentOfTerms) {
        this.trustFullfillmentOfTerms = trustFullfillmentOfTerms;
    }

    public Double getTrustDeliveryPackaging() {
        return this.trustDeliveryPackaging;
    }

    public void setTrustDeliveryPackaging(Double trustDeliveryPackaging) {
        this.trustDeliveryPackaging = trustDeliveryPackaging;
    }

    public Double getTrustNumberOfTransactions() {
        return this.trustNumberOfTransactions;
    }

    public void setTrustNumberOfTransactions(Double trustNumberOfTransactions) {
        this.trustNumberOfTransactions = trustNumberOfTransactions;
    }

    public Double getTrustNumberOfEvaluations() {
        return this.trustNumberOfEvaluations;
    }

    public void setTrustNumberOfEvaluations(Double trustNumberOfEvaluations) {
        this.trustNumberOfEvaluations = trustNumberOfEvaluations;
    }

    public String getLegalName() {
        return this.legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
        if (legalName != null) {
            String legalNameWithoutAccents = StringUtils.stripAccents(legalName);
            this.lowercaseLegalName = legalNameWithoutAccents.toLowerCase();
        }
    }

    public Map<String, String> getPpapDocumentType() {
        return this.ppapDocumentType;
    }

    public void setPpapDocumentType(Map<String, String> originMap) {
        if (originMap != null) {
            for(String key : originMap.keySet()) {
                this.addLabel(key, originMap.get(key));
            }
        } else {
            this.ppapDocumentType = null;
        }
    }

    public void addPpapDocumentType(String language, String label) {
        if (this.ppapDocumentType == null) {
            this.ppapDocumentType = new HashMap<>();
        }

        this.ppapDocumentType.put(language, label);
        this.addLanguage(language);
    }

    public Double getIdSorted() {
        return this.idSorted;
    }

    public void setIdSorted(Double idSorted) {
        this.idSorted = idSorted;
    }

    public String getLogoId() {
        return this.logoId;
    }

    public void setLogoId(String logoId) {
        this.logoId = logoId;
    }

    public String getBusinessType() {
        return this.businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Map<String, Collection<String>> getActivitySectors() {
        return this.activitySectors;
    }

    public void setActivitySectors(Map<String, Collection<String>> activitySectors) {
        this.activitySectors = activitySectors;
    }

    public Map<String, Collection<String>> getBusinessKeywords() {
        return this.businessKeywords;
    }

    public void setBusinessKeywords(Map<String, Collection<String>> businessKeywords) {
        this.businessKeywords = businessKeywords;
    }

    @SuppressWarnings("unchecked")
    public void addActivitySector(String language, String activitySector) {
        if (this.activitySectors == null) {
            this.activitySectors = new HashMap<>();
        }

        if (!this.activitySectors.containsKey(language)) {
            this.activitySectors.put(language, new HashSet<>());
        }

        this.activitySectors.get(language).add(activitySector);
        this.addLanguage(language);
    }

    @SuppressWarnings("unchecked")
    public void addBusinessKeyword(String language, String businessKeyword) {
        if (this.businessKeywords == null) {
            this.businessKeywords = new HashMap<>();
        }

        if (!this.businessKeywords.containsKey(language)) {
            this.businessKeywords.put(language, new HashSet<>());
        }

        this.businessKeywords.get(language).add(businessKeyword);
        this.addLanguage(language);
    }

    public Boolean getVerified() {
        return this.isVerified;
    }

    public void setVerified(Boolean verified) {
        this.isVerified = verified;
    }

    public String getWebsite() {
        return this.website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBasePlatformCompanyId() {
        return this.basePlatformCompanyId;
    }

    public void setBasePlatformCompanyId(String basePlatformCompanyId) {
        this.basePlatformCompanyId = basePlatformCompanyId;
    }

    public Map<String, Collection<Integer>> getCustomIntValues() {
        if (this.customIntValues == null) {
            this.customIntValues = new HashMap<>();
        }
        return this.customIntValues;
    }

    public Map<String, Collection<Double>> getCustomDoubleValues() {
        if (this.customDoubleValues == null) {
            this.customDoubleValues = new HashMap<>();
        }
        return this.customDoubleValues;
    }

    public Map<String, Collection<String>> getCustomStringValues() {
        if (this.customStringValues == null) {
            this.customStringValues = new HashMap<>();
        }
        return this.customStringValues;
    }

    public Map<String, Boolean> getCustomBooleanValue() {
        if (this.customBooleanValue == null) {
            this.customBooleanValue = new HashMap<>();
        }
        return this.customBooleanValue;
    }

    public Map<String, String> getCustomPropertyKeys() {
        if (this.customPropertyKeys == null) {
            this.customPropertyKeys = new HashMap<>();
        }
        return this.customPropertyKeys;
    }

    @ReadOnlyProperty
    public Map<String, PropertyType> getCustomProperties() {
        if (this.customProperties == null) {
            this.customProperties = new HashMap<>();
        }
        return this.customProperties;
    }
}